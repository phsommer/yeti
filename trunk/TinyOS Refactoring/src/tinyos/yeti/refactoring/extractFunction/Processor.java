package tinyos.yeti.refactoring.extractFunction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclaratorList;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.AssignmentExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PostfixExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PrefixExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.UnaryOperator.Operator;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.DoWhileStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ForStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.Statement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.WhileStatement;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.Filter;

public class Processor extends RefactoringProcessor {

	private Info info;
	private ASTUtil astUtil;
	private ASTUtil4Variables varUtil;

	public Processor(Info info) {
		this.info = info;
		this.astUtil = new ASTUtil();
		this.varUtil = new ASTUtil4Variables(astUtil);
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws CoreException,
			OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		return status;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		int begin = info.getSelectionBegin();
		int end = info.getSelectionEnd();
		if (!isInSameCompoundStatement(begin, end)) {
			status
					.addFatalError("Begin and End of your Selection have to be within the same Block to allow the Extract Function Refactoring");
		}

		return status;
	}

	/**
	 * To extract a Function the whole extracted Area should be part of one
	 * Block. Otherwise you would extract eg. half of a loop body. This Method
	 * tests wether begin and end are in the same Block.
	 */
	private boolean isInSameCompoundStatement(int begin, int end) {
		// System.err.println("Begin:"+begin+" End:"+end);
		ASTNode beginNode = info.getAstPositioning().getDeepestAstNodeAtPos(
				begin);
		ASTNode endNode = info.getAstPositioning().getDeepestAstNodeAtPos(end);

		CompoundStatement containingBegin = null;
		if (beginNode instanceof CompoundStatement) {
			containingBegin = (CompoundStatement) beginNode;
		} else {
			containingBegin = (CompoundStatement) astUtil.getParentForName(
					beginNode, CompoundStatement.class);
		}

		CompoundStatement containingEnd = null;
		if (endNode instanceof CompoundStatement) {
			containingEnd = (CompoundStatement) endNode;
		} else {
			containingEnd = (CompoundStatement) astUtil.getParentForName(
					endNode, CompoundStatement.class);
		}

		return containingBegin != null && containingBegin.equals(containingEnd);
	}

	private CompoundStatement getDeepedstCompoundSuperstatement() {
		int begin = info.getSelectionBegin();

		ASTNode beginNode = info.getAstPositioning().getDeepestAstNodeAtPos(
				begin);

		CompoundStatement ret = null;
		if (beginNode instanceof CompoundStatement) {
			ret = (CompoundStatement) beginNode;
		} else {
			ret = (CompoundStatement) astUtil.getParentForName(beginNode,
					CompoundStatement.class);
		}
		return ret;
	}

	private List<Statement> getStatementsToExtract() {
		int begin = info.getSelectionBegin();
		int end = info.getSelectionEnd();
		ASTPositioning util = info.getAstPositioning();
		LinkedList<Statement> ret = new LinkedList<Statement>();

		CompoundStatement compSt = getDeepedstCompoundSuperstatement();

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

		return ret;
	}

	private Set<String> getReadLocalVariablesAfterArea2Extract(
			Set<String> namesToCheck, CompoundStatement cs) {
		// Which names to check are defined in this cs => only occurrences of
		// those within cs after end are interesting
		namesToCheck = new HashSet<String>(namesToCheck);
		Set<String> localyDefined = getLocalyDefinedVariables(cs);
		Set<String> ret = getLocalyReadAfterEnd(cs, namesToCheck);
		namesToCheck.removeAll(localyDefined);
		namesToCheck.remove(ret);

		if (!isHighestLocalScope(cs) && namesToCheck.size() > 0) {
			ret.addAll(getReadLocalVariablesAfterArea2Extract(namesToCheck,
					(CompoundStatement) astUtil.getParentForName(cs,
							CompoundStatement.class)));
		}

		return ret;
	}

