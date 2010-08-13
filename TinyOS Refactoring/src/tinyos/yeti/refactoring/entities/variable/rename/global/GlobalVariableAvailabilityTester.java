package tinyos.yeti.refactoring.entities.variable.rename.global;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;

public class GlobalVariableAvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		VariableSelectionIdentifier selectionIdentifier=new VariableSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isGlobalVariable();
//		ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables();
//		return astUtil4Variables.isGlobalVariable(selectedIdentifier);
	}
	

}