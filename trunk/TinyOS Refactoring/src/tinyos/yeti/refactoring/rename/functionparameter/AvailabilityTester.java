package tinyos.yeti.refactoring.rename.functionparameter;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		FunctionParameterSelectionIdentifier selectionIdentifier=new FunctionParameterSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isFunctionParameter();
	}
	
}
