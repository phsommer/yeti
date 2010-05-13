package tinyos.yeti.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

import tinyos.yeti.refactoring.rename.RenameInfo;



public class DefaultRefactoringWizard extends RefactoringWizard {

	UserInputWizardPage inputPage;
	RenameInfo info;
	
	public DefaultRefactoringWizard(Refactoring refactoring, UserInputWizardPage inputPage, RenameInfo info) {
		super(refactoring, RefactoringWizard.DIALOG_BASED_USER_INTERFACE);
		this.inputPage = inputPage;
		this.info = info;
	}
	
	@Override
	protected void addUserInputPages() {
		setDefaultPageTitle(info.getInputWizardName());
		addPage(inputPage);

	}

}
