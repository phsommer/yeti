package tinyos.yeti.refactoring.entities.interfaces.rename;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.rename.SelectionIdentifier;
import tinyos.yeti.refactoring.rename.alias.AliasSelectionIdentifier;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;

public class InterfaceSelectionIdentifier extends SelectionIdentifier{
	
	private ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions(astUtil);
	
	/**
	 * @see SelectionIdentifier
	 * @param identifier
	 */
	public InterfaceSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}

	/**
	 * @see SelectionIdentifier
	 * @param identifier
	 */
	public InterfaceSelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory) {
		super(identifier, analyzerFactory);
	}
	
	/**
	 * Checks if the given identifier is part of an AST node associated to an interface.
	 * If the selected Identifier is an alias in a nesc function definition, this function will return false.
	 * @param identifier
	 * @return
	 */
	public boolean isInterface(){
		return isInterfaceDeclaration()
			||isInterfaceDefinition()
			||isInterfaceImplementationAndNoAlias()
			||isComponentWiringInterfacePart()
			||isInterfacePartInNesCFunctionCallAndNoAlias();
	}

	/**
	 * Checks if the given identifier is part of an interface definition,
	 * which is the interface identifier in the file which defines the interface.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceDefinition(){
		if(!factory4Selection.hasInterfaceAnalyzerCreated()){
			return false;
		}
		return interfaceAnalyzer.getEntityIdentifier()==identifier;
	}
	
	/**
	 * Checks if the given identifier is part of a interface reference in a provide or use statement of a component, which is a module or configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceDeclaration(){
		if(!factory4Selection.hasComponentAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,componentAnalyzer.getReferencedInterfaceIdentifiers());
	}
	
	/**
	 * Checks if the given identifier is part of a interface reference in a event or call implementation of a module.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceImplementation(){
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,moduleAnalyzer.getNesCFunctionImplementationInterfaceIdentifiers());
	}
	
	/**
	 * Checks if the given identifier is part of a interface reference in a event or call implementation of a module and also that it is no interface alias.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceImplementationAndNoAlias(){
		if(!isInterfaceImplementation()){
			return false;
		}
		AliasSelectionIdentifier aliasSelection=new AliasSelectionIdentifier(identifier,factory4Selection);
		return !aliasSelection.isInterfaceAliasInNescFunction();
	}
	
	/**
	 * Checks if the given identifier is the interface part of a nesc function call.
	 * And that the identifier is really the name of a interface and not an local defined alias.
	 * @return
	 */
	public boolean isInterfacePartInNesCFunctionCallAndNoAlias() {
		if(!astUtil4Functions.isInterfacePartInNesCFunctionCall(identifier)){
			return false;
		}
		AliasSelectionIdentifier aliasSelection=new AliasSelectionIdentifier(identifier,factory4Selection);
		return !aliasSelection.isInterfaceAliasInNescFunctionCall();
	}
	
	/**
	 * Checks if the given identifier is part of a interface reference in a module/component wiring.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentWiringInterfacePart(){
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,configurationAnalyzer.getWiringInterfacePartIdentifiers());
	}
	

}