	private Set<String> getLocalyReadAfterEnd(CompoundStatement cs,
			final Set<String> namesToCheck) {
		
		Collection<Statement> childs = new LinkedList<Statement>();
		for(ASTNode c : astUtil.getChilds(cs)){
			// Childs of CompoundStatement are always Statements
			 childs.add((Statement) c);
		}

		// For Loops, commands before the selection might be executed after the
		// selection was executed.
		// Thats why we also look at commands before the Selection.
		// if it's not a loop, the commands before and in the Selection get
		// deleted.
		if (!isLoop(cs)) {
			LinkedList<ASTNode> toDel = new LinkedList<ASTNode>();
			for (ASTNode child : childs) {
				if (info.getAstPositioning().start(child) < info
						.getSelectionEnd()) {
					toDel.add(child);
				}
			}
			childs.removeAll(toDel);
		}

		// Find all identifiers. If they are local Variables and contain in the
		// namesToCheck,
		// Then they occure after the execution of the Area to Extract.
		LinkedList<Statement> nodesToCheck = new LinkedList<Statement>();
		nodesToCheck.addAll(childs);
		// Set<String> readVars = new HashSet<String>();
		Filter<Identifier> filter = new Filter<Identifier>() {
			@Override
			public boolean test(Identifier toTest) {
				return varUtil.isLocalVariable(toTest)
						&& namesToCheck.contains(toTest.getName());
			}
		};

		return exploreVariableNamespace(nodesToCheck, filter);
	}

	/**
	 * Returns the Names of the Variables that are defined in the given
	 * CompoundStatement
	 */
	private Set<String> getLocalyDefinedVariables(ASTNode cs) {
		Set<String> ret = new HashSet<String>();

		Collection<ASTNode> nodesToCheckForDeclarations = new LinkedList<ASTNode>();

		// All Statements in the Compound Statement
		nodesToCheckForDeclarations.addAll(astUtil.getChilds(cs));

		// For Loop header Declarations
		ASTNode parent = cs.getParent();
		if (parent instanceof ForStatement) {
			ForStatement loop = (ForStatement) parent;
			nodesToCheckForDeclarations.add(loop.getInit());
		}

		for (ASTNode child : nodesToCheckForDeclarations) {
			if (isDeclaration(child)) {
				Declaration d = (Declaration) child;
				ret.addAll(getDeclaredVariableNames(d));
			}
		}

		return ret;
	}
	
	private boolean isDeclaration(ASTNode node){
		return (node instanceof Declaration);
	}
	
	private Set<String> getDeclaredVariableNames(Declaration d){
		Set<String> ret = new HashSet<String>();
		for (int j = 0; d.getInitlist().getChildrenCount() > j; j++) {
			InitDeclaratorList idl = d.getInitlist();
			Declarator declarator = idl.getNoError(j).getDeclarator();
			Identifier id = (Identifier) declarator.getChild(0);
			ret.add(id.getName());
		}
		return ret;
	}

	private Set<String> getPotentionalyChangedLocalVariables() {
		Filter<Identifier> filter = new Filter<Identifier>() {
			@Override
			public boolean test(Identifier id) {
				return varUtil.isLocalVariable(id) && isModifing(id);
			}
		};

		List<Statement> statements = getStatementsToExtract();
		return exploreVariableNamespace(statements, filter);
	}

	private Set<String> exploreVariableNamespace(ASTNode node,
			Filter<Identifier> test) {
		return exploreVariableNamespace_sub(node, test, new HashSet<String>());
	}

	private Set<String> exploreVariableNamespace(Collection<Statement> statements,
			Filter<Identifier> test) {
		
		
		Set<String> ret = new HashSet<String>();
		Set<String> declarations = new HashSet<String>();
		for(Statement s : statements){
			ret.addAll(exploreVariableNamespace(s, test));
		}
		// localy declarated variables don't need to be passed
		ret.removeAll(declarations);
		
		return ret;
	}
	
	private Set<String> getTopLevelDeclarations(Collection<Statement> statements){
		Set<String> declarations = new HashSet<String>();
		for(Statement s : statements){
			if(isDeclaration(s)){
				Declaration d = (Declaration)s;
				declarations.addAll(getDeclaredVariableNames(d));
			}
		}
		return declarations;
	}

