package tinyos.yeti.refactoring.utilities;

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
	
}
