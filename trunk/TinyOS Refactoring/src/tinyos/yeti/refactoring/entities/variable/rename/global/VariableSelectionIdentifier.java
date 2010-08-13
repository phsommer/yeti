package tinyos.yeti.refactoring.entities.variable.rename.global;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ArgumentExpressionList;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ArithmeticExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.AssignmentExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PostfixExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PrefixExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ExpressionStatement;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.rename.SelectionIdentifier;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;

public class VariableSelectionIdentifier extends SelectionIdentifier{

	private ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables(astUtil);
	
	public VariableSelectionIdentifier(Identifier identifier) {
		super(identifier);
	}

	public VariableSelectionIdentifier(Identifier identifier,AstAnalyzerFactory analyzerFactory) {
		super(identifier, analyzerFactory);
	}
	
	/**
	 * Checks if this identifier is part of a variable reference in a function body.
	 * @param identifier
	 * @return
	 */
	private boolean isVariableUsage(Identifier identifier){
		ASTNode parent=identifier.getParent();
		if(!(parent instanceof IdentifierExpression)){
			return false;
		}
		parent=parent.getParent();
		return parent instanceof AssignmentExpression
			||parent instanceof ArgumentExpressionList
			||parent instanceof ArithmeticExpression
			||parent instanceof PrefixExpression
			||parent instanceof PostfixExpression
			||parent instanceof ExpressionStatement;
	}
	
	/**
	 * Checks if the given identifier represents a global variable.
	 * @return
	 */
	public boolean isGlobalVariable(){
		return isGlobalVariableDeclaration()
			||isGlobalVariableReference();
	}
	
	/**
	 * Checks if this identifier represents a global variable declaration.
	 * @param identifier
	 * @return
	 */
	public boolean isGlobalVariableDeclaration(){
		if(!factory4Selection.hasNesCAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,cAnalyzer.getGlobalVariableDeclarationNames());
	}
	
	/**
	 * Checks if this identifier represents a variable reference.
	 * @param identifier
	 * @return
	 */
	public boolean isGlobalVariableReference(){
		if(!factory4Selection.hasNesCAnalyzerCreated()){
			return false;
		}
		if(!cAnalyzer.getGlobalVariableDeclarationNames().contains(identifier)){
			return false;
		}
		if(astUtil4Variables.isLocalVariable(identifier)){
			return false;
		}
		return isVariableUsage(identifier);
	}
	
	
	
	/**
	 * Checks if this identifier is part of a variable declaration.
	 * @param identifier
	 * @return
	 */
	public boolean isImplementationLocalVariableDeclaration(){
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		return astUtil.containsIdentifierInstance(identifier,moduleAnalyzer.getImplementationLocalVariableDeclarationNames());
	}
	
	/**
	 * Checks if this identifier is part of a variable reference in a function body.
	 * @param identifier
	 * @return
	 */
	public boolean isImplementationLocalVariableUsage(){
		if(!isVariableUsage(identifier)){
			return false;
		}
		if(astUtil4Variables.isLocalVariable(identifier)){
			return false;
		}
		if(!factory4Selection.hasModuleAnalyzerCreated()){
			return false;
		}
		return moduleAnalyzer.getImplementationLocalVariableDeclarationNames().contains(identifier);
	}
	
	/**
	 * Checks if the given identifier represents a implementation local variable.
	 * @return
	 */
	public boolean isImplementationLocalVariable(){
		return isImplementationLocalVariableDeclaration()
			||isImplementationLocalVariableUsage();
	}
}
