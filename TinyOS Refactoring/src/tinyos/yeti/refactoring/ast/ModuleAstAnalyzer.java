package tinyos.yeti.refactoring.ast;

import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;

public class ModuleAstAnalyzer extends ComponentAstAnalyser {

	private NesCExternalDefinitionList implementation;

	public ModuleAstAnalyzer(TranslationUnit root,Identifier componentIdentifier, AccessList specification,NesCExternalDefinitionList implementation) {
		super(root, componentIdentifier, specification);
		this.implementation = implementation;
	}
	
	
	
}
