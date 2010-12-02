package tinyos.yeti.refactoring.entities.component.alias.introduce;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ConfigurationAstAnalyzer;
import tinyos.yeti.refactoring.entities.component.rename.ComponentSelectionIdentifier;
import tinyos.yeti.refactoring.utilities.ASTUtil;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		AstAnalyzerFactory analyzerFacotry=new AstAnalyzerFactory(selectedIdentifier);
		ComponentSelectionIdentifier selectionIdentifier=new ComponentSelectionIdentifier(selectedIdentifier,analyzerFacotry);
		if(!selectionIdentifier.isComponentDeclaration()){
			return false;
		}
		ASTUtil astUtil=new ASTUtil();
		ConfigurationAstAnalyzer configurationAnalyzer=analyzerFacotry.getConfigurationAnalyzer();
		//If the component is alredy aliased, the refactoring is not available. If it is not already aliased the local2global name map contains the identifier itself in its keyset.
		return astUtil.containsIdentifierInstance(selectedIdentifier, configurationAnalyzer.getComponentLocalName2ComponentGlobalName().keySet());	
	}
	


}
