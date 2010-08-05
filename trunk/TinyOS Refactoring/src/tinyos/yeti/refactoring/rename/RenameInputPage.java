package tinyos.yeti.refactoring.rename;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RenameInputPage extends UserInputWizardPage {

	private RenameInfo info;
	private boolean newNameSet=false;
	
	public RenameInputPage(RenameInfo info) {
		super(info.getInputPageName());
		this.info = info;
	}
	
	/**
	 * Checks if the user has already set the new name for the entity.
	 * @return
	 */
	public boolean isNewNameSet() {
		return newNameSet;
	}

	@Override
	public void createControl(Composite parent) {

		Composite root = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(1, false);
		root.setLayout(layout);

		setControl(root);
		initializeDialogUnits(root);
		Dialog.applyDialogFont(root);
		Label lblNewName = new Label(root, SWT.NONE);
		lblNewName.setText(info.getNewNameFieldLabel());
		lblNewName.forceFocus();
		
		final Text newNameTextField = new Text(root, SWT.NONE);
		
		newNameTextField.setText( info.getOldName() );
	    newNameTextField.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
	    newNameTextField.selectAll();
	    newNameTextField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
		        info.setNewName( newNameTextField.getText() );
		        newNameSet=true;
			}
		});
	}

}
