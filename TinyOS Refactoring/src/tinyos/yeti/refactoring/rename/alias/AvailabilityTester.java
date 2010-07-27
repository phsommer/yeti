package tinyos.yeti.refactoring.rename.alias;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.utilities.ASTUtil4Aliases;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		ASTUtil4Aliases astUtil4Aliases=new ASTUtil4Aliases();
		return astUtil4Aliases.isAlias(selectedIdentifier);
	}
	


}
