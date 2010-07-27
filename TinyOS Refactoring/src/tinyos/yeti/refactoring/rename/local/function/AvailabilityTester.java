package tinyos.yeti.refactoring.rename.local.function;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions();
		return astUtil4Functions.isLocalFunction(selectedIdentifier);
	}
	
}