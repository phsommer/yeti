package tinyos.yeti.refactoring.renameLocalVariable;

import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameInputPage;

public class RenameLocalVariableInputPage extends RenameInputPage {

	public RenameLocalVariableInputPage(RenameInfo info) {
		super(info);
		info.setNewNameFieldLabel("New Variable Name:");
	}
}
