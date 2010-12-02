package tinyos.yeti.refactoring.entities.function.rename.local;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.entities.function.rename.FunctionSelectionIdentifier;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		FunctionSelectionIdentifier selectionIdentifier=new FunctionSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isImplementationLocalFunction();
//		ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions();
//		return astUtil4Functions.isLocalFunction(selectedIdentifier);
	}
	
}