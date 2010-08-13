package tinyos.yeti.refactoring.rename.function.global;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;

public class GlobalFunctionAvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		FunctionSelectionIdentifier selectionIdentifier=new FunctionSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isGlobalFunction();
//		ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions();
//		return astUtil4Functions.isGlobalFunction(selectedIdentifier);
	}
	
}
