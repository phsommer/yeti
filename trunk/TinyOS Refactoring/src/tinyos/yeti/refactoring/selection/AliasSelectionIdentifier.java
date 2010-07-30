package tinyos.yeti.refactoring.selection;

import java.util.Collection;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.utilities.DebugUtil;

public class AliasSelectionIdentifier extends SelectionIdentifier{
	
	/**
	 * @see SelectionIdentifier(Identifier identifier)
	 * @param identifier
	 */
	public AliasSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}
	
	/**
	 * @see SelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory)
	 */
	public AliasSelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory){
		super(identifier,analyzerFactory);
	}

	/**
	 * Checks if the given identifier is part of an AST node associated to an NesC alias like a component alias or a interface alias, which are introduces with the "as" keyword.
	 * @param identifier
	 * @return
	 */
	public boolean isAlias(Identifier identifier){
		if(!analyzerFactory.hasComponentAnalyzerCreated()){
			return false;
		}
		return isComponentAlias()
			||isInterfaceAlias();
	}
	
	/**
	 * Checks if the given identifier is an Alias for a component in the implementation of a nesc configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAlias(){
		return isComponentAliasingInComponentsStatement()
			||isComponentAliasingInComponentWiring();
	}
	
	/**
	 * Checks if the given identifier is an Alias for an interface in the given component.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAlias(){
		return isInterfaceAliasingInSpecification()
		||isInterfaceAliasInNescComponentWiring()
		||isInterfaceAliasInNescFunction();
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC component alias in a NesC "components" statement in a implementation of a NesC configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAliasingInComponentsStatement(){
		if(!analyzerFactory.hasConfigurationAnalyzerCreated()){
			return false;
		}
		Collection<Identifier> componentAliases=configurationAnalyzer.getComponentAliasIdentifiers();
		return componentAliases.contains(identifier);
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC component alias in a NesC component wiring in a implementation of a NesC configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAliasingInComponentWiring(){
		if(!analyzerFactory.hasConfigurationAnalyzerCreated()){
			return false;
		}
		Collection<Identifier> componentWirings=configurationAnalyzer.getWiringComponentPartIdentifiers();
		DebugUtil.immediatePrint("componentWirings: "+componentWirings.size());
		for(Identifier id:componentWirings){
			DebugUtil.immediatePrint(id.getName());
		}
		return containsIdentifierInstance(componentWirings);
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC interface alias in the specification of a module/configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAliasingInSpecification(){
		if(!analyzerFactory.hasComponentAnalyzerCreated()){
			return false;
		}
		Identifier alias=componentAnalyzer.getAliasIdentifier4InterfaceAliasName(identifier.getName());
		return alias==identifier;
	}

	/**
	 * Checks if the given identifier is an Alias for a interface in the implementation of a nesc module in a call/event statement.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAliasInNescFunction() {
		if(!analyzerFactory.hasModuleAnalyzerCreated()){
			return false;
		}
		Collection<Identifier> identifiers=moduleAnalyzer.getNesCFunctionImplementationInterfaceIdentifiers();
		if(!containsIdentifierInstance(identifiers)){
			return false;
		}
		boolean val=moduleAnalyzer.isDefinedInterfaceAliasName(identifier.getName());
		return val;
	}
	
	/**
	 * Checks if the given identifier is an Alias for a interface in a NesC component wiring in the implementation of a NesC Configuration implementation.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAliasInNescComponentWiring(){
		if(!analyzerFactory.hasConfigurationAnalyzerCreated()){
			return false;
		}
		boolean val=containsIdentifierInstance(configurationAnalyzer.getWiringSpecificationPartIdentifiers());
		return val;
	}
}
