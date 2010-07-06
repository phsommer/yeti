package tinyos.yeti.refactoring.renameFunction;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.FunctionCall;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ASTUtil;

public class ASTUtil4Functions {
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionCallAncestorSequence=new Class[]{
		IdentifierExpression.class,
		FunctionCall.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionDefinitionAncestorSequence=new Class[]{
		DeclaratorName.class,
		FunctionDeclarator.class,
		FunctionDefinition.class
	};
	
	/**
	 * Tests if the given identifier is the name of the function, in a function call.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function call. False otherwise.
	 */
	public static boolean isFunctionCall(Identifier identifier){
		return ASTUtil.checkAncestorSequence(identifier,functionCallAncestorSequence);	
	}
	
	/**
	 * Tests if the given identifier is the name of the function, in a function definition.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function definition. False otherwise.
	 */
	public static boolean isFunctionDefinition(Identifier identifier){
		return ASTUtil.checkAncestorSequence(identifier,functionDefinitionAncestorSequence);
	}
	
	
	
}
