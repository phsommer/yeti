package tinyos.yeti.refactoring.abstractrefactoring.rename;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.refactoring.abstractrefactoring.rename.InputValidation.CNameDifference;

public class RenameInputPage extends UserInputWizardPage {

	private RenameInfo info;
	
	private boolean foundButtons;
	private Button previewButton;
	private Button okButton;
	private Button cancelButton;
	
	private Label newNameLabel; 
	private Text newNameTextField;
	private Label messageLabel; 
	
	public RenameInputPage(RenameInfo info) {
		super(info.getInputPageName());
		this.info = info;
	}

	@Override
	public void createControl(Composite parent) {
		tryToFindControlButtons(parent);

		Composite root = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(1, false);
		root.setLayout(layout);

		setControl(root);
		initializeDialogUnits(root);
		Dialog.applyDialogFont(root);
		newNameLabel = new Label(root, SWT.NONE);
		newNameLabel.setText(info.getNewNameFieldLabel());
		
		newNameTextField = new Text(root, SWT.NONE);
		newNameTextField.setText( info.getOldName() );
	    newNameTextField.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
	    newNameTextField.selectAll();
	    newNameTextField.addModifyListener(new OptionalInputValidationModifyListener());
	    newNameTextField.forceFocus();
	    
	    messageLabel=new Label(root,SWT.NONE);
	    StringBuffer buffer=new StringBuffer();
	    for(int i=0;i<CNameDifference.getLongestUserMessage().length()*1.5;++i){
	    	buffer.append(" ");
	    }
	    messageLabel.setText(buffer.toString());
	}
	
	private void setUserMessage(String message){
		messageLabel.setText(message);
	}
	
	private void clearUserMessage(){
		setUserMessage("");
	}

	/**
	 * Tries to find the buttons which enables the user to proceed after entering a new name.
	 * If we are able to find the ok and the preview button, we can do input validation bevor the user selects any button.
	 * If we dont find the buttons, we have to do inputvalidation in the processor itself.
	 * Sets instance variable foundButtons to the appropriate value.
	 */
	private void tryToFindControlButtons(Composite parent){
		try {
			parent=parent.getParent();
			org.eclipse.swt.widgets.Control[] controls=parent.getChildren();
			Composite urChild=null;
			for(int i=0;i<controls.length;++i){
				if(controls[i] instanceof Composite){
					urChild=(Composite)controls[i];
				}
			}
			if(urChild!=null){
				controls=urChild.getChildren();
				if(controls.length==3){
					previewButton=(Button)controls[0];
					okButton=(Button)controls[1];
					cancelButton=(Button)controls[2];
					if(previewButton.getText().equals("Previe&w >")
						&&okButton.getText().equals("OK")
						&&cancelButton.getText().equals("Cancel")
						){
						foundButtons=true;
					}
				}
			}
		} catch (Exception e) {
			return;
		}
	}


	/**
	 * If we have access to the buttons of the new name input GUI, this class will enable or disable the buttons according to the state of the input.
	 * If we dont have access to the buttons this class purely propagates a new name set by the user to the rename info.
	 * @author Max Urech
	 *
	 */
	private class OptionalInputValidationModifyListener implements ModifyListener{
		
		private final InputValidation validation;
		
		public OptionalInputValidationModifyListener() {
			super();
			if(foundButtons){
				validation=new InputValidation();
			}else{
				validation=null;
			}
		}
		
		@Override
		public void modifyText(ModifyEvent e) {
			String newName=newNameTextField.getText();
	        if(foundButtons){
	        	boolean validInput=validation.isCName(newName);
	        	System.err.println("valid? "+validInput);
	        	okButton.setEnabled(validInput);
	        	previewButton.setEnabled(validInput);
	        	if(validInput){
	        		clearUserMessage();
	        	}else{
	        		CNameDifference difference=validation.decideCNameDifference(newName);
	        		if(difference!=null){
	        			setUserMessage(difference.getUserMessage());
	        		}else{
	        			setUserMessage("Invalid input.");
	        		}
	        	}
	        }
	        info.setNewName(newName );
		}
		
		
	}

}


