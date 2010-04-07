package tinyos.yeti.refactoring.renameLocalVariable;

public class RenameLocalVariableInfo {
	private String oldName;
	private String newName=null;
	private String inputPageName="Getting the new Variable name.";
	
	public RenameLocalVariableInfo(String oldName) {
		this.oldName = oldName;
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
	
	

}
