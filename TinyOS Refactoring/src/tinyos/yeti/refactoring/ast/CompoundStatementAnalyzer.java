package tinyos.yeti.refactoring.ast;


import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.DoWhileStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ForStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.Statement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.WhileStatement;
import tinyos.yeti.refactoring.RefactoringInfo;
import tinyos.yeti.refactoring.utilities.ASTUtil;

public class CompoundStatementAnalyzer extends StatementListAnalyzer {
	CompoundStatement cs;
	
	public CompoundStatementAnalyzer(CompoundStatement cs, RefactoringInfo info) {
		super(getChild(cs),info);
		this.cs=cs;
	}
	
	private static List<Statement> getChild(CompoundStatement cs){
		LinkedList<Statement> ret = new LinkedList<Statement>();
		for(ASTNode n: (new ASTUtil()).getChilds(cs)){
			ret.add((Statement) n);
		}
		return ret;
	}
	
	public Set<VariableDeclaration> getLocalVariableDeclarations() {
		Collection<ASTNode> nodesToCheckForDeclarations = new LinkedList<ASTNode>();

		// For Loop header Declarations
		ASTNode parent = cs.getParent();
		if (parent instanceof ForStatement) {
			ForStatement loop = (ForStatement) parent;
			nodesToCheckForDeclarations.add(loop.getInit());
		}

		// For FunctionDeclarations
		if (parent instanceof FunctionDefinition) {
			FunctionDefinition funcDef = (FunctionDefinition) parent;
			Collection<ASTNode> defs = astUtil.getChilds(funcDef
					.getDeclarator().getFunction().getParameters());
			nodesToCheckForDeclarations.addAll(defs);
		}
		
		Set<VariableDeclaration> declarations = VariableDeclaration.getTopLevelDeclarations(nodesToCheckForDeclarations,getInfo());
		declarations.addAll(getTopLevelDeclarations());
		
		return declarations;
	}
	

	public boolean isHighestLocalScope() {
		return cs.getParent() instanceof FunctionDefinition;
	}
	

	@SuppressWarnings("unchecked")
	public boolean isLoop() {
		ASTNode parent = cs.getParent();
		Class[] loopSatements = new Class[] { ForStatement.class,
				WhileStatement.class, DoWhileStatement.class };
		boolean ret = false;
		for (Class type : loopSatements) {
			ret = ret || type.isInstance(parent);
		}
		return ret;
	}
	
	/**
	 * Returns the Names of the Variables that are defined in the given
	 * CompoundStatement
	 */
	public Set<String> getLocalyDefinedVariableNames() {
		Set<String> ret = new HashSet<String>();
		for (VariableDeclaration d : getLocalVariableDeclarations()) {
			ret.addAll(d.getVariableNames());
		}

		return ret;
	}

}
