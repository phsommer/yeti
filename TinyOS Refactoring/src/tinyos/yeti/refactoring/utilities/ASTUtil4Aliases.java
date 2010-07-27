package tinyos.yeti.refactoring.utilities;

import java.util.Collection;
import java.util.LinkedList;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ComponentList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Configuration;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceReference;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Module;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.RefComponent;

public class ASTUtil4Aliases {
	
	private ASTUtil astUtil;
	private ASTUtil4Components astUtil4Components;
	
	public ASTUtil4Aliases(){
		astUtil=new ASTUtil();
		astUtil4Components=new ASTUtil4Components(astUtil);
	}
	
	public ASTUtil4Aliases(ASTUtil astUtil) {
		super();
		this.astUtil = astUtil;
		astUtil4Components=new ASTUtil4Components(astUtil);
	}

	/**
	 * Checks if the given identifier is part of an AST node associated to an NesC alias like a component alias or a interface alias, which are introduces with the "as" keyword.
	 * @param identifier
	 * @return
	 */
	public boolean isAlias(Identifier identifier){
		return isInterfaceAliasingInSpecification(identifier)
			||isComponentAlias(identifier);
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC interface alias in the specification of a module/configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isInterfaceAliasingInSpecification(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!astUtil.isOfType(parent, InterfaceReference.class)){
			return false;
		}
		return astUtil.checkFieldName((InterfaceReference)parent, identifier, InterfaceReference.RENAME);
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC component alias in a NesC "components" statement in a implementation of a NesC configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAliasingInComponentsStatement(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!astUtil.isOfType(parent, RefComponent.class)){
			return false;
		}
		return astUtil.checkFieldName((RefComponent)parent, identifier, RefComponent.RENAME);
	}
	
	/**
	 * Checks if the given identifier is the identifier of a NesC component alias in a NesC component wiring in a implementation of a NesC configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAliasingInComponentWiring(Identifier identifier){
		if(!astUtil4Components.isComponentWiringComponentPart(identifier)){
			return false;
		}
		
		return false;
	}
	
	/**
	 * Checks if the given identifier is an Alias for a component in the implementation of a nesc configuration.
	 * @param identifier
	 * @return
	 */
	public boolean isComponentAlias(Identifier identifier){
		//Check if the given identifier even is in an implementation.
		ConfigurationDeclarationList implementationNode=astUtil4Components.getConfigurationImplementationNodeIfInside(identifier);
		if(implementationNode==null){
			return false;
		}
		//Check all Component aliases, if there alias name equals the one of the given identifier; 
		Collection<ComponentList> componentLists=astUtil.getChildsOfType(implementationNode, ComponentList.class);
		Collection<RefComponent> refComponents=new LinkedList<RefComponent>();
		for(ComponentList cl:componentLists){
			refComponents.addAll(astUtil.getAllNodesOfType(cl, RefComponent.class));
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

	/**
	 * Returns the "InterfaceReference" of the interface, which has the given alias, in the ast which includes the given node.
	 * Returns null if there is no interface with such an alias in the given ast.
	 * @param name
	 * @param astNode 
	 * @return
	 */
	public InterfaceReference getInterfaceNameWithAlias(String alias, ASTNode node) {
		AccessList specificationNode=null;
		if(astUtil4Components.isConfiguration(node)){
			Configuration configuration=astUtil4Components.getConfigurationNode(node);
			specificationNode=(AccessList)configuration.getField(Configuration.CONNECTIONS);
		}else if(astUtil4Components.isModule(node)){
			Module module=(Module)astUtil4Components.getModuleNode(node);
			specificationNode=(AccessList)module.getField(Module.CONNECTIONS);
		}else{
			return null;
		}
		Collection<InterfaceReference> iRefs=astUtil.getAllNodesOfType(specificationNode, InterfaceReference.class);
		for(InterfaceReference ref:iRefs){
			Identifier renameIdentifier=(Identifier)ref.getField(InterfaceReference.RENAME);
			if(renameIdentifier!=null&&alias.equals(renameIdentifier.getName())){
				return ref;
			}
		}
		return null;
	}
	
}
