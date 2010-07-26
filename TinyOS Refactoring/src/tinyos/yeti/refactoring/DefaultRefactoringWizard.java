package tinyos.yeti.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;




public class DefaultRefactoringWizard extends RefactoringWizard {

	UserInputWizardPage inputPage;
	RefactoringInfo info;
	
	public DefaultRefactoringWizard(Refactoring refactoring, UserInputWizardPage inputPage, RefactoringInfo info) {
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
