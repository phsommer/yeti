package tinyos.yeti.refactoring.renameLocalVariable;

import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.refactoring.DefaultRefactoringWizard;
import tinyos.yeti.refactoring.rename.RenameInfo;

public class RenameLocalVariableDelegateCommonWork {
	public void doWork(NesCEditor editor) {
		String oldName = "";
		RenameInfo info = new RenameLocalVariableInfo(oldName,editor);
		RenameProcessor processor = new RenameLocalVariableProcessor(info);
		RenameRefactoring refactoring = new RenameRefactoring(processor);
		DefaultRefactoringWizard wizard = new DefaultRefactoringWizard(
				refactoring, 
				new RenameLocalVariableInputPage(info), 
				info);
		RefactoringWizardOpenOperation wizardStarter = new RefactoringWizardOpenOperation(wizard);

		try {
			wizardStarter.run(editor.getSite().getShell(),
					"Rename Local Variable");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
