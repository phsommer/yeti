package tinyos.yeti.refactoring.rename.interfaces;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		InterfaceSelectionIdentifier selectionIdentifier=new InterfaceSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isInterface();
	}
	


}
