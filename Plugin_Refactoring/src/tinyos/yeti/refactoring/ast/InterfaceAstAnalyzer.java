package tinyos.yeti.refactoring.ast;

import java.util.Collection;

import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declaration;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Datadef;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.DatadefList;

public class InterfaceAstAnalyzer extends NesCAstAnalyzer {

	private DatadefList body;
	
	private Collection<Identifier> nesCFunctionIdentifiers;
	
	public InterfaceAstAnalyzer(TranslationUnit root,Identifier interfacIdentifier, DatadefList body) {
		super(root,interfacIdentifier);
		this.body=body;
	}
	
	/**
	 * Returns the name identifiers of all NesC functions in this interface.
	 * @return
	 */
	public Collection<Identifier> getNesCFunctionIdentifiers(){
		if(nesCFunctionIdentifiers==null){
			 Collection<Datadef> datadefs=astUtil.getChildsOfType(body,Datadef.class);
			 Collection<Declaration> declarations=astUtil.collectFieldsWithName(datadefs, Datadef.DECLARATION);
			 nesCFunctionIdentifiers=depackDeclarationsAndGetNesCFunctionIdentifiers(declarations);
		}
		return nesCFunctionIdentifiers;
	}
}
