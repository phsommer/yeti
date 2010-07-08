package tinyos.yeti.refactoring.renameFunction;

import tinyos.yeti.refactoring.rename.RenameInputPage;

public class InputPage extends RenameInputPage{

	public InputPage(Info info) {
		super(info);
		info.setNewNameFieldLabel("New Function Name:");
	}

}
