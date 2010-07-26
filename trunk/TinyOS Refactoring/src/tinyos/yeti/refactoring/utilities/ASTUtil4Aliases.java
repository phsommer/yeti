package tinyos.yeti.refactoring.utilities;

import java.util.Collection;
import java.util.LinkedList;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ComponentList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceReference;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.RefComponent;

public class ASTUtil4Aliases {

	/**
	 * Checks if the given identifier is part of an AST node associated to an NesC alias like a component alias or a interface alias, which are introduces with the "as" keyword.
	 * @param identifier
	 * @return
	 */
	public static boolean isAlias(Identifier identifier){
		return isInterfaceAliasingInSpecification(identifier)
			||isComponentAlias(identifier);
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC interface alias in the specification of a module/configuration.
	 * @param identifier
	 * @return
	 */
	public static boolean isInterfaceAliasingInSpecification(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!ASTUtil.isOfType(parent, InterfaceReference.class)){
			return false;
		}
		return ASTUtil.checkFieldName((InterfaceReference)parent, identifier, InterfaceReference.RENAME);
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC component alias in a NesC "components" statement in a implementation of a NesC configuration.
	 * @param identifier
	 * @return
	 */
	public static boolean isComponentAliasingInComponentsStatement(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!ASTUtil.isOfType(parent, RefComponent.class)){
			return false;
		}
		return ASTUtil.checkFieldName((RefComponent)parent, identifier, RefComponent.RENAME);
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC component alias in a NesC component wiring in a implementation of a NesC configuration.
	 * @param identifier
	 * @return
	 */
	public static boolean isComponentAliasingInComponentWiring(Identifier identifier){
		if(!ASTUtil4Components.isComponentWiringComponentPart(identifier)){
			return false;
		}
		
		return false;
	}
	
	public static boolean isComponentAlias(Identifier identifier){
		//Check if the given identifier even is in an implementation.
		ConfigurationDeclarationList implementationNode=ASTUtil4Components.getConfigurationImplementationNodeIfInside(identifier);
		if(implementationNode==null){
			return false;
		}
		//Check all Component aliases, if there alias name equals the one of the given identifier; 
		Collection<ComponentList> componentLists=ASTUtil.getChildsOfType(implementationNode, ComponentList.class);
		Collection<RefComponent> refComponents=new LinkedList<RefComponent>();
		for(ComponentList cl:componentLists){
			refComponents.addAll(ASTUtil.getAllNodesOfType(cl, RefComponent.class));
		}
		String targetName=identifier.getName();
		for(RefComponent ref:refComponents){
			Identifier renameIdentifier=(Identifier)ref.getField(RefComponent.RENAME);
			if(renameIdentifier!=null&&targetName.equals(renameIdentifier.getName())){
				return true;
			}
		}
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
