package tinyos.yeti.refactoring.renameLocalVariable;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.refactoring.rename.RenameInfo;

public class RenameLocalVariableInfo extends RenameInfo{
	public RenameLocalVariableInfo(String oldName,NesCEditor editor) {
		super(oldName,editor);
		this.setInputWizardName("Rename local Varible");
	}
}
