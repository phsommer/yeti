package tinyos.yeti.refactoring.rename;

import tinyos.yeti.editors.NesCEditor;

public class RenameInfo {

	private String oldName;
	private String newName = null;
	private String inputPageName = "Getting the new Variable name.";
	private String newNameFieldLabel = "New Name:";
	private String inputWizardName = "Rename";
	private NesCEditor editor;
	
	public RenameInfo(String oldName, NesCEditor editor) {
		this.oldName=oldName;
		this.newName = oldName; // Is a good default, if it is not done and the User does not change the Name, a Nullpoiter Exception occures
		this.editor = editor;
	}
	
	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public String getOldName() {
		return oldName;
	}

	public void setInputPageName(String inputPageName) {
		this.inputPageName = inputPageName;
	}

	public String getInputPageName() {
		return inputPageName;
	}

	public void setNewNameFieldLabel(String newNameFieldLabel) {
		this.newNameFieldLabel = newNameFieldLabel;
	}

	public String getNewNameFieldLabel() {
		return newNameFieldLabel;
	}

	public void setInputWizardName(String inputWizardName) {
		this.inputWizardName = inputWizardName;
	}

	public String getInputWizardName() {
		return inputWizardName;
	}

	public void setEditor(NesCEditor editor) {
		this.editor = editor;
	}

	public NesCEditor getEditor() {
		return editor;
	}
	
	

}
