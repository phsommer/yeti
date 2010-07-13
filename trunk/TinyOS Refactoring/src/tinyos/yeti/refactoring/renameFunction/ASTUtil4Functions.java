package tinyos.yeti.refactoring.renameFunction;

import java.util.Collection;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.FunctionCall;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ASTUtil;

public class ASTUtil4Functions {
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionDeclarationAncestorSequence=new Class[]{
		DeclaratorName.class,
		FunctionDeclarator.class,
		InitDeclarator.class
		
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionDefinitionAncestorSequence=new Class[]{
		DeclaratorName.class,
		FunctionDeclarator.class,
		FunctionDefinition.class
	};
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends ASTNode>[] functionCallAncestorSequence=new Class[]{
		IdentifierExpression.class,
		FunctionCall.class
	};
	
	/**
	 * Tests if the given identifier is the name of the function, in a function declaration.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function declaration. False otherwise.
	 */
	public static boolean isFunctionDeclaration(Identifier identifier){
		return ASTUtil.checkAncestorSequence(identifier,functionDeclarationAncestorSequence);
	}
	
	/**
	 * Tests if the given identifier is the name of the function, in a function definition.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function definition. False otherwise.
	 */
	public static boolean isFunctionDefinition(Identifier identifier){
		return ASTUtil.checkAncestorSequence(identifier,functionDefinitionAncestorSequence);
	}
	
	/**
	 * Tests if the given identifier is the name of the function, in a function call.
	 * @param identifier The identifier which is in question.
	 * @return true if the given identifier is the name of the function, in a function call. False otherwise.
	 */
	public static boolean isFunctionCall(Identifier identifier){
		return ASTUtil.checkAncestorSequence(identifier,functionCallAncestorSequence);	
	}
	
	/**
	 * Evaluates the FunctionPartType of which this Identifier relates. 
	 * @param identifier
	 * @return
	 */
	public static FunctionPart identifyFunctionPart(Identifier identifier){
		if(isFunctionDeclaration(identifier)){
			return FunctionPart.DECLARATION;
		}else if(isFunctionDefinition(identifier)){
			return FunctionPart.DEFINITION;
		}else if(isFunctionCall(identifier)){
			return FunctionPart.CALL;
		}
		return FunctionPart.NO_FUNCTION_PART;
	}
	
	/**
	 * Tests if the given identifier is the name of a function.
	 * @param identifier The identifier which is in question.
	 * @return True if the given identifier is the name of a function. False otherwise and especially if the given identifier is NULL. 
	 */
	public static boolean isFunction(Identifier identifier){
		if(identifier==null){
			return false;
		}
		return 	identifyFunctionPart(identifier)!=FunctionPart.NO_FUNCTION_PART;
	}
	
	
	
	
}
