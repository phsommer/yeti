package tinyos.yeti.refactoring.selection;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;

public class NescFunctionSelectionIdentifier extends SelectionIdentifier {
	
	public NescFunctionSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}

	public NescFunctionSelectionIdentifier(Identifier identifier, AstAnalyzerFactory analyzerFactory) {
		super(identifier, analyzerFactory);
	}
	
	
	/**
	 * Checks if the given identifier is part of an AST node associated to an NesC function like a command or an event.
	 * @param identifier
	 * @return
	 */
	public boolean isNescFunction(Identifier identifier){
		return isFunctionDeclaration()
			||isFunctionDefinition();
	}
	
	
	/**
	 * Checks if the given identifier is the name identifier of a nesc function declaration in a nesc interface ast.
	 * @return
	 */
	public boolean isFunctionDeclaration(){
		if(!factory4Selection.hasInterfaceAnalyzerCreated()){
			return false;
		}
		return containsIdentifierInstance(interfaceAnalyzer.getNesCFunctionIdentifiers());
	}
	
	/**
	 * Checks if the given identifier is the name identifier of a nesc function definition of a interface implementation in a nesc module ast.
	 * @return
	 */
	public boolean isFunctionDefinition(){
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		return containsIdentifierInstance(moduleAnalyzer.getNesCFunctionImplementationFunctionIdentifiers());
	}

}
