package tinyos.yeti.refactoring.renameLocalVariable;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;

public class RenameLocalVariableDelegate implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	private ISelection selection;

	@Override
	public void dispose() {
		window = null;

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;

	}
	
	@Override
	public void run(IAction action) {
		RenameLocalVariableDelegateCommonWork commonWork=new RenameLocalVariableDelegateCommonWork();
		commonWork.doWork(getEditor());
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;

	}

	private NesCEditor getEditor() {
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null) {
			throw new RuntimeException("----- No active Page!");
		}

		IEditorPart editorTmp = activePage.getActiveEditor();
		if (editorTmp == null || !(editorTmp instanceof MultiPageNesCEditor)) {
			throw new RuntimeException("----- Was not MultiPageNesCEditor");
		}else{
			MultiPageNesCEditor multi=(MultiPageNesCEditor) editorTmp;
			return multi.getNesCEditor();
		} 
	}

}
