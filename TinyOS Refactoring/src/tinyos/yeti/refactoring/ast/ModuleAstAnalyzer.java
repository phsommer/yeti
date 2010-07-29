package tinyos.yeti.refactoring.ast;

import java.util.Collection;
import java.util.LinkedList;

import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclarationSpecifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.NesCNameDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.StorageClass;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.refactoring.utilities.DebugUtil;

public class ModuleAstAnalyzer extends ComponentAstAnalyser {

	private NesCExternalDefinitionList implementation;
	private Collection<FunctionDefinition> functionDefinitionsInImplementation;
	private Collection<NesCNameDeclarator> nesCFunctionDeclarationNames;
	private Collection<Identifier> nesCFunctionImplementationInterfaceIdentifiers;
	private Collection<Identifier> nesCFunctionImplementationFunctionIdentifiers;

	public ModuleAstAnalyzer(TranslationUnit root,Identifier componentIdentifier, AccessList specification,NesCExternalDefinitionList implementation) {
		super(root, componentIdentifier, specification);
		this.implementation = implementation;
	}
	
	/**
	 * Collects all FunctionDefinitions in the implementation scope of a NesC module.
	 * This definitions can be C functions as well as NesC functions like events/commands.
	 * @return
	 */
	public Collection<FunctionDefinition> getFunctionDefinitionsInImplementation(){
		if(functionDefinitionsInImplementation==null){
			functionDefinitionsInImplementation =astUtil.getChildsOfType(implementation, FunctionDefinition.class);
		}
		return functionDefinitionsInImplementation;
	}
	
	/**
	 * Collects all NesCNameDeclarators of the NesC functions like event or command in the implementation of this NesC module.
	 * @return
	 */
	public Collection<NesCNameDeclarator> getNesCFunctionDeclarationNames(){
		if(nesCFunctionDeclarationNames==null){
			nesCFunctionDeclarationNames=new LinkedList<NesCNameDeclarator>();
			for(FunctionDefinition functionDefinition:getFunctionDefinitionsInImplementation()){
				if(isNesCFunction(functionDefinition)){
					FunctionDeclarator functionDeclarator=(FunctionDeclarator)functionDefinition.getField(FunctionDefinition.DECLARATOR);
					if(functionDeclarator!=null){
						NesCNameDeclarator declarator=(NesCNameDeclarator)functionDeclarator.getField(FunctionDeclarator.DECLARATOR);
						if(declarator!=null){
							nesCFunctionDeclarationNames.add(declarator);
						}
					}
				}
			}
		}
		return nesCFunctionDeclarationNames;
	}
	
	/**
	 * Collects all Identifiers which stand for a interface name in the implementation of a NesCFunction like event or command in the implementation scope of a NesC module.
	 * The returned collection can also contain Identifiers which names are actually aliases of some interface. 
	 * In this case the alias has to be defined in the specification of this module.
	 * @return
	 */
	public Collection<Identifier> getNesCFunctionImplementationInterfaceIdentifiers(){
		if(nesCFunctionImplementationInterfaceIdentifiers==null){
			nesCFunctionImplementationInterfaceIdentifiers=collectFieldsWithName(getNesCFunctionDeclarationNames(), NesCNameDeclarator.INTERFACE);
		}
		return nesCFunctionImplementationInterfaceIdentifiers;
	}
	
	/**
	 * Collects all Identifiers which stand for a function name in the implementation of a NesCFunction like event or command in the implementation scope of a NesC module.
	 * @return
	 */
	public Collection<Identifier> getNesCFunctionImplementationFunctionIdentifiers(){
		if(nesCFunctionImplementationFunctionIdentifiers==null){
			nesCFunctionImplementationFunctionIdentifiers=collectFieldsWithName(getNesCFunctionDeclarationNames(), NesCNameDeclarator.FUNCTION_NAME);
		}
		return nesCFunctionImplementationFunctionIdentifiers;
	}
	
	/**
	 * Checks if the FunctionDefinition is the definition of a NesC function like a command or event and not just a pure C function.
	 * @param definition
	 * @return
	 */
	private boolean isNesCFunction(FunctionDefinition definition){
		DeclarationSpecifierList list=(DeclarationSpecifierList)definition.getField(FunctionDefinition.SPECIFIERS);
		if(list==null){
			return false;
		}
		Collection<StorageClass> storageClasses=astUtil.getChildsOfType(list, StorageClass.class);
		return storageClasses.size()==1;	//If this was a pure C function there was no storage class.

	}

}
