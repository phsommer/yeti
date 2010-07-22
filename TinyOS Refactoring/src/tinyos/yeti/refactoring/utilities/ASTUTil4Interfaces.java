package tinyos.yeti.refactoring.utilities;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.NesCNameDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Interface;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceType;

public class ASTUTil4Interfaces {

	/**
	 * Checks if the given identifier is part of an AST node associated to an interface.
	 * @param identifier
	 * @return
	 */
	public static boolean isInterface(Identifier identifier){
		return isInterfaceDeclaration(identifier)
			||isInterfaceDefinition(identifier)
			||isInterfaceImplementation(identifier);
	}
	
	/**
	 * Checks if the given identifier is part of an interface definition,
	 * which is the interface identifier in the file which defines the interface.
	 * @param identifier
	 * @return
	 */
	public static boolean isInterfaceDefinition(Identifier identifier){
		return ASTUtil.isOfType(identifier.getParent(), Interface.class);
	}
	
	/**
	 * Checks if the given identifier is part of a interface reference in a provide or use statement of a module.
	 * @param identifier
	 * @return
	 */
	public static boolean isInterfaceDeclaration(Identifier identifier){
		return ASTUtil.isOfType(identifier.getParent(), InterfaceType.class);
	}
	
	/**
	 * Checks if the given identifier is part of a interface reference in a event or call implementation of a module.
	 * @param identifier
	 * @return
	 */
	public static boolean isInterfaceImplementation(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!ASTUtil.isOfType(parent, NesCNameDeclarator.class)){
			return false;
		}
		//The first child is the interface identifier, the second the event/command identifier
		return identifier.equals((Identifier)parent.getChild(0));
	}
	
}
