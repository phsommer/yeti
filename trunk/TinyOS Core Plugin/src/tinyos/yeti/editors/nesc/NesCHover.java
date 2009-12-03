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
package tinyos.yeti.editors.nesc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerHelpRegistry;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.editors.INesCEditorParserClient;
import tinyos.yeti.editors.NesCDocumentMap;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.NesCSourceViewerConfiguration;
import tinyos.yeti.editors.hyperlink.DocumentRegion;
import tinyos.yeti.editors.quickfixer.MarkerQuickFixInformation;
import tinyos.yeti.editors.spelling.SpellingQuickFixInformation;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.ep.parser.IDocumentRegion;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.IHoverInformation;
import tinyos.yeti.ep.parser.INesCAST;
import tinyos.yeti.ep.parser.INesCDocComment;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.jobs.PublicJob;
import tinyos.yeti.model.ProjectModel;

/**
 * This {@link ITextHover} searches for various {@link Annotation}s and returns
 * either their message or {@link IQuickFixInformation}s.
 * @author Benjamin Sigg
 */
public class NesCHover extends DefaultTextHover implements ITextHoverExtension, ITextHoverExtension2, INesCEditorParserClient{
	private NesCEditor editor;
	private NesCSourceViewerConfiguration configuration;
	
	private NesCInformationControlCreator creator;
	private INesCAST ast;
	private IASTReference[] references;
	private INesCInspector inspector;
	
	public NesCHover( NesCEditor editor, NesCSourceViewerConfiguration configuration ){
		super( editor.getEditorSourceViewer() );
		this.editor = editor;
		this.configuration = configuration;
		editor.addParserClient( this );
	}
	
	public void setupParser( NesCEditor editor, INesCParser parser ){
		parser.setCreateReferences( true );	
		parser.setCreateAST( true );
		parser.setCreateInspector( true );
	}
	
	public void closeParser( NesCEditor editor, boolean successful, INesCParser parser ){
		if( successful ){
			references = parser.getReferences();
			ast = parser.getAST();
			inspector = parser.getInspector();
		}
	}
	
	@Override
	public String getHoverInfo( ITextViewer textViewer, IRegion hoverRegion ){
		return String.valueOf( getHoverInfo2( textViewer, hoverRegion ) );
	}
	
	@Override
	protected boolean isIncluded( Annotation annotation ){
		return configuration.isShownInText( annotation );
	}

	@SuppressWarnings("unchecked")	
	public Object getHoverInfo2( ITextViewer textViewer, final IRegion hoverRegion ){
		// search for an annotation
		IAnnotationModel model= editor.getAnnotationModel();
		if( model != null ){
			Iterator<Annotation> e= model.getAnnotationIterator();
			while( e.hasNext() ){
				Annotation annotation = e.next();
				if( isIncluded( annotation )){
					Position p = model.getPosition( annotation );
					if( p != null && p.overlapsWith( hoverRegion.getOffset(), hoverRegion.getLength()) ){
						if( annotation instanceof SpellingAnnotation ){
							SpellingAnnotation spelling = (SpellingAnnotation)annotation;
							return new SpellingQuickFixInformation( spelling, editor );
						}
						else if( annotation instanceof MarkerAnnotation ){
							return getFixInformation( (MarkerAnnotation)annotation );
						}
						else if( annotation.getText() != null && annotation.getText().length() > 0 ) {
							return annotation;
						}
					}
				}
			}
		}

		// search for a model-node encompassing the element
		IASTModelNode node = nodeAt( hoverRegion );
		if( node != null && node.getDocumentation() != null )
			return node;
		
		// search for a reference
		IASTModelNode reference = referenceAt( hoverRegion );
		if( reference != null && reference.getDocumentation() != null )
			return reference;
		
		// ask the ast (if there is any)
		if( ast != null ){
			IDocumentMap documentMap = new NesCDocumentMap( editor.getDocument() );
			IDocumentRegion location = new DocumentRegion( editor.getEditorSourceViewer(), hoverRegion, documentMap );
			IHoverInformation information = ast.getHoverInformation( location );
			if( information != null )
				return information;
		}
		
		// try finding a documentation reference
		if( node != null ){
			IASTModelNode comment = referencedDocumentation( node );
			if( comment != null )
				return comment;
		}
		
		if( node != null )
			return node;
		
		return reference;
	}
	
