package tinyos.yeti.refactoring.renameLocalVariable;

import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

import tinyos.yeti.editors.NesCEditor;

public class RenameLocalVariableDelegateCommonWork {
	public void doWork(NesCEditor editor){
		String oldName="";
		RenameLocalVariableInfo info = new RenameLocalVariableInfo(oldName);
		info.setEditor(editor);
		RenameLocalVariableProcessor processor = new RenameLocalVariableProcessor(info);
		RenameLocalVariableRefactoring refactoring = new RenameLocalVariableRefactoring(processor);
		RenameLocalVariableWizard wizard = new RenameLocalVariableWizard(refactoring,info);
		RefactoringWizardOpenOperation wizardStarter = new RefactoringWizardOpenOperation(
				wizard);

		try {
			wizardStarter.run(editor.getSite().getShell(), "Rename Local Variable");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
