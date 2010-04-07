package tinyos.yeti.refactoring.renameLocalVariable;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;



public class RenameLocalVariableWizard extends RefactoringWizard {

	private RenameLocalVariableInfo info;

	public RenameLocalVariableWizard(Refactoring refactoring, RenameLocalVariableInfo info) {
		super(refactoring, RefactoringWizard.DIALOG_BASED_USER_INTERFACE);
		this.info=info;
		
	}
	
	@Override
	protected void addUserInputPages() {
		setDefaultPageTitle("Rename local Variable");
		addPage(new RenameLocalVariableInputPage(info));

	}

}
