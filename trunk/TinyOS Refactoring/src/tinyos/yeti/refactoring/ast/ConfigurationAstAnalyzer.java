package tinyos.yeti.refactoring.ast;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ComponentList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Connection;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Endpoint;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.RefComponent;

public class ConfigurationAstAnalyzer extends ComponentAstAnalyser {

	private ConfigurationDeclarationList implementation;
	private Collection<RefComponent> components;
	private Collection<Identifier> referencedComponents;
	private Collection<String> namesOfReferencedComponents;
	private Collection<Identifier> componentAliases;
	private Collection<Endpoint> wiringEndpoints;
	private Collection<Identifier> wiringComponentPartIdentifiers;
	private Collection<Identifier> wiringSpecificationPartIdentifiers;

	public ConfigurationAstAnalyzer(TranslationUnit root,Identifier componentIdentifier, AccessList specification,ConfigurationDeclarationList implementation) {
		super(root, componentIdentifier, specification);
		this.implementation = implementation;
	}
	
	/**
	 * Gathers all Component declarations which are found in the configuration implementation.
	 * These are the Components which are declared with the NesC "components" statement. 
	 * @return
	 */
	public Collection<RefComponent> getComponentDeclarations(){
		if(components==null){
			components=new LinkedList<RefComponent>();
			Collection<ComponentList> componentLists=astUtil.getChildsOfType(implementation, ComponentList.class);
			for(ComponentList componentList:componentLists){
				components.addAll(astUtil.getChildsOfType(componentList, RefComponent.class));
			}
		}
		return components;
	}
	
	/**
	 * Gathers all identifiers which are found in the configuration implementation in a NesC "components" statement, which reference some NesC Component.
	 * @return
	 */
	public Collection<Identifier> getIdentifiersOfReferencedComponents(){
		if(referencedComponents==null){
			referencedComponents= collectFieldsWithName(getComponentDeclarations(), RefComponent.NAME);
		}
		return referencedComponents;
	}
	
	/**
	 * Gathers all identifier names which are found in the configuration implementation in a NesC "components" statement, which reference some NesC Component.
	 * @return
	 */
	public Collection<String> getNamesOfReferencedComponents(){
		if(namesOfReferencedComponents==null){
			namesOfReferencedComponents=new LinkedList<String>(); 
			for(Identifier identifier:getIdentifiersOfReferencedComponents()){
				namesOfReferencedComponents.add(identifier.getName());
			}
		}
		return namesOfReferencedComponents;
	}
	
	/**
	 * Gathers all Component aliases identifiers which appear in the NesC "components" statements in the implementation
	 * @return
	 */
	public Collection<Identifier> getComponentAliasIdentifiers(){
		if(componentAliases==null){
			Collection<RefComponent> components=getComponentDeclarations();
			componentAliases=collectFieldsWithName(components, RefComponent.RENAME);
		}
		return componentAliases;
	}
	
	/**
	 * Gathers all wiring endpoints of the wirings in the implementation of the configuration which are in a NesC Wiring statement.
	 * @return
	 */
	public Collection<Endpoint> getWiringEndpoints(){
		if(wiringEndpoints==null){
			Collection<Connection> connections=astUtil.getChildsOfType(implementation, Connection.class);
			wiringEndpoints=collectFieldsWithName(connections, Connection.LEFT);
			Collection<Endpoint> rightEndpoints=collectFieldsWithName(connections, Connection.RIGHT);
			wiringEndpoints.addAll(rightEndpoints);
		}
		return wiringEndpoints;
	}
	
	/**
	 * Gathers all component identifier parts of a NesC wiring.
	 * @return
	 */
	public Collection<Identifier> getWiringComponentPartIdentifiers(){
		if(wiringComponentPartIdentifiers==null){
			Collection<ParameterizedIdentifier> parameterizedIdentifiers=collectFieldsWithName(getWiringEndpoints(),Endpoint.COMPONENT);
			wiringComponentPartIdentifiers=collectFieldsWithName(parameterizedIdentifiers, ParameterizedIdentifier.IDENTIFIER);
		}
		return wiringComponentPartIdentifiers;
	}
	
	/**
	 * Gathers all specification, which are interfaceNames, identifier parts of a NesC wiring.
	 * @return
	 */
	public Collection<Identifier> getWiringSpecificationPartIdentifiers(){
		if(wiringSpecificationPartIdentifiers==null){
			Collection<ParameterizedIdentifier> parameterizedIdentifiers=collectFieldsWithName(getWiringEndpoints(),Endpoint.SPECIFICATION);
			wiringSpecificationPartIdentifiers=collectFieldsWithName(parameterizedIdentifiers, ParameterizedIdentifier.IDENTIFIER);
		}
		return wiringSpecificationPartIdentifiers;
	}
	
	/**
	 * Returns all component Identifiers in the configuration implementation with the given alias.
	 * If the given name is not an alias, an empty list is returned.
	 * @param name
	 * @return
	 */
	public Collection<Identifier> getComponentAliasIdentifiersWithName(String name){
		Collection<Identifier> result=new LinkedList<Identifier>();
		addIdentifiersWhichMatchName(name, getComponentAliasIdentifiers(), result);
		if(result.size()!=1){
			return Collections.emptyList();
		}
		addIdentifiersWhichMatchName(name, getWiringComponentPartIdentifiers(), result);
		return result;
	}
	
	/**
	 * Adds all identifiers of the identifiers collection, which have the same name as name, to the result collection.  
	 * @param name
	 * @param identifiers
	 * @param result
	 */
	private void addIdentifiersWhichMatchName(String name,Collection<Identifier> identifiers,Collection<Identifier> result){
		for(Identifier id:identifiers){
			if(name.equals(id.getName())){
				result.add(id);
			}
		}
	}
}
