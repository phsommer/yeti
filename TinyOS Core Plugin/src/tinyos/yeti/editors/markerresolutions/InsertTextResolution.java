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
package tinyos.yeti.editors.markerresolutions;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.preferences.PreferenceConstants;
import tinyos.yeti.utility.IOConversion;

public class InsertTextResolution implements IMarkerResolution {
	String text;

	int offset = -1;

	String skeleton;

	public InsertTextResolution(String text, int offset, String skeleton) {
		this.text = text;
		this.offset = offset;
		this.skeleton = skeleton;
	}

	public String getLabel() {
		return text;
	}

	public void run(final IMarker marker) {
		IFile f = (IFile) marker.getResource();
		boolean dirty = false;

		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();

		IEditorReference parts[] = page.getEditorReferences();
		for (int i = 0; i < parts.length; i++) {
			IEditorInput iei;
			try {
				iei = parts[i].getEditorInput();

				if (iei instanceof FileEditorInput) {
					if (((FileEditorInput) iei).getFile().equals(f)) {
						// found dirty open file
						IEditorPart editor = parts[i].getEditor(false);

						IDocumentProvider dp = null;

						if (editor instanceof MultiPageNesCEditor) {
							dp = ((MultiPageNesCEditor) editor)
									.getDocumentProvider();
						} else if (editor instanceof NesCEditor) {
							dp = ((NesCEditor) editor).getDocumentProvider();
						}
						IDocument doc = dp.getDocument(editor.getEditorInput());
						dp.aboutToChange(doc);

						try {
							doc.replace(offset, 0, applyFormatting(skeleton));
							dp.changed(doc);
//							dp.notifyAll();

						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						dirty = true;
						break;
					}
				}
			} catch (PartInitException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		if (!dirty) {
			String content;
			try {
				content = IOConversion.getStringFromStream(f.getContents(true));
				content = content.substring(0, offset)
						+ applyFormatting(skeleton)
						+ content.substring(offset + 1);
				f.setContents(new ByteArrayInputStream(content.getBytes()),
						true, true, null);
				f.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	

	/*
	 * @param s, the string to be formatted
	 * @return s
	 */
	private String applyFormatting(String s) {
		
		boolean tabs = TinyOSPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.USE_TABS);
		if (!tabs) {
			return s.replaceAll("\t", PreferenceConstants.spacesPerTab());
		} else {
			return s;
		}

	}
}