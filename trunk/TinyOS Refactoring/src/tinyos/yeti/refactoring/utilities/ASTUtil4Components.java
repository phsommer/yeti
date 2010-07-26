package tinyos.yeti.refactoring.utilities;

import java.util.Collection;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Configuration;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Endpoint;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Module;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.RefComponent;

public class ASTUtil4Components {
	
	/**
	 * Checks if the given identifier is part of an AST node associated to an NesC component like a module or a configuration.
	 * @param identifier
	 * @return
	 */
	public static boolean isComponent(Identifier identifier){
		return isComponentDefinition(identifier)
			||isComponentDeclaration(identifier)
			||isComponentWiringComponentPartNotAliased(identifier);
	}

	/**
	 * Checks if the given identifier is the identifier of a NesC Component Definition like a Module or a Configuration.
	 * @param identifier
	 * @return
	 */
	public static boolean isComponentDefinition(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(	ASTUtil.isOfType(parent, Configuration.class)||ASTUtil.isOfType(parent, Module.class)){
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the given identifier is the component identifier of a NesC configuration' "components" statement.
	 * @param identifier
	 * @return
	 */
	public static boolean isComponentDeclaration(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!ASTUtil.isOfType(parent, RefComponent.class)){
			return false;
		}
		return ASTUtil.checkFieldName((RefComponent)parent, identifier, RefComponent.NAME);
	}
	
	/**
	 * Checks if the given identifier is the component identifier of a NesC configuration wiring  statement.
	 * @param identifier
	 * @return
	 */
	public static boolean isComponentWiringComponentPart(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!ASTUtil.isOfType(parent,ParameterizedIdentifier.class)){
			return false;
		}
		ParameterizedIdentifier pI=(ParameterizedIdentifier)parent;
		parent=pI.getParent();
		if(!ASTUtil.isOfType(parent,Endpoint.class)){
			return false;
		}
		return ASTUtil.checkFieldName((Endpoint)parent, pI, Endpoint.COMPONENT);
	}
	
	/**
	 * Checks if the given identifier is the component identifier of a NesC configuration wiring  statement, and if it is no alias.
	 * @param identifier
	 * @return
	 */
	public static boolean isComponentWiringComponentPartNotAliased(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!ASTUtil.isOfType(parent,ParameterizedIdentifier.class)){
			return false;
		}
		ParameterizedIdentifier pI=(ParameterizedIdentifier)parent;
		parent=pI.getParent();
		if(!ASTUtil.isOfType(parent,Endpoint.class)){
			return false;
		}
		if(!ASTUtil.checkFieldName((Endpoint)parent, pI, Endpoint.COMPONENT)){
			return false;
		}
		return !ASTUtil4Aliases.isComponentAlias(identifier);
	}

	/**
	 * Returns the root node in the ast for a module implementation.
	 * Null if the given node is not in an implementation.
	 * @param node
	 * @return
	 */
	public static NesCExternalDefinitionList getModuleImplementationNodeIfInside(ASTNode node){
		//Get the root node for the local implementation of this module.
		ASTNode root=ASTUtil.getParentForName(node, NesCExternalDefinitionList.class);
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
	public static ConfigurationDeclarationList getConfigurationImplementationNodeIfInside(ASTNode node){
		//Get the root node for the local implementation of this module.
		ASTNode root=ASTUtil.getParentForName(node, ConfigurationDeclarationList.class);
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
	public static Module getModuleNode(ASTNode node){
		ASTNode root=ASTUtil.getAstRoot(node);
		Collection<Module> modules=ASTUtil.getChildsOfType(root,Module.class);
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
	public static Configuration getConfigurationNode(ASTNode node){
		ASTNode root=ASTUtil.getAstRoot(node);
		Collection<Configuration> configuration=ASTUtil.getChildsOfType(root,Configuration.class);
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
	public static boolean isModule(ASTNode node){
		return getModuleNode(node)!=null;
	}
	
	/**
	 * Checks if the given node is part of a configuration ast.
	 * @param node
	 * @return
	 */
	public static boolean isConfiguration(ASTNode node){
		return getConfigurationNode(node)!=null;
	}
	
	/**
	 * If the given node is in a configuration or a module, this method returns the identifier of the module, else it returns null.
	 * @param selectedIdentifier
	 * @return
	 */
	public static Identifier getIdentifierOFComponentDefinition(ASTNode node){
		Identifier id=null;
		ASTNode root=ASTUtil.getAstRoot(node);
		if(ASTUtil4Components.isConfiguration(root)){
			Configuration configuration=ASTUtil4Components.getConfigurationNode(node);
			id=(Identifier)configuration.getField(Configuration.NAME);
		}else if(ASTUtil4Components.isModule(root)){
			Module module=ASTUtil4Components.getModuleNode(node);
			id=(Identifier)module.getField(Module.NAME);
		}
		return id;
	}
	
}
