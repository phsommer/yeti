package tinyos.yeti.refactoring.entities.functionparameter.rename.c;

import org.eclipse.core.commands.IHandler;

import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameActionHandler;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameProcessor;

public class ActionHandler extends RenameActionHandler implements IHandler {

	@Override
	protected RenameProcessor createProcessor(RenameInfo info) {
		return new Processor(info);
	}

}
