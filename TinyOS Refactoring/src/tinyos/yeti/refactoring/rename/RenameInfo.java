package tinyos.yeti.refactoring.rename;

public class RenameInfo {

	private String oldName;
	private String newName = null;
	private String inputPageName = "Getting the new Variable name.";
	private String newNameFieldLabel = "New Name:";
	private String inputWizardName = "Rename";
	
	public RenameInfo(String oldName) {
		this.oldName=oldName;
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
	
	

}