	private Set<String> exploreVariableNamespace_sub(ASTNode node,
			Filter<Identifier> test, Set<String> shadowedVars) {
		Queue<ASTNode> nodesToCheck = new LinkedList<ASTNode>();
		nodesToCheck.add(node);
		Set<String> vars = new HashSet<String>();
		while (!nodesToCheck.isEmpty()) {
			ASTNode child = nodesToCheck.poll();
			if (child instanceof Identifier) {
				Identifier id = (Identifier) child;
				if (!shadowedVars.contains(id.getName()) && test.test(id)) {
					String name = id.getName();
					vars.add(name);
				}
			} else if (child instanceof CompoundStatement
					|| child instanceof ForStatement) {
				// If Variable gets shadowed, it would be wrong to look for it.

				Set<String> child_shadowedVars = new HashSet<String>();

				if (child instanceof CompoundStatement) {
					CompoundStatement child_cs = (CompoundStatement) child;
					child_shadowedVars
							.addAll(getLocalyDefinedVariables(child_cs));
				}

				if (child instanceof ForStatement) {
					ForStatement child_for = (ForStatement) child;
					child_shadowedVars
							.addAll(getLocalyDefinedVariables(child_for
									.getInit()));
				}

				child_shadowedVars.addAll(shadowedVars);
				// if I would send the child_cs, it would result in a
				// infinit loop
				for (ASTNode c : astUtil.getChilds(child)) {
					vars.addAll(exploreVariableNamespace_sub(c, test,
							child_shadowedVars));
				}
			} else {
				nodesToCheck.addAll(astUtil.getChilds(child));
			}

		}

		return vars;
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
		PostfixExpression pfe = (PostfixExpression) astUtil.getParentForName(
				id, PostfixExpression.class);
		if (pfe != null) {
			if (pfe.getExpression() instanceof IdentifierExpression) {
				if (((IdentifierExpression) pfe.getExpression())
						.getIdentifier().equals(id)) {
					// The only Postfix Operators are Increament and Decrement
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

	private boolean isHighestLocalScope(CompoundStatement cs) {
		return cs.getParent() instanceof FunctionDefinition;
	}

	@SuppressWarnings("unchecked")
	private boolean isLoop(CompoundStatement cs) {
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
	 * Returns a list of names of local Variables which get modified within the
	 * Area to extract, but are also read afterwards. Each Variable in that
	 * Collection will become an output-Parameter for the created Function.
	 * 
	 * @param begin
	 * @param end
	 * @return
	 */
	private Set<String> getOutputParameters() {
		// check the Area to extract for write operations on those variables
		Set<String> changedVariablesInAreaToExtract = getPotentionalyChangedLocalVariables();

		// Get all Identifiers used after the Area to extract.
		CompoundStatement cs = getDeepedstCompoundSuperstatement();
		Set<String> varibalesReadAfterAreaToExtract = getReadLocalVariablesAfterArea2Extract(
				Collections.unmodifiableSet(changedVariablesInAreaToExtract),
				cs);
		
		// Return Variables that are modified in the area to extract, but also
		// read afterwords
		varibalesReadAfterAreaToExtract
				.retainAll(changedVariablesInAreaToExtract);
		return varibalesReadAfterAreaToExtract;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		Set<String> outputParameter = getOutputParameters();
		Set<String> inputParameter = getInputParameter();
		// if a variable is an output parameter, there is no need to also create
		// an input Parameter
		inputParameter.removeAll(outputParameter);

		System.err.println("Output Paramteter: " + outputParameter
				+ "\nInput Parameter: " + inputParameter);
		return new NullChange();
	}

	private Set<String> getInputParameter() {
		Filter<Identifier> filter = new Filter<Identifier>() {
			@Override
			public boolean test(Identifier toTest) {
				return varUtil.isLocalVariable(toTest);
			}
		};

		Collection<Statement> selectedStatements = 	getStatementsToExtract();
		
		Set<String> ret = exploreVariableNamespace(selectedStatements, filter);
		ret.removeAll(getTopLevelDeclarations(selectedStatements));
		return ret;
	}

	@Override
	public Object[] getElements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdentifier() {
		return "tinyos.yeti.extractFunction.Processor";
	}

	@Override
	public String getProcessorName() {
		return "Extract Function";
	}

	@Override
	public boolean isApplicable() throws CoreException {
		RefactoringStatus status = checkInitialConditions(new NullProgressMonitor());
		return status.isOK();
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,
			SharableParticipants sharedParticipants) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
