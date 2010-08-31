package tinyos.yeti.refactoring.ast;


import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.DoWhileStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ForStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.Statement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.WhileStatement;
import tinyos.yeti.refactoring.RefactoringInfo;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.Filter;

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
	
	/**
	 * Returns a Set of VariablesDeclarations that are made within this CompoundStatement.
	 * Variable Declarations in sub-compoundStatements are not included.
	 * @return
	 */
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
	

	/**
	 * Tells whether this Compound statement is a Function Body
	 * @return
	 */
	public boolean isHighestLocalScope() {
		return cs.getParent() instanceof FunctionDefinition;
	}
	

	/**
	 * Tells wether this CompoundStatement is the Body of a loop Statement like for,while or do..while
	 * @return
	 */
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
	
	/**
	 * Finds all used Variables after the Execution of afterExecutionOf
	 * @param namesToCheck
	 * @param afterExecutionOf
	 * @return
	 */
	public Set<String> getReadLocalVariablesAfter(
			Set<String> namesToCheck,Statement afterExecutionOf) {
		// Which names to check are defined in this cs => only occurrences of
		// those within cs after end are interesting
		namesToCheck = new HashSet<String>(namesToCheck);
		Set<String> localyDefined = getLocalyDefinedVariableNames();
		Set<String> ret = getLocalyReadVariables(namesToCheck,afterExecutionOf);
		namesToCheck.removeAll(localyDefined);
		namesToCheck.remove(ret);

		if (!isHighestLocalScope() && namesToCheck.size() > 0) {
			CompoundStatement parentCs = astUtil.getParentForName(cs,
					CompoundStatement.class); 
			CompoundStatementAnalyzer parentCsa= new CompoundStatementAnalyzer(parentCs, getInfo());
			ret.addAll(parentCsa.getReadLocalVariablesAfter(namesToCheck,
					parentCs));
		}

		return ret;
	}
	

	private Set<String> getLocalyReadVariables(final Set<String> namesToCheck, Statement afterExecutionOf) {
		int startPos = 0;
		if(afterExecutionOf != null){
			startPos = getInfo().getAstPositioning().end(afterExecutionOf);
		}
		
		Collection<Statement> childs = new LinkedList<Statement>();
		for (ASTNode c : astUtil.getChilds(cs)) {
			// Childs of CompoundStatement are always Statements
			childs.add((Statement) c);
		}

		// For Loops, commands before the selection might be executed after the
		// selection was executed.
		// Thats why we also look at commands before the Selection.
		// if it's not a loop, the commands before and in the Selection get
		// deleted.
		if (!isLoop()) {
			LinkedList<ASTNode> toDel = new LinkedList<ASTNode>();
			for (ASTNode child : childs) {
				if (getInfo().getAstPositioning().start(child) < startPos) {
					toDel.add(child);
				}
			}
			childs.removeAll(toDel);
		}

		// Find all identifiers. If they are local Variables and contain in the
		// namesToCheck,
		// Then they occur after the execution of the Area to Extract.
		LinkedList<Statement> nodesToCheck = new LinkedList<Statement>();
		nodesToCheck.addAll(childs);
		// Set<String> readVars = new HashSet<String>();
		Filter<Identifier> filter = new Filter<Identifier>() {
			@Override
			public boolean test(Identifier toTest) {
				return varUtil.isLocalVariableOrFunctionParameter(toTest)
						&& namesToCheck.contains(toTest.getName());
			}
		};

		Set<Identifier> ids = varUtil.exploreVariableNamespace(nodesToCheck,
				filter, getInfo());
		return varUtil.getNames(ids);
	}

}
