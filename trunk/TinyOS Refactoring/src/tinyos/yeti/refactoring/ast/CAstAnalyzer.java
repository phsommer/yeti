package tinyos.yeti.refactoring.ast;

import java.util.Collection;
import java.util.LinkedList;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclaratorList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.PointerDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class CAstAnalyzer extends AstAnalyzer {

	protected TranslationUnit root;
	
	private Collection<Identifier> globalVariableDeclarationNames;
	private Collection<Identifier> globalFunctionDeclarationNames;
	private Collection<Identifier> globalFunctionDefinitionNames;
	
	public CAstAnalyzer(TranslationUnit root) {
		super();
		this.root = root;
	}
	
	/**
	 * Collects global function and variable declaration names.
	 * 
	 */
	private void collectGlobalDeclarationNames(){
		globalFunctionDeclarationNames=new LinkedList<Identifier>();
		globalVariableDeclarationNames=new LinkedList<Identifier>();
		collectDeclarationNamesInScope(root, globalFunctionDeclarationNames, globalVariableDeclarationNames);
	}
	
	/**
	 * Searches the declaration for its specific Declaration name field. 
	 * For a function declaration it will add a FunctionDeclarator to the returned Collection, for a variable declaration a DeclaratorName.
	 * Especially removes PointerDeclarations which encapsulate the specific declaration. 
	 * @param declarations
	 * @return
	 */
	protected Collection<ASTNode> unpackDeclarationsToSpecificDeclarations(Collection<Declaration> declarations){
		Collection<ASTNode> specificDeclarations=new LinkedList<ASTNode>();
		for(Declaration declaration:declarations){
			InitDeclaratorList list=(InitDeclaratorList)declaration.getField(Declaration.INIT_LIST);
			if(list!=null){
				InitDeclarator initDec=astUtil.getFirstChildOfType(list, InitDeclarator.class);
				if(initDec!=null){
					ASTNode node=initDec.getField(InitDeclarator.DECLARATOR);
					if(node!=null){
						if(node instanceof PointerDeclarator){
							node=((PointerDeclarator)node).getField(PointerDeclarator.DECLARATOR);
						}
						specificDeclarations.add(node);
					}
				}
			}
		}
		return specificDeclarations;
	}
	
	/**
	 * Searches the FunctionDefinition for its specific Declaration name field. 
	 * It will add a FunctionDeclarator to the returned Collection for every definition..
	 * Especially removes PointerDeclarations which encapsulate the specific declaration. 
	 * @param declarations
	 * @return
	 */
	protected Collection<FunctionDeclarator> unpackFunctionDefinitionsToFunctionDeclarator(Collection<FunctionDefinition> definitions){
		Collection<FunctionDeclarator> declarators=new LinkedList<FunctionDeclarator>();
		for(FunctionDefinition definition:definitions){
			ASTNode node=definition.getField(FunctionDefinition.DECLARATOR);
			if(node instanceof PointerDeclarator){
				node=((PointerDeclarator)node).getField(PointerDeclarator.DECLARATOR);
			}
			if(node instanceof FunctionDeclarator){
				FunctionDeclarator funcDeclarator=(FunctionDeclarator)node;
				declarators.add(funcDeclarator);
			}
		}
		return declarators;
	}
	
	/**
	 * Collects function and variable declaration names in the given scope and adds them to the given specific Collection.
	 * @param scope
	 * @param functionDeclarations
	 * @param variableDeclarations
	 */
	protected void collectDeclarationNamesInScope(ASTNode scope,Collection<Identifier> functionDeclarations,Collection<Identifier> variableDeclarations){
		Collection<Declaration> declarations=astUtil.getChildsOfType(scope, Declaration.class);
		Collection<ASTNode> specificDeclarations=unpackDeclarationsToSpecificDeclarations(declarations);
		for(ASTNode node:specificDeclarations){
			if(node instanceof FunctionDeclarator){
				FunctionDeclarator funcDeclarator=(FunctionDeclarator)node;
				node=funcDeclarator.getField(FunctionDeclarator.DECLARATOR);
				if(node instanceof DeclaratorName){
					DeclaratorName decName=(DeclaratorName)node;
					Identifier name= (Identifier)decName.getField(DeclaratorName.NAME);
					if(name!=null){
						functionDeclarations.add(name);
					}
				}
			}else if(node instanceof DeclaratorName){
				DeclaratorName decName=(DeclaratorName)node;
				Identifier name=(Identifier)decName.getField(DeclaratorName.NAME);
				if(name!=null){
					variableDeclarations.add(name);
				}
			}
		}

	}

	/**
	 * Collects function definition names in the given scope and adds them to the given Collection.
	 * @param scope
	 * @param functionDeclarations
	 * @param variableDeclarations
	 */
	protected void collectFunctionDefinitionNamesInScope(ASTNode scope,Collection<Identifier> functionDefinitions){
		Collection<FunctionDefinition> definitions=astUtil.getChildsOfType(scope, FunctionDefinition.class);
		Collection<FunctionDeclarator> declarators=unpackFunctionDefinitionsToFunctionDeclarator(definitions);
		for(FunctionDeclarator declarator:declarators){
			ASTNode node=declarator.getField(FunctionDeclarator.DECLARATOR);
			if(node instanceof DeclaratorName){
				DeclaratorName decName=(DeclaratorName)node;
				Identifier name= (Identifier)decName.getField(DeclaratorName.NAME);
				if(name!=null){
					functionDefinitions.add(name);
				}
			}
		}

	}

	
	/**
	 * Returns all name identifiers of global variables.
	 * @return
	 */
	public Collection<Identifier> getGlobalVariableDeclarationNames(){
		if(globalVariableDeclarationNames==null){
			collectGlobalDeclarationNames();
		}
		return globalVariableDeclarationNames;
	}
	
	/**
	 * Returns all name identifiers of global function declarations.
	 * @return
	 */
	public Collection<Identifier> getGlobalFunctionDeclarationNames(){
		if(globalFunctionDeclarationNames==null){
			collectGlobalDeclarationNames();
		}
		return globalFunctionDeclarationNames;
	}
	
	/**
	 * Returns all global function definition names.
	 * @return
	 */
	public Collection<Identifier> getGlobalFunctionDefinitionNames(){
		if(globalFunctionDefinitionNames==null){
			globalFunctionDefinitionNames=new LinkedList<Identifier>();
			collectFunctionDefinitionNamesInScope(root, globalFunctionDefinitionNames);
		}
		return globalFunctionDefinitionNames;
	}
	
}
