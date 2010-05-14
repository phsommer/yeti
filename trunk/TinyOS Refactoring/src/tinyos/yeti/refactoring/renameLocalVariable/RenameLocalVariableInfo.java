package tinyos.yeti.refactoring.renameLocalVariable;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.refactoring.rename.RenameInfo;

public class RenameLocalVariableInfo extends RenameInfo{
	private NesCEditor editor;

	public void setEditor(NesCEditor editor) {
		this.editor = editor;
	}
	
	public NesCEditor getEditor() {
		return editor;
	}

	public RenameLocalVariableInfo(String oldName) {
		super(oldName);
		this.setInputWizardName("Rename local Varible");
	}
}
