package tinyos.yeti.refactoring.selection;

import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;

public class FunctionParameterSelectionIdentifier extends SelectionIdentifier {
	
	private ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables(astUtil);
	private ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions(astUtil);

	public FunctionParameterSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}

	public FunctionParameterSelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory) {
		super(identifier, analyzerFactory);
	}
	
	/**
	 * Checks if the given identifier is a identifier in a function parameter list of a function declaration.
	 * @param identifier
	 * @return
	 */
	public boolean isInFunctionDeclarationParameterList(){
		if(isInFunctionDefinitionParameterList()){	//Without this test the result was also true if it was a parameter of a functionDefinition.
			return false;
		}
		FunctionDeclarator declarator=astUtil.getParentForName(identifier, FunctionDeclarator.class);
		if(declarator==null){
			return false;
		}
		return astUtil4Functions.isInFunctionDeclaratorParameterList(identifier, declarator);
	}
	
	/**
	 * Checks if the given identifier is a identifier in a function parameter list of a function definition.
	 * @param identifier
	 * @return
	 */
	public boolean isInFunctionDefinitionParameterList(){
		FunctionDefinition definition=astUtil.getParentForName(identifier, FunctionDefinition.class);
		if(definition==null){
			return false;
		}
		FunctionDeclarator declarator=astUtil4Functions.getFunctionDeclarator(definition);
		if(declarator==null){
			return false;
		}
		return astUtil4Functions.isInFunctionDeclaratorParameterList(identifier, declarator);
	}
	
	/**
	 * Checks if the given identifier is a identifier in the body of a function definition which references a function parameter.
	 * @param identifier
	 * @return
	 */
	public boolean isFunctionParameterInFunctionBody(){
		if(astUtil4Variables.isLocalVariable(identifier)){
			return false;
		}
		FunctionDefinition definition=astUtil.getParentForName(identifier, FunctionDefinition.class);
		if(definition==null){
			return false;
		}
		FunctionDeclarator declarator=astUtil4Functions.getFunctionDeclarator(definition);
		Integer index=astUtil4Functions.getIndexOfParameterWithName(identifier.getName(), declarator);
		return index!=null;
	}
	
	/**
	 * Checks if the given identifier is a functionParameter.
	 * @param identifier
	 * @return
	 */
	public boolean isFunctionParameter(){
		return isInFunctionDeclarationParameterList()
			||isInFunctionDefinitionParameterList()
			||isFunctionParameterInFunctionBody();
	}
	
	

}
