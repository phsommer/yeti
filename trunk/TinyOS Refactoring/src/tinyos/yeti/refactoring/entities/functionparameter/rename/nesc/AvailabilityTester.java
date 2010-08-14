package tinyos.yeti.refactoring.entities.functionparameter.rename.nesc;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.entities.functionparameter.rename.c.FunctionParameterSelectionIdentifier;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		FunctionParameterSelectionIdentifier selectionIdentifier=new FunctionParameterSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isCFunctionParameter();
	}
	
}
