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
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.parser.IASTModelElement;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

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
		NesCEditor editor = getEditor();
		for(IASTModelElement element:editor.getSelectedElements()){
			if(element instanceof Identifier){
				Identifier identifier = (Identifier) element;
				oldName=identifier.getName();
			} else {
				// TODO: This happens if not an identifier is selected. 
				return;
			}
		}

		RenameLocalVariableInfo info = new RenameLocalVariableInfo(oldName);
		RenameLocalVariableProcessor processor = new RenameLocalVariableProcessor(editor);
		RenameLocalVariableRefactoring refactoring = new RenameLocalVariableRefactoring(processor);
		RenameLocalVariableWizard wizard = new RenameLocalVariableWizard(refactoring,info);
		RefactoringWizardOpenOperation wizardStarter = new RefactoringWizardOpenOperation(
				wizard);

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;

	}

	private NesCEditor getEditor() {
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null) {
			return null;
		}

		IEditorPart editorTmp = activePage.getActiveEditor();
		if (editorTmp == null || !(editorTmp instanceof MultiPageNesCEditor)) {
			throw new RuntimeException("----- Es war kein MultiPageNesCEditor");
		}
		ITextEditor editor = (ITextEditor) editorTmp;

		if (editor instanceof NesCEditor) {
			return (NesCEditor) editor;
		} else {
			throw new IllegalStateException(
					"Rename Local Varibel Refactoring is only allowed if a NesC Editor is in use.");
		}
	}

}
