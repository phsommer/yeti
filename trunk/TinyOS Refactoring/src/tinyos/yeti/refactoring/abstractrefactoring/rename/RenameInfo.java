package tinyos.yeti.refactoring.abstractrefactoring.rename;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.refactoring.RefactoringInfo;

public class RenameInfo extends RefactoringInfo{

	private String oldName;
	private String newName = null;
	private String newNameFieldLabel = "New Name:";
	
	private RenameInputPage inputPage;

	public RenameInfo(String oldName, NesCEditor editor) {
		super(editor,"Rename","Getting the new name.");
		this.oldName = oldName;
		this.newName = oldName; // Is a good default, if it is not done and the User does not change the Name, a Nullpoiter Exception occures
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

	public void setNewNameFieldLabel(String newNameFieldLabel) {
		this.newNameFieldLabel = newNameFieldLabel;
	}

	public String getNewNameFieldLabel() {
		return newNameFieldLabel;
	}
	
	public RenameInputPage getInputPage() {
		return inputPage;
	}

	public void setInputPage(RenameInputPage inputPage) {
		this.inputPage = inputPage;
	}
	
	

}
