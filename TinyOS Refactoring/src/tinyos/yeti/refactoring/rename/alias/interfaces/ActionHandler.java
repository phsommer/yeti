package tinyos.yeti.refactoring.rename.alias.interfaces;

import org.eclipse.core.commands.IHandler;

import tinyos.yeti.refactoring.rename.RenameActionHandler;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;

public class ActionHandler extends RenameActionHandler implements IHandler{

	@Override
	protected RenameProcessor createProcessor(RenameInfo info) {
		return new Processor(info);
	}

}
