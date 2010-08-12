package tinyos.yeti.refactoring.rename.implementation.variable;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.selection.ImplementationLocalVariableSelectionIdentifier;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		ImplementationLocalVariableSelectionIdentifier selectionIdentifier=new ImplementationLocalVariableSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isImplementationLocalVariable();
//		ASTUtil4Variables asttil4Variables=new ASTUtil4Variables();
//		return asttil4Variables.isImplementationLocalVariable(selectedIdentifier);
	}
	
}