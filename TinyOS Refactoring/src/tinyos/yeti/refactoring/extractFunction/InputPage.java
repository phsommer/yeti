package tinyos.yeti.refactoring.extractFunction;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class InputPage extends UserInputWizardPage {

	Info info;
	
	public InputPage(Info info) {
		super(info.getInputPageName());
		this.info = info;
	}

	@Override
	public void createControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(1, false);
		root.setLayout(layout);

		setControl(root);
		initializeDialogUnits(root);
		Dialog.applyDialogFont(root);
		

		
	}

}
