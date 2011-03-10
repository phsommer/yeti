/**
 * 
 */
package tinyos.yeti.debug.editorActions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

import tinyos.yeti.debug.CDTAbstractionLayer.CDTBreakpointToggleTarget;

/**
 * @author dcg
 *
 */
public class BreakpointRulerDelegate extends AbstractRulerActionDelegate {

	private ToggleBreakpointAction m_action;
	
	public BreakpointRulerDelegate(){
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if ( m_action != null ) {
			m_action.dispose();
			m_action = null;
		}
		super.setActiveEditor( action, targetEditor );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
	}

	public IAction createAction(ITextEditor editor,
			IVerticalRulerInfo rulerInfo) {
		m_action = new ToggleBreakpointAction(editor, rulerInfo, new CDTBreakpointToggleTarget());
		return m_action;
	}
}
