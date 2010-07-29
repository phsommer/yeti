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
	 * Returns all component Identifiers in the configuration implementation which reference or define the given alias.
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
	 * Returns the associated Component identifier in a NesC components statement for the componentAlias identifier in the statement.
	 * Returns null if there is no such componentAlias.
	 * @param componentAlias
	 * @return
	 */
	public Identifier getComponentIdentifier4ComponentAliasIdentifier(Identifier componentAlias){
		for(Identifier id:getComponentAliasIdentifiers()){
			if(id==componentAlias){
				RefComponent parent=(RefComponent)id.getParent();
				return (Identifier)parent.getField(RefComponent.NAME);
			}
		}
		return null;
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

	/**
	 * Returns the component identifier of the component which this interfaceIdentifier is associated with.
	 * The given interfaceIdentifier has to be in a component wiring statement.
	 * This component identifier is either the component field of the endpoint in which the given interfaceIdentifier is or 
	 * the identifier of this configuration itself, in case the given interfaceIdentifier actually references an interface in this configurations specification.
	 * Note: If the interface identifier has a associated component field in the wiring, then this component identifier can also be an alias defined in this configuration components statements.
	 * Returns null if the given identifier doesn't appear in a componentWiring Specification part.  
	 * @param selection
	 */
	public Identifier getAssociatedComponentIdentifier4InterfaceIdentifierInWiring(Identifier interfaceIdentifier) {
		Collection<Identifier> identifiers=getWiringSpecificationPartIdentifiers();
		//TODO replace with call to InterfaceSelectionIdentifier as soon as this class is created.
		//Check if the given identifier is an interfaceIdentifier
		boolean found=false;
		for(Identifier identifier:identifiers){
			if(identifier==interfaceIdentifier){
				found=true;
			}
		}
		if(!found){
			return null;
		}
		
		//Try to get associated Component of the wiring
		Endpoint endpoint=(Endpoint)astUtil.getParentForName(interfaceIdentifier, Endpoint.class);
		if(endpoint==null){
			return null;
		}
		ParameterizedIdentifier pI=(ParameterizedIdentifier)endpoint.getField(Endpoint.COMPONENT);
		if(pI==null){
			return null;
		}
		Identifier targetComponent=(Identifier)pI.getField(ParameterizedIdentifier.IDENTIFIER);
		if(targetComponent!=null){
			return targetComponent;
		}
		
		//If there is no component associated with the interface, it has to be an implicit reference to the this configuration itself.
		return componentIdentifier;
	}
}
