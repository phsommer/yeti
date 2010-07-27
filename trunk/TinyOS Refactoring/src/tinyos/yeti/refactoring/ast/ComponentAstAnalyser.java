package tinyos.yeti.refactoring.ast;

import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;

public class ComponentAstAnalyser {
	
	private TranslationUnit root;
	private Identifier componentIdentifier;
	private AccessList specification;
	
	public ComponentAstAnalyser(TranslationUnit root,Identifier componentIdentifier, AccessList specification) {
		super();
		this.root = root;
		this.componentIdentifier = componentIdentifier;
		this.specification = specification;
	}
	
	
	
}
