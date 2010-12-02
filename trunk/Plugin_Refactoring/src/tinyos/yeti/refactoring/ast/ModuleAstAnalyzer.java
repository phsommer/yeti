package tinyos.yeti.refactoring.ast;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.NesCNameDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;

public class ModuleAstAnalyzer extends ComponentAstAnalyzer {

	private NesCExternalDefinitionList implementation;
	
	private Collection<FunctionDefinition> functionDefinitionsInImplementation;
	private Collection<NesCNameDeclarator> nesCFunctionDeclarationNames;
	private Collection<Identifier> nesCFunctionImplementationInterfaceIdentifiers;
	private Collection<Identifier> nesCFunctionImplementationFunctionIdentifiers;
	private Map<Identifier,Collection<Identifier>> localInterfaceName2AssociatedFunctionNames;
	
	private Collection<Identifier> implementationLocalVariableDeclarationNames;
	private Collection<Identifier> implementationLocalFunctionDeclarationNames;
	private Collection<Identifier> implementationLocalFunctionDefinitionNames;

	public ModuleAstAnalyzer(TranslationUnit root,Identifier componentIdentifier, AccessList specification,NesCExternalDefinitionList implementation) {
		super(root, componentIdentifier, specification);
		this.implementation = implementation;
	}
	
	/**
	 * Collects global function and variable declaration names.
	 * 
	 */
	private void collectImplementationLocalDeclarationNames(){
		implementationLocalFunctionDeclarationNames=new LinkedList<Identifier>();
		implementationLocalVariableDeclarationNames=new LinkedList<Identifier>();
		collectDeclarationNamesInScope(implementation, implementationLocalFunctionDeclarationNames, implementationLocalVariableDeclarationNames);
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
			Collection<FunctionDefinition> definitions=getFunctionDefinitionsInImplementation();
			Collection<FunctionDeclarator> declarators=unpackFunctionDefinitionsToFunctionDeclarator(definitions);
			for(FunctionDeclarator declarator:declarators){
				ASTNode node =declarator.getField(FunctionDeclarator.DECLARATOR);
				if(node instanceof NesCNameDeclarator){
					NesCNameDeclarator nescDeclarator=(NesCNameDeclarator)declarator.getField(FunctionDeclarator.DECLARATOR);
					if(nescDeclarator!=null){
						nesCFunctionDeclarationNames.add(nescDeclarator);
					}
				}
			}
		}
		return nesCFunctionDeclarationNames;
	}
	
	/**
	 * Returns a map which maps the local interface name to its associated functions, which are defined in this module.
	 * The local interface name may be the name of a real nesc interface or just an alias defined in this modules specification for a nesc interface.
	 * @return
	 */
	public Map<Identifier,Collection<Identifier>> getLocalInterfaceName2AssociatedFunctionNames(){
		if(localInterfaceName2AssociatedFunctionNames==null){
			localInterfaceName2AssociatedFunctionNames=new HashMap<Identifier,Collection<Identifier>>();
			Collection<NesCNameDeclarator> nameDeclarators=getNesCFunctionDeclarationNames();
			for(NesCNameDeclarator nameDeclarator:nameDeclarators){
				Identifier interfac=(Identifier)nameDeclarator.getField(NesCNameDeclarator.INTERFACE);
				Identifier function=(Identifier)nameDeclarator.getField(NesCNameDeclarator.FUNCTION_NAME);
				if(interfac!=null&&function!=null){
					Collection<Identifier> functions=localInterfaceName2AssociatedFunctionNames.get(interfac);
					if(functions==null){
						functions=new LinkedList<Identifier>();
						localInterfaceName2AssociatedFunctionNames.put(interfac, functions);
					}
					functions.add(function);
				}
					
			}
		}
		return localInterfaceName2AssociatedFunctionNames;
	}
	
	
	/**
	 * Returns the associated local interface name, could be a alias in the specification of this nesc module, of a function identifier in the module implementation.
	 * @param functionIdentifier
	 * @return
	 */
	public Identifier getAssociatedInterfaceName4FunctionIdentifier(Identifier functionIdentifier){
		Map<Identifier,Collection<Identifier>> map=getLocalInterfaceName2AssociatedFunctionNames();
		Set<Identifier> localInterfaceNames=map.keySet(); 
		for(Identifier localInterfaceName:localInterfaceNames){
			Collection<Identifier> functions=map.get(localInterfaceName);
			if(astUtil.containsIdentifierInstance(functionIdentifier,functions)){
				return localInterfaceName;
			}
		}
		return null;
	}
	
	/**
	 * Collects all Identifiers which stand for a interface name in the implementation of a NesCFunction like event or command in the implementation scope of a NesC module.
	 * The returned collection can also contain Identifiers which names are actually aliases of some interface. 
	 * In this case the alias has to be defined in the specification of this module.
	 * @return
	 */
	public Collection<Identifier> getNesCFunctionImplementationInterfaceIdentifiers(){
		if(nesCFunctionImplementationInterfaceIdentifiers==null){
			nesCFunctionImplementationInterfaceIdentifiers=astUtil.collectFieldsWithName(getNesCFunctionDeclarationNames(), NesCNameDeclarator.INTERFACE);
		}
		return nesCFunctionImplementationInterfaceIdentifiers;
	}
	
	/**
	 * Collects all Identifiers which stand for a function name in the implementation of a NesCFunction like event or command in the implementation scope of a NesC module.
	 * @return
	 */
	public Collection<Identifier> getNesCFunctionImplementationFunctionIdentifiers(){
		if(nesCFunctionImplementationFunctionIdentifiers==null){
			nesCFunctionImplementationFunctionIdentifiers=astUtil.collectFieldsWithName(getNesCFunctionDeclarationNames(), NesCNameDeclarator.FUNCTION_NAME);
		}
		return nesCFunctionImplementationFunctionIdentifiers;
	}
	
	/**
	 * Returns all name identifiers of implementation local variables.
	 * @return
	 */
	public Collection<Identifier> getImplementationLocalVariableDeclarationNames(){
		if(implementationLocalVariableDeclarationNames==null){
			collectImplementationLocalDeclarationNames();
		}
		return implementationLocalVariableDeclarationNames;
	}
	
	/**
	 * Returns all name identifiers of implementation local function declarations.
	 * @return
	 */
	public Collection<Identifier> getImplementationLocalFunctionDeclarationNames(){
		if(implementationLocalFunctionDeclarationNames==null){
			collectImplementationLocalDeclarationNames();
		}
		return implementationLocalFunctionDeclarationNames;
	}
	
	/**
	 * Returns all implementation local function definition names.
	 * @return
	 */
	public Collection<Identifier> getImplementationLocalFunctionDefinitionNames(){
		if(implementationLocalFunctionDefinitionNames==null){
			implementationLocalFunctionDefinitionNames=new LinkedList<Identifier>();
			collectFunctionDefinitionNamesInScope(implementation, implementationLocalFunctionDefinitionNames);
		}
		return implementationLocalFunctionDefinitionNames;
	}
	
	

}
