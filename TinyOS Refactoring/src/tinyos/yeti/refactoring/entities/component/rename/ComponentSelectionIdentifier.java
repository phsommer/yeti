package tinyos.yeti.refactoring.entities.component.rename;

import java.util.Collection;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.abstractrefactoring.rename.SelectionIdentifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;

public class ComponentSelectionIdentifier extends SelectionIdentifier {

	public ComponentSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}

	public ComponentSelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory) {
		super(identifier, analyzerFactory);
	}
	
	/**
	 * Checks if the given identifier is part of an AST node associated to an NesC component like a module or a configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponent(){
		return isComponentDefinition()
			||isComponentDeclaration()
			||isComponentWiringComponentPartNotAliased();
	}

	/**
	 * Checks if the given identifier is the identifier of a NesC Component Definition like a Module or a Configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentDefinition(){
		if(!factory4Selection.hasComponentAnalyzerCreated()){
			return false;
		}
		return componentAnalyzer.getEntityIdentifier()==identifier;
	}
	
	/**
	 * Checks if the given identifier is the component identifier of a NesC configuration' "components" statement.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentDeclaration(){
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			return false;
		}
		Collection<Identifier> identifiers=configurationAnalyzer.getIdentifiersOfReferencedComponents();
		return astUtil.containsIdentifierInstance(identifier,identifiers);
	}
	
	/**
	 * Checks if the given identifier is the component identifier of a NesC configuration wiring  statement, and if it is no alias.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentWiringComponentPartNotAliased(){
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			return false;
		}
		Collection<Identifier> identifiers=configurationAnalyzer.getWiringComponentPartIdentifiers();
		if(!astUtil.containsIdentifierInstance(identifier,identifiers)){
			return false;
		}
		return !isComponentAlias();
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
	 * Checks if the given identifier is the identifier of a NesC component alias in a NesC "components" statement in a implementation of a NesC configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAliasingInComponentsStatement(){
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
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
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			return false;
		}
		Collection<Identifier> componentWirings=configurationAnalyzer.getWiringComponentPartIdentifiers();
		if(!astUtil.containsIdentifierInstance(identifier,componentWirings)){
			return false;
		}
		Collection<Identifier> componentAliases=configurationAnalyzer.getComponentAliasIdentifiers();
		return componentAliases.contains(identifier);

	}
	
	

}
