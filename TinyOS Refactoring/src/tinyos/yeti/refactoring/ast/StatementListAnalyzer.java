package tinyos.yeti.refactoring.ast;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tinyos.yeti.nesc12.parser.ast.nodes.statement.Statement;
import tinyos.yeti.refactoring.RefactoringInfo;

public class StatementListAnalyzer extends AstAnalyzer {
	private RefactoringInfo info;
	private List<Statement> statements;
	
	public StatementListAnalyzer(List<Statement> statements, RefactoringInfo info){
		super();
		this.info = info;
		this.statements = new LinkedList<Statement>(statements);
	}
	
	public Set<VariableDeclaration> getTopLevelDeclarations() {
		return VariableDeclaration.getTopLevelDeclarations(statements,getInfo());
	}
	

	
	protected RefactoringInfo getInfo(){
		return info;
	}
}
