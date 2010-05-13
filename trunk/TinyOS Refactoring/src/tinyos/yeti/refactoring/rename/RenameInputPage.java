package tinyos.yeti.refactoring.rename;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.refactoring.rename.RenameInfo;

public class RenameInputPage extends UserInputWizardPage {

	RenameInfo info;
	
	public RenameInputPage(RenameInfo info) {
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
		Label lblNewName = new Label(root, SWT.NONE);
		lblNewName.setText(info.getNewNameFieldLabel());
		
		final Text newNameTextField = new Text(root, SWT.NONE);
		
		newNameTextField.setText( info.getOldName() );
	    newNameTextField.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
	    newNameTextField.selectAll();
	    newNameTextField.addKeyListener( new KeyAdapter() {
	      public void keyReleased( final KeyEvent e ) {
	        info.setNewName( newNameTextField.getText() );
	      }
	    } );

	}

}
