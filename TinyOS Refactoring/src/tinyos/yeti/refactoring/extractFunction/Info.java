package tinyos.yeti.refactoring.extractFunction;

import java.util.LinkedList;
import java.util.List;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.Statement;
import tinyos.yeti.refactoring.RefactoringInfo;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;

public class Info extends RefactoringInfo{

	private String functionName;
	
	
	public Info(NesCEditor editor) {
		super(editor, "Extract Infos", "Extract Function Wizzard");
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public String getFunctionName() {
		return functionName;
	}
	
	/**
	 * Returns the Statements that are selected to be extracted.
	 * It is guaranteed that at least one Statement is selected. Otherwise
	 * @throws IllegalStateException If no statement is selected to be extracted.
	 */
	public List<Statement> getStatementsToExtract() {
		int begin = getOrigSelectionBegin();
		int end = getOrigSelectionEnd();
		ASTPositioning util = getAstPositioning();
		LinkedList<Statement> ret = new LinkedList<Statement>();

		CompoundStatement compSt = getAstPositioning().getDeepedstSuperCompoundSuperstatement(begin);

		for (int i = 0; i < compSt.getChildrenCount()
				&& util.end(compSt.getChild(i)) <= end; i++) {
			ASTNode statement = compSt.getChild(i);
			if (util.start(statement) >= begin) {
				if (!(statement instanceof Statement)) {
					throw new RuntimeException(
							"Found a non Statement as Child of a Compound Statement. It was a "
									+ statement.getClass().getCanonicalName());
				}
				ret.add((Statement) statement);
			}
		}
		if(ret.size() < 1){
			throw new IllegalStateException("At least one Statement has to be Selected.");
		}

		return ret;
	}
	
	/**
	 * Give the Position just before the fist selected Statement.
	 */
	public int getSelectionBegin(){
		LinkedList<Statement> statements = new LinkedList<Statement>(getStatementsToExtract());
		return getAstPositioning().start(statements.getFirst());
	}
	
	/**
	 * Give the Position just after the Last selected Statement.
	 */
	public int getSelectionEnd(){
		LinkedList<Statement> statements = new LinkedList<Statement>(getStatementsToExtract());
		return getAstPositioning().end(statements.getLast());
	}
	
	
	public int getOrigSelectionBegin(){
		return getSelection().getOffset();
	}
	
	
	public int getOrigSelectionEnd(){
		return getOrigSelectionBegin()+getSelection().getLength();
	}

}
