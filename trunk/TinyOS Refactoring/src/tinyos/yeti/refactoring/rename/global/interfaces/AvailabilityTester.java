package tinyos.yeti.refactoring.rename.global.interfaces;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.utilities.ASTUTil4Interfaces;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		return ASTUTil4Interfaces.isInterface(selectedIdentifier);
	}
	


}
