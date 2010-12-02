package tinyos.yeti.refactoring.entities.interfaces.alias.rename;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.entities.interfaces.rename.InterfaceSelectionIdentifier;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		InterfaceSelectionIdentifier selectionIdentifier=new InterfaceSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isInterfaceAlias();
	}
	


}
