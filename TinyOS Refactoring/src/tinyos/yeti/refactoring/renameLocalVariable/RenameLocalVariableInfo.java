package tinyos.yeti.refactoring.renameLocalVariable;

import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;

public class RenameLocalVariableInfo {
	private String oldName;
	private String newName="Hans-Peter";
	private String inputPageName="Getting the new Variable name.";
	private MultiPageNesCEditor multiPageEditor;
	
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

	public void setMultiPageEditor(MultiPageNesCEditor multiPageEditor) {
		this.multiPageEditor = multiPageEditor;
	}

	public MultiPageNesCEditor getMultiPageEditor() {
		return multiPageEditor;
	}
	
	public NesCEditor getEditor(){
		return this.multiPageEditor.getNesCEditor();
	}
	

}
