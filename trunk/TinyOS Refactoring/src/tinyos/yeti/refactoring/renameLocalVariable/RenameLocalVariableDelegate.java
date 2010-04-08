package tinyos.yeti.refactoring.renameLocalVariable;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

import tinyos.yeti.editors.MultiPageNesCEditor;

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
		String oldName="";
		MultiPageNesCEditor multiPageEditor = getEditor();
		//NesCEditor editor = multiPageEditor.getNesCEditor();
		
		
		

		RenameLocalVariableInfo info = new RenameLocalVariableInfo(oldName);
		info.setMultiPageEditor(getEditor());
		RenameLocalVariableProcessor processor = new RenameLocalVariableProcessor(info);
		RenameLocalVariableRefactoring refactoring = new RenameLocalVariableRefactoring(processor);
		RenameLocalVariableWizard wizard = new RenameLocalVariableWizard(refactoring,info);
		RefactoringWizardOpenOperation wizardStarter = new RefactoringWizardOpenOperation(
				wizard);

		try {
			wizardStarter.run(multiPageEditor.getSite().getShell(), "Rename Local Variable");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;

	}

	private MultiPageNesCEditor getEditor() {
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null) {
			return null;
		}

		IEditorPart editorTmp = activePage.getActiveEditor();
		if (editorTmp == null || !(editorTmp instanceof MultiPageNesCEditor)) {
			throw new RuntimeException("----- Es war kein MultiPageNesCEditor");
		}
		ITextEditor editor = (ITextEditor) editorTmp;

		if (editor instanceof MultiPageNesCEditor) {
			return (MultiPageNesCEditor) editor;
		} else {
			throw new IllegalStateException(
					"Rename Local Varibel Refactoring is only allowed if a NesC Editor is in use. But "+editor.getClass().getName()+" was in use.");
		}
	}

}
