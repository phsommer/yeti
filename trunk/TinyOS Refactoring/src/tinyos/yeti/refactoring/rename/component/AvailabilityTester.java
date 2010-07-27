package tinyos.yeti.refactoring.rename.component;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.utilities.ASTUtil4Components;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		ASTUtil4Components astUtil4Components=new ASTUtil4Components();
		return astUtil4Components.isComponent(selectedIdentifier);
	}
	


}
