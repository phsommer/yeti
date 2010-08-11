package tinyos.yeti.refactoring.rename.global.field;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.selection.GlobalFunctionSelectionIdentifier;

public class GlobalFunctionAvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		GlobalFunctionSelectionIdentifier selectionIdentifier=new GlobalFunctionSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isGlobalFunction();
//		ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions();
//		return astUtil4Functions.isGlobalFunction(selectedIdentifier);
	}
	
}
