package tinyos.yeti.refactoring.ast;

import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;

public class ComponentAstAnalyser extends AstAnalyzer {
	
	protected TranslationUnit root;
	protected Identifier componentIdentifier;
	protected AccessList specification;
	
	public ComponentAstAnalyser(TranslationUnit root,Identifier componentIdentifier, AccessList specification) {
		super();
		this.root = root;
		this.componentIdentifier = componentIdentifier;
		this.specification = specification;
	}
	
	
	
}
