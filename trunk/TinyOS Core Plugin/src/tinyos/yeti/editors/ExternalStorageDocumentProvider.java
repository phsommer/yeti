/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos.yeti.editors;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.ide.FileStoreEditorInput;

import tinyos.yeti.editors.annotation.IAnnotationModelFactory;
import tinyos.yeti.editors.nesc.NesCDocumentPartitioner;

/**
 * @author ed
 * @version 1.0, May 19, 2003
 */
public class ExternalStorageDocumentProvider extends FileDocumentProvider {
	private AnnotationModel annotationModel;
	final static String ANNOTATION_MODEL_PROVIDER_EXTENSION_PREFIX = "TinyOS";
	final static String ANNOTATION_MODEL_PROVIDER_EXTENSION = "EditorAnnotationModelProvider";

	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
		if (element instanceof ExternalEditorInput) {
			ExternalEditorInput external = (ExternalEditorInput) element;
			FileStorage storage = (FileStorage)external.getStorage();
			String encoding = getEncoding(element);
			if (encoding == null)
				encoding = getDefaultEncoding();
			try {
				InputStream stream = new ByteArrayInputStream(document.get().getBytes(encoding));
				try {
					// inform about the upcoming content change
					fireElementStateChanging(element);
					storage.setContents(stream, overwrite, true, monitor);
					stream.close();
				} catch (RuntimeException e) {
					// inform about failure
					fireElementStateChangeFailed(element);
					throw e;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			super.doSaveDocument(monitor, element, document, overwrite);
		}
	}

	@Override
	protected IAnnotationModel createAnnotationModel( Object element ) throws CoreException{
		IAnnotationModel model = null;
		if( element instanceof ExternalEditorInput ){
			model = getAnnotationModelFromExtension(element);
			if(model == null) {
				model =  getAnnotationModel();
			}
		}
		else{
			model = super.createAnnotationModel( element );
			if(model == null) {
				model = getAnnotationModelFromExtension(element);
			}
		}

		return model;
	}

	public IAnnotationModel getAnnotationModel(){
		if( annotationModel == null ){
			annotationModel = new AnnotationModel();
		}
		return annotationModel;
	}

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = null;

		if (element instanceof ILocationProvider) {
			IPath path = ((ILocationProvider)element).getPath(element);
			FileStorage fs = new FileStorage(path);
			element = new ExternalEditorInput(fs);
		}

		if( element instanceof FileStoreEditorInput ){
			URI uri = ((FileStoreEditorInput)element).getURI();
			File file = new File( uri );
			if( file.exists() ){
				FileStorage fs = new FileStorage( new Path( file.getAbsolutePath() ));
				element = new ExternalEditorInput( fs );
			}
		}

		document = super.createDocument(element);

		if (document != null) {
			IDocumentPartitioner partitioner;

			if (document instanceof IDocumentExtension3) {
				IDocumentExtension3 extension3= (IDocumentExtension3) document;

				partitioner = extension3.getDocumentPartitioner( INesCPartitions.NESC_PARTITIONING );
				if( partitioner != null ){
					partitioner.disconnect();
				}

				partitioner = new NesCDocumentPartitioner();
				extension3.setDocumentPartitioner( INesCPartitions.NESC_PARTITIONING, partitioner);		
			} else {
				partitioner = document.getDocumentPartitioner();
				if( partitioner != null ){
					partitioner.disconnect();
				}

				partitioner = new NesCDocumentPartitioner();
				document.setDocumentPartitioner(partitioner);
			}
			partitioner.connect(document);
		}
		return document;
	}

	/**
	 * Find an extension which provides an annotation model for the given element and return the annotation
	 * model defined by the extension. Returns null if no extension for the content type of the element was found.
	 * @param element
	 * @return The annotation model provided by a fitting extension or null.
	 * @throws CoreException
	 */
	protected IAnnotationModel getAnnotationModelFromExtension(Object element) throws CoreException {
		IPath path = null;
		if(element instanceof ILocationProvider) {
			path = ((ILocationProvider)element).getPath(element);
		} else if(element instanceof IStorageEditorInput){
			IStorage storage = ((IStorageEditorInput)element).getStorage();
			if(storage != null){
				path = storage.getFullPath();
			}
		}
		if(path != null) {
			String contentId = getContentId(path);
			IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(ANNOTATION_MODEL_PROVIDER_EXTENSION_PREFIX, ANNOTATION_MODEL_PROVIDER_EXTENSION);
			if(extensionPoint != null) {
				IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
				for (int i= 0; i < elements.length; i++) {
					String contentTypeId = elements[i].getAttribute("contentTypeId");
					if( contentTypeId != null && contentTypeId.equals(contentId)) {
						IAnnotationModelFactory factory = (IAnnotationModelFactory)elements[i].createExecutableExtension("class");
						if(factory != null) {
							return  factory.createAnnotationModel(path);
						}
					}
				}
			}
		}
		return null;
	}

	private String getContentId(IPath path) {
		if(path != null) {
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			IContentType ct = contentTypeManager.findContentTypeFor(path.lastSegment());
			if(ct != null) {
				return ct.getId();
			}
		}
		return null;
	}

}