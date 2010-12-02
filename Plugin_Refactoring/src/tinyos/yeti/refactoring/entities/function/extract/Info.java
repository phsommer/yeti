package tinyos.yeti.refactoring.entities.function.extract;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.Statement;
import tinyos.yeti.refactoring.RefactoringInfo;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.ast.StatementListAnalyzer;

public class Info extends RefactoringInfo{

	private String functionName;
	private List<Statement> statementsToExtract = null;
	
	
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
		if(statementsToExtract != null){
			// Createing a new LinkedList prevents from caller changing local List 
			return new LinkedList<Statement>(statementsToExtract);
		}
		int begin = getOrigSelectionBegin();
		int end = getOrigSelectionEnd();
		ASTPositioning astPos = getAstPositioning();
		LinkedList<Statement> ret = new LinkedList<Statement>();

		CompoundStatement compSt = astPos.getDeepedstSuperCompoundSuperstatement(begin);
		if(compSt == null){
			return Collections.emptyList();
		}

		for (int i = 0; i < compSt.getChildrenCount()
				&& astPos.start(compSt.getChild(i)) < end; i++) {
			ASTNode statement = compSt.getChild(i);
			// If just a small part of the Statement is selected, the whole statement gets extracted.
			if (astPos.end(statement) > begin) {
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
		
		this.statementsToExtract = ret;
		return new LinkedList<Statement>(ret);
	}
	
	public StatementListAnalyzer getStatementsToExtractAnalyzer(){
		return new StatementListAnalyzer(getStatementsToExtract(), this);
	}
	
	public boolean isVaidSelection(){
		try{
			getStatementsToExtract();
		}catch (IllegalStateException e) {
			return false;
		}
		return true;
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
