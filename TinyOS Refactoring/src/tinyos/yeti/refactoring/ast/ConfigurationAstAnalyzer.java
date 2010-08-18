package tinyos.yeti.refactoring.ast;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ComponentList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Connection;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Endpoint;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.RefComponent;
import tinyos.yeti.refactoring.utilities.DebugUtil;

public class ConfigurationAstAnalyzer extends ComponentAstAnalyzer {

	private ConfigurationDeclarationList implementation;
	private Collection<RefComponent> components;
	private Map<Identifier,Identifier> componentLocalName2ComponentGlobalName;
	private Collection<Identifier> referencedComponents;
	private Collection<String> namesOfReferencedComponents;
	private Collection<Identifier> componentAliases;
	private Collection<Endpoint> wiringEndpoints;
	private Collection<Identifier> wiringComponentPartIdentifiers;
	private Collection<Identifier> wiringInterfacePartIdentifiers;

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
	 * Returns a map which maps the componentLocalNameIdentifier, which is an component rename with the NesC "components" keyword in the configuration implementation,
	 * to the componentGlobalName, which is the component before the as keyword in the configuration implementation.
	 * If there is no as keyword for a given component reference then componentGlobalName==componentLocalName.
	 * @return
	 */
	public Map<Identifier,Identifier> getComponentLocalName2ComponentGlobalName(){
		if(componentLocalName2ComponentGlobalName==null){
			componentLocalName2ComponentGlobalName=new HashMap<Identifier, Identifier>();
			for(RefComponent reference:getComponentDeclarations()){
				Identifier componentGlobalName=(Identifier)reference.getField(RefComponent.NAME);
				if(componentGlobalName!=null){
					Identifier componentLocalName=(Identifier)reference.getField(RefComponent.RENAME);
					if(componentLocalName==null){
						componentLocalName=componentGlobalName;
					}
					componentLocalName2ComponentGlobalName.put(componentLocalName, componentGlobalName);
				}
			}
		}
		return componentLocalName2ComponentGlobalName;
	}
	
	/**
	 * Gathers all identifiers which are found in the configuration implementation in a NesC "components" statement, which reference some NesC Component.
	 * @return
	 */
	public Collection<Identifier> getIdentifiersOfReferencedComponents(){
		if(referencedComponents==null){
			referencedComponents= astUtil.collectFieldsWithName(getComponentDeclarations(), RefComponent.NAME);
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
			componentAliases=astUtil.collectFieldsWithName(components, RefComponent.RENAME);
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
			wiringEndpoints=astUtil.collectFieldsWithName(connections, Connection.LEFT);
			Collection<Endpoint> rightEndpoints=astUtil.collectFieldsWithName(connections, Connection.RIGHT);
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
			collectWiringsIdentifiers();
		}
		return wiringComponentPartIdentifiers;
	}
	
	/**
	 * Gathers all interface identifier parts of a NesC wiring.
	 * @return
	 */
	public Collection<Identifier> getWiringInterfacePartIdentifiers(){
		if(wiringInterfacePartIdentifiers==null){
			collectWiringsIdentifiers();
		}
		return wiringInterfacePartIdentifiers;
	}
	
	/**
	 * 	Gathers all component identifier parts of a NesC wiring.
	 * 	And Gathers all interface identifier parts of a NesC wiring.
	 */
	private void collectWiringsIdentifiers(){
		wiringComponentPartIdentifiers=new LinkedList<Identifier>();
		wiringInterfacePartIdentifiers=new LinkedList<Identifier>();
		for(Endpoint endpoint:getWiringEndpoints()){
			ParameterizedIdentifier componentPart=(ParameterizedIdentifier)endpoint.getField(Endpoint.COMPONENT);
			ParameterizedIdentifier specificationPart=(ParameterizedIdentifier)endpoint.getField(Endpoint.SPECIFICATION);
			if(componentPart!=null&&specificationPart!=null){
				
				Identifier component=(Identifier)componentPart.getField(ParameterizedIdentifier.IDENTIFIER);
				Identifier interFace=(Identifier)specificationPart.getField(ParameterizedIdentifier.IDENTIFIER);
				if(component!=null&&interFace!=null){	//This shoudl always be true, otherwise there was a problem in the parser.
					wiringComponentPartIdentifiers.add(component);
					wiringInterfacePartIdentifiers.add(interFace);
				}
			}else if(componentPart!=null){	//In this case component part can be an interface or an component
				Identifier candidate=(Identifier)componentPart.getField(ParameterizedIdentifier.IDENTIFIER);
				if(candidate!=null){
					DebugUtil.immediatePrint("Tie! for "+candidate.getName());
					if(isComponentName(candidate)){
						DebugUtil.immediatePrint("Component");
						wiringComponentPartIdentifiers.add(candidate);
					}else{	//If it is not a component name it has to be an interface name.
						DebugUtil.immediatePrint("Interface");
						wiringInterfacePartIdentifiers.add(candidate);
					}
				}
			}
		}
	}
	
	/**
	 * Checks if the given identifier is a component reference or an alias of such a reference in a NesC components statement.
	 * @return
	 */
	private boolean isComponentName(Identifier componentIdentifier){
		return getComponentLocalName2ComponentGlobalName().get(componentIdentifier)!=null;
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
	public Identifier getComponentIdentifier4ComponentAliasIdentifier(String componentAlias){
		for(Identifier id:getComponentAliasIdentifiers()){
			if(componentAlias.equals(id.getName())){
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
		DebugUtil.immediatePrint("getAssociatedComponentIdentifier4InterfaceIdentifierInWiring");
		
		//Try to get associated Component of the wiring
		Endpoint endpoint=(Endpoint)astUtil.getParentForName(interfaceIdentifier, Endpoint.class);
		if(endpoint==null){
			DebugUtil.immediatePrint("\tendpoint");
			return null;
		}
		ParameterizedIdentifier pI=(ParameterizedIdentifier)endpoint.getField(Endpoint.COMPONENT);
		if(pI==null){
			DebugUtil.immediatePrint("\tpI");
			return null;
		}
		Identifier targetComponent=(Identifier)pI.getField(ParameterizedIdentifier.IDENTIFIER);
		if(targetComponent!=interfaceIdentifier){	//If there is just one identifier involved in the wiring, the identifier is in the component field of the Endpoint. => If the target component == the given identifier then this must be a interface identifier
			DebugUtil.immediatePrint("\t!=");
			return targetComponent;
		}
		//If there is no component associated with the interface, it has to be an implicit reference to the this configuration itself.
		DebugUtil.immediatePrint("\tgetEntityIdentifier");
		return getEntityIdentifier();
	}
	
	/**
	 * Returns the name of the component with which this interface wiring is associated.
	 * Returns null, if the given identifier is no interface identifier in a wiring. 
	 * @param identifier
	 * @return
	 */
	public String getUseDefiningComponent4InterfaceInWiring(Identifier interfaceIdentifier){
		Identifier associatedComponent=getAssociatedComponentIdentifier4InterfaceIdentifierInWiring(interfaceIdentifier);
		if(associatedComponent==null){	//this should never happen
			return null;
		}
		if(associatedComponent==getEntityIdentifier()){	//In this case the interfaceIdentifier has a implizit reference on the configuration itself.
			return associatedComponent.getName();
		}
		Identifier realComponent=getComponentIdentifier4ComponentAliasIdentifier(associatedComponent.getName());
		if(realComponent!=null){	//If the associatedComponent is allready the real component, then realComponent is null.
			return realComponent.getName();
		}
		return associatedComponent.getName();
	}
}
