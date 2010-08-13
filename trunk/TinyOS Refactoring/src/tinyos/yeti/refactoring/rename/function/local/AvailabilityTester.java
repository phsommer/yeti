package tinyos.yeti.refactoring.rename.function.local;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.entities.function.rename.global.FunctionSelectionIdentifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		FunctionSelectionIdentifier selectionIdentifier=new FunctionSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isImplementationLocalFunction();
//		ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions();
//		return astUtil4Functions.isLocalFunction(selectedIdentifier);
	}
	
}