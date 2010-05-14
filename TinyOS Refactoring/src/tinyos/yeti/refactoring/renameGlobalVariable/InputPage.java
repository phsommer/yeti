package tinyos.yeti.refactoring.renameGlobalVariable;

import tinyos.yeti.refactoring.rename.RenameInputPage;

public class InputPage extends RenameInputPage{

	public InputPage(Info info) {
		super(info);
		info.setNewNameFieldLabel("New Variable Name:");
	}

}
