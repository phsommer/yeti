package tinyos.yeti.refactoring.entities.functionparameter.rename.c;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.NesCNameDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.abstractrefactoring.rename.SelectionIdentifier;
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
	private boolean isInFunctionDeclarationParameterList(){
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
	private boolean isInFunctionDefinitionParameterList(){
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
	private boolean isFunctionParameterInFunctionBody(){
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
		return isCFunctionParameter()
			||isNesCFunctionParameter();
	}
	
	/**
	 * Checks if the given identifier is a identifier in a function parameter list of a C function declaration.
	 * @param identifier
	 * @return
	 */
	public boolean isInCFunctionDeclarationParameterList(){
		if(!isInFunctionDeclarationParameterList()){
			return false;
		}
		return !isInNesCFunctionDeclarationParameterList();
	}
	
	/**
	 * Checks if the given identifier is a identifier in a function parameter list of a C function definition.
	 * @param identifier
	 * @return
	 */
	public boolean isInCFunctionDefinitionParameterList(){
		if(!isInFunctionDefinitionParameterList()){
			return false;
		}
		FunctionDeclarator declarator=astUtil.getParentForName(identifier, FunctionDeclarator.class);
		return !isPartOfNesCFunctionDefinition(declarator);
	}
	
	/**
	 * Checks if the given identifier is a identifier in the body of a C function definition which references a function parameter.
	 * @param identifier
	 * @return
	 */
	public boolean isCFunctionParameterInFunctionBody(){
		if(!isFunctionParameterInFunctionBody()){
			return false;
		}
		FunctionDefinition definition=astUtil.getParentForName(identifier, FunctionDefinition.class);
		FunctionDeclarator declarator=astUtil4Functions.getFunctionDeclarator(definition);
		return !isPartOfNesCFunctionDefinition(declarator);
	}
	
	/**
	 * Checks if the given identifier is a functionParameter.
	 * @param identifier
	 * @return
	 */
	public boolean isCFunctionParameter(){
		return isInCFunctionDeclarationParameterList()
			||isInCFunctionDefinitionParameterList()
			||isCFunctionParameterInFunctionBody();
	}

	
	/**
	 * Checks if the given identifier is a identifier in a function parameter list of a NesC function declaration.
	 * @param identifier
	 * @return
	 */
	public boolean isInNesCFunctionDeclarationParameterList(){
		if(!factory4Selection.hasInterfaceAnalyzerCreated()){
			return false;
		}
		return isInFunctionDeclarationParameterList();
	}
	
	/**
	 * Checks if the given identifier is a identifier in a function parameter list of a NesC function definition.
	 * @param identifier
	 * @return
	 */
	public boolean isInNesCFunctionDefinitionParameterList(){
		if(!isInFunctionDefinitionParameterList()){
			return false;
		}
		FunctionDeclarator declarator=astUtil.getParentForName(identifier, FunctionDeclarator.class);
		return isPartOfNesCFunctionDefinition(declarator);
	}
	
	/**
	 * Checks if the given identifier is a identifier in the body of a function definition which references a NesC function parameter.
	 * @param identifier
	 * @return
	 */
	public boolean isNesCFunctionParameterInFunctionBody(){
		if(!isFunctionParameterInFunctionBody()){
			return false;
		}
		FunctionDefinition definition=astUtil.getParentForName(identifier, FunctionDefinition.class);
		FunctionDeclarator declarator=astUtil4Functions.getFunctionDeclarator(definition);
		return isPartOfNesCFunctionDefinition(declarator);
	}
	
	
	/**
	 * Checks if the given identifier is a functionParameter.
	 * @param identifier
	 * @return
	 */
	public boolean isNesCFunctionParameter(){
		return isInNesCFunctionDeclarationParameterList()
			||isInNesCFunctionDefinitionParameterList()
			||isNesCFunctionParameterInFunctionBody();
	}

	/**
	 * Checks if the given declarator is part of a NesC function definition.
	 * @param declarator
	 * @return
	 */
	public boolean isPartOfNesCFunctionDefinition(FunctionDeclarator declarator){
		ASTNode node=declarator.getField(FunctionDeclarator.DECLARATOR);
		return node instanceof NesCNameDeclarator;
	}
}
