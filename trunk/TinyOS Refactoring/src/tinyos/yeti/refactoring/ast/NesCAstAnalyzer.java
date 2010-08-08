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
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class NesCAstAnalyzer extends AstAnalyzer {
	
	protected TranslationUnit root;
	protected Identifier entityIdentifier;
	
	public NesCAstAnalyzer(TranslationUnit root, Identifier entityIdentifier) {
		super();
		this.root = root;
		this.entityIdentifier = entityIdentifier;
	}
	
	/**
	 * Returns the name identifier of this component/interface.
	 * @return
	 */
	public Identifier getEntityIdentifier(){
		return entityIdentifier;
	}
	
	/**
	 * Returns the name of this component/interface.
	 * @return
	 */
	public String getEntityName(){
		return entityIdentifier.getName();
	}
	
	/**
	 * Collects all NesC function identifiers out of a collection of declarations.
	 * @param declarations
	 * @return
	 */
	protected Collection<Identifier> depackDeclarationsAndGetNesCFunctionIdentifiers(Collection<Declaration> declarations){
		Collection<InitDeclaratorList> initLists=astUtil.collectFieldsWithName(declarations, Declaration.INIT_LIST);
		Collection<InitDeclarator> initDeclarators=new LinkedList<InitDeclarator>();
		for(InitDeclaratorList initList:initLists){
			InitDeclarator declarator=astUtil.getFirstChildOfType(initList, InitDeclarator.class);
			if(declarator!=null){
				initDeclarators.add(declarator);
			}
		}
		Collection<FunctionDeclarator> declarators=new LinkedList<FunctionDeclarator>();
		for(InitDeclarator declarator:initDeclarators){
			ASTNode child=declarator.getField(InitDeclarator.DECLARATOR);
			if(child!=null){
				if(child instanceof PointerDeclarator){	//Functions which return a pointer have an additional layer in between.
					child=((PointerDeclarator)child).getField(PointerDeclarator.DECLARATOR);
				}
				declarators.add((FunctionDeclarator)child);
			}
		}
		Collection<DeclaratorName> declaratorNames=astUtil.collectFieldsWithName(declarators, FunctionDeclarator.DECLARATOR);
		return astUtil.collectFieldsWithName(declaratorNames, DeclaratorName.NAME);
	}
	
	
}
