package tinyos.yeti.refactoring.utilities;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Configuration;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Endpoint;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Module;
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
			||isComponentWiringComponentPart(identifier);
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
		return ASTUtil.isOfType(parent, RefComponent.class);
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
		Endpoint endpoint=(Endpoint)parent;
		String fieldName=endpoint.getFieldName(pI);
		if(Endpoint.COMPONENT.equals(fieldName)){
			return true;
		}
		return false;
	}
	
}
