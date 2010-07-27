package tinyos.yeti.refactoring.ast;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ComponentList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Configuration;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Connection;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Endpoint;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Module;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.RefComponent;
import tinyos.yeti.refactoring.utilities.ASTUtil4Aliases;
import tinyos.yeti.refactoring.utilities.ASTUtil4Components;

public class ConfigurationAstAnalyzer extends ComponentAstAnalyser {

	private ConfigurationDeclarationList implementation;
	private Collection<RefComponent> components;
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
	 * Gathers all Component aliases identifiers which appear in the NesC "components" statements in the implementation
	 * @return
	 */
	public Collection<Identifier> getComponentAliasIdentifiers(){
		if(componentAliases==null){
			componentAliases=new LinkedList<Identifier>();
			Collection<RefComponent> components=getComponentDeclarations();
			for(RefComponent component:components){
				Identifier candidate=(Identifier)component.getField(RefComponent.RENAME);
				if(candidate!=null){
					componentAliases.add(candidate);
				}
			}
		}
		return componentAliases;
	}
	
	/**
	 * Gathers all wiring endpoints of the wirings in the implementation of the configuration which are in a NesC Wiring statement.
	 * @return
	 */
	public Collection<Endpoint> getWiringEndpoints(){
		if(wiringEndpoints==null){
			wiringEndpoints=new LinkedList<Endpoint>();
			Collection<Connection> connections=astUtil.getChildsOfType(implementation, Connection.class);
			for(Connection connection:connections){
				Endpoint left=(Endpoint)connection.getField(Connection.LEFT);
				if(left!=null){
					wiringEndpoints.add(left);
				}
				Endpoint right=(Endpoint)connection.getField(Connection.RIGHT);
				if(right!=null){
					wiringEndpoints.add(right);
				}
			}
		}
		return wiringEndpoints;
	}
	
	/**
	 * Gathers all component identifier parts of a NesC wiring.
	 * @return
	 */
	public Collection<Identifier> getWiringComponentPartIdentifiers(){
		if(wiringComponentPartIdentifiers==null){
			wiringComponentPartIdentifiers=new LinkedList<Identifier>();
			for(Endpoint endpoint:getWiringEndpoints()){
				ParameterizedIdentifier pI=(ParameterizedIdentifier)endpoint.getField(Endpoint.COMPONENT);
				if(pI!=null){
					Identifier id=(Identifier)pI.getField(ParameterizedIdentifier.IDENTIFIER);
					if(id!=null){
						wiringComponentPartIdentifiers.add(id);
					}
				}
			}
		}
		return wiringComponentPartIdentifiers;
	}
	
