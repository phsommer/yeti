package tinyos.yeti.refactoring.ast;

import java.util.Collection;
import java.util.LinkedList;

import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.RefComponent;

public class ConfigurationAstAnalyzer extends ComponentAstAnalyser {

	private ConfigurationDeclarationList implementation;

	public ConfigurationAstAnalyzer(TranslationUnit root,Identifier componentIdentifier, AccessList specification,ConfigurationDeclarationList implementation) {
		super(root, componentIdentifier, specification);
		this.implementation = implementation;
	}
	
	public Collection<RefComponent> getAllComponentDeclarations(){
		Collection<RefComponent> components=new LinkedList<RefComponent>();
		return null;
		
	}
	
	
	
}
