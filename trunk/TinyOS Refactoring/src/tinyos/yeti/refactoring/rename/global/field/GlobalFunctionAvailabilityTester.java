package tinyos.yeti.refactoring.rename.global.field;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;

public class GlobalFunctionAvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		return ASTUtil4Functions.isGlobalFunction(selectedIdentifier);
	}
	
}
