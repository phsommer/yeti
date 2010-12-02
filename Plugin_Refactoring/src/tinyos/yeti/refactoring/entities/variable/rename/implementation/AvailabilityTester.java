package tinyos.yeti.refactoring.entities.variable.rename.implementation;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.entities.variable.rename.global.VariableSelectionIdentifier;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		VariableSelectionIdentifier selectionIdentifier=new VariableSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isImplementationLocalVariable();
	}
	
}