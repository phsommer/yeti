package tinyos.yeti.refactoring.rename.local.functionparameter;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables();
		return astUtil4Variables.isFunctionParameter(selectedIdentifier);
	}
	
}
