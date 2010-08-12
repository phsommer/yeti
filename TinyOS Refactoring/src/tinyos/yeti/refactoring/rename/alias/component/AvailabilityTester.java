package tinyos.yeti.refactoring.rename.alias.component;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.rename.alias.AliasSelectionIdentifier;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		AliasSelectionIdentifier selectionIdentifier=new AliasSelectionIdentifier(selectedIdentifier);
		return selectionIdentifier.isComponentAlias();
	}
	


}
