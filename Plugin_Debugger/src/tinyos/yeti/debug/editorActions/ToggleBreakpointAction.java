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
package tinyos.yeti.debug.editorActions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import tinyos.yeti.debug.ITinyOSDebugConstants;
import tinyos.yeti.debug.TinyOSDebugPlugin;

public class ToggleBreakpointAction extends Action {
	
	static class EmptySelection implements ISelection {

		public boolean isEmpty() {
			return true;
		}
	}

	private static final ISelection EMPTY_SELECTION = new EmptySelection();

	public ToggleBreakpointAction(IWorkbenchPart part, IVerticalRulerInfo ruler, IToggleBreakpointsTarget target) {
		super();
		m_rulerInfo = ruler;
		m_workbenchPart = part;
		m_target = target;
		setId(ITinyOSDebugConstants.ACTION_TOGGLE_BREAKPOINT);
	}

	/**
	 * Disposes this action
	 */
	public void dispose() {
		m_workbenchPart = null;
		m_rulerInfo = null;
		m_target = null;
	}

	public void run() {
		try {
			m_target.toggleLineBreakpoints(getWorkbenchPart(),
					getTargetSelection());
		} catch (CoreException e) {
			TinyOSDebugPlugin.getDefault().log("Exception while toggling breakpoint.",e);
		}

	}

	public IVerticalRulerInfo getRulerInfo() {
		return m_rulerInfo;
	}

	public IWorkbenchPart getWorkbenchPart() {
		return m_workbenchPart;
	}

	/**
	 * Returns the current selection in the active part, possibly and empty
	 * selection, but never <code>null</code>.
	 * 
	 * @return the selection in the active part, possibly empty
	 */
	private ISelection getTargetSelection() {
		IDocument doc = getDocument();
		if (doc != null) {
			int line = m_rulerInfo.getLineOfLastMouseButtonActivity();
			try {
				IRegion region = doc.getLineInformation(line);
				return new TextSelection(doc, region.getOffset(), region
						.getLength());
			} catch (BadLocationException e) {
				TinyOSDebugPlugin.getDefault().log("Bad location!",e);
			}
		}
		return EMPTY_SELECTION;
	}

	private IDocument getDocument() {
		if (m_workbenchPart instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) m_workbenchPart;
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if (provider != null)
				return provider.getDocument(textEditor.getEditorInput());
		}
		return null;
	}

	private IVerticalRulerInfo m_rulerInfo;
	private IWorkbenchPart m_workbenchPart;
	private IToggleBreakpointsTarget m_target;
}