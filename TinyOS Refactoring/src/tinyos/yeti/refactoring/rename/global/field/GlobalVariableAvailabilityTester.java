package tinyos.yeti.refactoring.rename.global.field;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.selection.GlobalVariableSelectionIdentifier;

public class GlobalVariableAvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		GlobalVariableSelectionIdentifier selectionIdentifier=new GlobalVariableSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isGlobalVariable();
//		ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables();
//		return astUtil4Variables.isGlobalVariable(selectedIdentifier);
	}
	

}