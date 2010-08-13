package tinyos.yeti.refactoring.entities.function.rename.nesc;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameAvailabilityTester;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		NescFunctionSelectionIdentifier selectionIdentifier=new NescFunctionSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isNescFunction(selectedIdentifier);
	}
	


}