	private IASTModelNode nodeAt( final IRegion hoverRegion ){
		IASTModel astModel = editor.getASTModel();
		if( astModel != null ){
			IASTModelNode[] nodes = astModel.getNodes( new IASTModelNodeFilter(){
				public boolean include( IASTModelNode node ){
					TagSet tags = node.getTags();
					if( tags == null )
						return false;
					if( !tags.contains( Tag.IDENTIFIABLE ))
						return false;
					
					IFileRegion region = node.getRegion();
					if( region == null )
						return false;
					if( region.getOffset() + region.getLength() < hoverRegion.getOffset() )
						return false;
					if( region.getOffset() > hoverRegion.getOffset() + hoverRegion.getLength() )
						return false;
					return true;
				}
			});
			if( nodes.length > 0 ){
				int min = Integer.MAX_VALUE;
				IASTModelNode best = null;
				for( IASTModelNode node : nodes ){
					int length = node.getRegion().getLength();
					if( length < min ){
						min = length;
						best = node;
					}
				}
				return best;
			}
		}
		return null;
	}
	
	private IASTModelNode referenceAt( IRegion hoverRegion ){
		if( references != null ){
			int offset = hoverRegion.getOffset();
			int length = hoverRegion.getLength();
			IASTReference best = null;
			
			for( IASTReference reference : references ){
				IFileRegion source = reference.getSource();
				if( source.getOffset() <= offset && source.getOffset() + source.getLength() >= offset+length ){
					if( best == null || best.getSource().getLength() > source.getLength() ){
						best = reference;
					}
				}
			}
			
			if( best != null ){
				IASTModelNode node = getNode( best );
				if( node != null )
					return node;
			}
		}
		return null;
	}
	
	private IQuickFixInformation getFixInformation( MarkerAnnotation annotation ){
		IMarker marker = annotation.getMarker();
		IMarkerHelpRegistry registry = IDE.getMarkerHelpRegistry();
		IMarkerResolution[] resolutions = registry.getResolutions( marker );
		return new MarkerQuickFixInformation( editor, annotation, marker, resolutions );
	}
	
	private IASTModelNode getNode( final IASTReference reference ){
		IASTModelNode node = null;
		
		IASTModel model = editor.getASTModel();
		if( model != null ){
			node = model.getNode( reference.getTarget() );
		}
		
		if( node == null ){
			ProjectTOS project = editor.getProjectTOS();
			if( project != null ){
				final ProjectModel projectModel = project.getModel();
				class Find extends PublicJob{
					public IASTModelNode node;
					
					public Find(){
						super( "Find reference" );
					}
					@Override
					public IStatus run( IProgressMonitor monitor ){
						node = projectModel.getNode( reference.getTarget(), monitor );
						return Status.OK_STATUS;
					}
				}
				
				Find find = new Find();
				projectModel.runJob( find, null );
				node = find.node;
			}
		}
		
		if( node == null )
			return null;
		
		return node;
	}
	
	/**
	 * Searches referenced documentation of <code>node</code>
	 * @param node the node whose documentation is searched
	 * @return a node which should have documentation, if nothing found: <code>node</code>
	 */
	private IASTModelNode referencedDocumentation( IASTModelNode node ){
		INesCDocComment comment = node.getDocumentation();
		if( comment != null )
			return null;
		
		if( inspector == null )
			return null;
		
		INesCNode nesc = inspector.getNode( node );
		if( nesc == null )
			return null;
		
		INesCNode[] references = nesc.getReferences( INesCNode.DOCUMENTATION_REFERENCE, inspector );
		if( references == null )
			return null;
		
		List<IASTModelNode> nodes = new ArrayList<IASTModelNode>();
		for( INesCNode reference : references ){
			IASTModelNode referenced = reference.asNode();
			if( referenced != null ){
				nodes.add( referenced );
				comment = referenced.getDocumentation();
				if( comment != null )
					return referenced;
			}
		}
		
		for( IASTModelNode referenced : nodes ){
			IASTModelNode check = referencedDocumentation( referenced );
			if( check.getDocumentation() != null  )
				return check;
		}
		
		return null;
	}
	
	public IInformationControlCreator getHoverControlCreator(){
		if( creator == null )
			creator = new NesCInformationControlCreator();
		
		return creator;
	}
	
	private static class NesCInformationControlCreator implements IInformationControlCreator{
		public IInformationControl createInformationControl( Shell parent ){
			return new NesCInformationControl( parent, this );
		}
	}
}
