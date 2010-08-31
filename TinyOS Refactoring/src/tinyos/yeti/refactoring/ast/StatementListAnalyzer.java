package tinyos.yeti.refactoring.ast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tinyos.yeti.nesc12.parser.ast.nodes.expression.AssignmentExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PostfixExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PrefixExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.UnaryOperator.Operator;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.Statement;
import tinyos.yeti.refactoring.RefactoringInfo;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.Filter;

public class StatementListAnalyzer extends AstAnalyzer {
	private RefactoringInfo info;
	private List<Statement> statements;
	protected ASTUtil4Variables varUtil;
	
	public StatementListAnalyzer(List<Statement> statements, RefactoringInfo info){
		super();
		this.info = info;
		this.statements = new LinkedList<Statement>(statements);
		varUtil = new ASTUtil4Variables(getASTUtil());
	}
	
	public Set<VariableDeclaration> getTopLevelDeclarations() {
		return VariableDeclaration.getTopLevelDeclarations(statements,getInfo());
	}
	

	
	protected RefactoringInfo getInfo(){
		return info;
	}
	
	/**
	 * Fields that are defined within the Statements to extract, but are still
	 * visible after the End of the selection (so highest level declarations)
	 * are not filterd out.
	 */
	public Set<String> getPotentionalyChangedLocalVariables() {
		
		Filter<Identifier> filter = new Filter<Identifier>() {
			@Override
			public boolean test(Identifier id) {
				return varUtil.isLocalVariableOrFunctionParameter(id)
						&& isModifing(id);
			}
		};

		Set<Identifier> ids = varUtil.exploreVariableNamespace(statements, filter,info);
		Set<String> names = varUtil.getNames(ids);
		return names;
	}
	
	private boolean isModifing(Identifier id) {
		boolean ret = false;

		// Assignment Operation x=4 or x += 4
		AssignmentExpression ass = (AssignmentExpression) astUtil
				.getParentForName(id, AssignmentExpression.class);
		if (ass != null) {
			Expression potentialIdentifierExpression = ass.getVariable();
			if (potentialIdentifierExpression instanceof IdentifierExpression) {
				IdentifierExpression identifierExpression = (IdentifierExpression) potentialIdentifierExpression;
				ret |= identifierExpression.getIdentifier().equals(id);
			}
		}

		// Postfix Operator ++/--
		// TODO seems to have problems with brakes
		PostfixExpression pfe = (PostfixExpression) astUtil.getParentForName(
				id, PostfixExpression.class);
		if (pfe != null) {
			if (pfe.getExpression() instanceof IdentifierExpression) {
				if (((IdentifierExpression) pfe.getExpression())
						.getIdentifier().equals(id)) {
					// The only Postfix Operators are Increment and Decrement
					ret = true;
				}
			}
		}

		// Prefix Operator ++/--/& (& makes it horriblely complex. I just
		// assume: occurring & => modified)
		PrefixExpression prefe = (PrefixExpression) astUtil.getParentForName(
				id, PrefixExpression.class);
		if (prefe != null) {
			if (prefe.getExpression() instanceof IdentifierExpression) {
				if (((IdentifierExpression) prefe.getExpression())
						.getIdentifier().equals(id)) {
					Operator op = prefe.getOperator().getOperator();
					// Other Operators does'nt change the Variable like */-..
					ret |= Arrays.asList(
							new Operator[] { Operator.ADDRESS,
									Operator.INCREMENT, Operator.DECREMENT })
							.contains(op);
				}
			}
		}

		return ret;
	}
	
	public Set<String> getInternalyUnusedDeclarations() {
		Set<String> ret = new HashSet<String>();

		for (VariableDeclaration dec : getTopLevelDeclarations()) {
			List<Statement> list = new LinkedList<Statement>(statements);
			list.remove(dec.getAstNode());
			for (String var : dec.getVariableNames()) {
				Set<Identifier> ids = varUtil.exploreVariableNamespace(list,
						new Filter<Identifier>() {

							@Override
							public boolean test(Identifier toTest) {
								return (new ASTUtil4Variables())
										.isLocalVariableOrFunctionParameter(toTest);
							}
						},info);
				if (!varUtil.getNames(ids).contains(var)) {
					ret.add(var);
				}
			}
		}

		return ret;
	}

}