	/**
	 * Gathers all specification, which are interfaceNames, identifier parts of a NesC wiring.
	 * @return
	 */
	public Collection<Identifier> getWiringSpecificationPartIdentifiers(){
		if(wiringSpecificationPartIdentifiers==null){
			wiringSpecificationPartIdentifiers=new LinkedList<Identifier>();
			for(Endpoint endpoint:getWiringEndpoints()){
				ParameterizedIdentifier pI=(ParameterizedIdentifier)endpoint.getField(Endpoint.SPECIFICATION);
				if(pI!=null){
					Identifier id=(Identifier)pI.getField(ParameterizedIdentifier.IDENTIFIER);
					if(id!=null){
						wiringSpecificationPartIdentifiers.add(id);
					}
				}
			}
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Checks if the given identifier is part of an AST node associated to an NesC component like a module or a configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponent(Identifier identifier){
		return isComponentDefinition(identifier)
			||isComponentDeclaration(identifier)
			||isComponentWiringComponentPartNotAliased(identifier);
	}

	/**
	 * Checks if the given identifier is the identifier of a NesC Component Definition like a Module or a Configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentDefinition(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(	getASTUtil().isOfType(parent, Configuration.class)||getASTUtil().isOfType(parent, Module.class)){
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the given identifier is the component identifier of a NesC configuration' "components" statement.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentDeclaration(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!getASTUtil().isOfType(parent, RefComponent.class)){
			return false;
		}
		return getASTUtil().checkFieldName((RefComponent)parent, identifier, RefComponent.NAME);
	}
	
	/**
	 * Checks if the given identifier is the component identifier of a NesC configuration wiring  statement.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentWiringComponentPart(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!getASTUtil().isOfType(parent,ParameterizedIdentifier.class)){
			return false;
		}
		ParameterizedIdentifier pI=(ParameterizedIdentifier)parent;
		parent=pI.getParent();
		if(!getASTUtil().isOfType(parent,Endpoint.class)){
			return false;
		}
		return getASTUtil().checkFieldName((Endpoint)parent, pI, Endpoint.COMPONENT);
	}
	
	/**
	 * Checks if the given identifier is the component identifier of a NesC configuration wiring  statement, and if it is no alias.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentWiringComponentPartNotAliased(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!getASTUtil().isOfType(parent,ParameterizedIdentifier.class)){
			return false;
		}
		ParameterizedIdentifier pI=(ParameterizedIdentifier)parent;
		parent=pI.getParent();
		if(!getASTUtil().isOfType(parent,Endpoint.class)){
			return false;
		}
		if(!getASTUtil().checkFieldName((Endpoint)parent, pI, Endpoint.COMPONENT)){
			return false;
		}
		ASTUtil4Aliases astUtil4Aliases=new ASTUtil4Aliases(getASTUtil());
		return !astUtil4Aliases.isComponentAlias(identifier);
	}

	/**
	 * Returns the root node in the ast for a module implementation.
	 * Null if the given node is not in an implementation.
	 * @param node
	 * @return
	 */
	public NesCExternalDefinitionList getModuleImplementationNodeIfInside(ASTNode node){
		//Get the root node for the local implementation of this module.
		ASTNode root=getASTUtil().getParentForName(node, NesCExternalDefinitionList.class);
		if(root==null){
			return null;
		}
		return (NesCExternalDefinitionList)root;
	}
	
	/**
	 * Returns the root node in the ast for a configuration implementation.
	 * Null if the given node is not in an implementation.
	 * @param node
	 * @return 
	 */
	public ConfigurationDeclarationList getConfigurationImplementationNodeIfInside(ASTNode node){
		//Get the root node for the local implementation of this module.
		ASTNode root=getASTUtil().getParentForName(node, ConfigurationDeclarationList.class);
		if(root==null){
			return null;
		}
		return (ConfigurationDeclarationList)root;
	}
	
	/**
	 * Returns the "Module" node of the AST which includes the given node.Returns null if the node is not in an ast with a module node, which means this is with verry high probability no module file.
	 * @param node
	 * @return
	 */
	public Module getModuleNode(ASTNode node){
		ASTNode root=getASTUtil().getAstRoot(node);
		Collection<Module> modules=getASTUtil().getChildsOfType(root,Module.class);
		if(modules.size()!=1){
			return null;
		}
		return modules.iterator().next();
	}
	
	/**
	 * Same as getModuleNode, but for Configuration.
	 * @param node
	 * @return
	 */
	public Configuration getConfigurationNode(ASTNode node){
		ASTNode root=getASTUtil().getAstRoot(node);
		Collection<Configuration> configuration=getASTUtil().getChildsOfType(root,Configuration.class);
		if(configuration.size()!=1){
			return null;
		}
		return configuration.iterator().next();
	}
	
	/**
	 * Checks if the given node is part of a module ast.
	 * @param node
	 * @return
	 */
	public boolean isModule(ASTNode node){
		return getModuleNode(node)!=null;
	}
	
	/**
	 * Checks if the given node is part of a configuration ast.
	 * @param node
	 * @return
	 */
	public boolean isConfiguration(ASTNode node){
		return getConfigurationNode(node)!=null;
	}
	
	/**
	 * If the given node is in a configuration or a module, this method returns the identifier of the module, else it returns null.
	 * @param selectedIdentifier
	 * @return
	 */
	public Identifier getIdentifierOFComponentDefinition(ASTNode node){
		ASTUtil4Components astUtil4Components=new ASTUtil4Components(getASTUtil());
		
		Identifier id=null;
		ASTNode root=getASTUtil().getAstRoot(node);
		if(astUtil4Components.isConfiguration(root)){
			Configuration configuration=astUtil4Components.getConfigurationNode(node);
			id=(Identifier)configuration.getField(Configuration.NAME);
		}else if(astUtil4Components.isModule(root)){
			Module module=astUtil4Components.getModuleNode(node);
			id=(Identifier)module.getField(Module.NAME);
		}
		return id;
	}
	
	
	
}
