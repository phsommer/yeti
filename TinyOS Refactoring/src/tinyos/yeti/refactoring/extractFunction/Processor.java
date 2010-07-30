package tinyos.yeti.refactoring.extractFunction;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
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
import tinyos.yeti.refactoring.RefactoringPlugin;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;
import tinyos.yeti.refactoring.utilities.Filter;
import tinyos.yeti.refactoring.utilities.StringUtil;

public class Processor extends RefactoringProcessor {

	private Info info;
	private ASTUtil astUtil;
	private ASTUtil4Variables varUtil;
	private ASTPositioning astPos;
	private String newLine = null;

	public Processor(Info info) {
		this.info = info;
		this.astUtil = new ASTUtil();
		this.varUtil = new ASTUtil4Variables(astUtil);
		this.astPos = new ASTPositioning(info.getAst());
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

	



	private Set<String> getReadLocalVariablesAfterArea2Extract(
			Set<String> namesToCheck, CompoundStatement cs) {
		// Which names to check are defined in this cs => only occurrences of
		// those within cs after end are interesting
		namesToCheck = new HashSet<String>(namesToCheck);
		Set<String> localyDefined = getLocalyDefinedVariableNames(cs);
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
		for (ASTNode c : astUtil.getChilds(cs)) {
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
				if (astPos.start(child) < info
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

		Set<Identifier> ids = exploreVariableNamespace(nodesToCheck, filter);
		return getNames(ids);
	}

	/**
	 * Returns the Names of the Variables that are defined in the given
	 * CompoundStatement
	 */
	private Set<String> getLocalyDefinedVariableNames(ASTNode cs) {
		Set<String> ret = new HashSet<String>();
		for (VariableDeclaration d : getLocalVariableDeclarations(cs)) {
			ret.addAll(d.getVariableNames());
		}

		return ret;
	}

	private Set<VariableDeclaration> getLocalVariableDeclarations(ASTNode cs) {
		Collection<ASTNode> nodesToCheckForDeclarations = new LinkedList<ASTNode>();

		// All Statements in the Compound Statement
		nodesToCheckForDeclarations.addAll(astUtil.getChilds(cs));

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

		return getTopLevelDeclarations(nodesToCheckForDeclarations);
	}

	/**
	 * Fields that are defined within the Statements to extract, but are still
	 * visible after the End of the selection (so highest level declarations)
	 * are not filterd out.
	 */
	private Set<String> getPotentionalyChangedLocalVariables() {
		Filter<Identifier> filter = new Filter<Identifier>() {
			@Override
			public boolean test(Identifier id) {
				return varUtil.isLocalVariable(id) && isModifing(id);
			}
		};

		List<Statement> statements = info.getStatementsToExtract();
		Set<Identifier> ids = exploreVariableNamespace(statements, filter);
		Set<String> names = getNames(ids);
		return names;
	}

	private Set<String> getNames(Set<Identifier> ids) {
		Set<String> names = new HashSet<String>();
		for (Identifier id : ids) {
			names.add(id.getName());
		}
		return names;
	}
	
	private Set<String> getNames(Collection<VariableDeclaration> decs){
		Set<String> names = new HashSet<String>();
		for (VariableDeclaration dec : decs) {
			names.addAll(dec.getVariableNames());
		}
		return names;
		
	}

	private Set<Identifier> exploreVariableNamespace(ASTNode node,
			Filter<Identifier> test) {
		return exploreVariableNamespace_sub(node, test, new HashSet<String>());
	}

	private Set<Identifier> exploreVariableNamespace(
			Collection<Statement> statements, Filter<Identifier> test) {

		Set<Identifier> ret = new HashSet<Identifier>();

		for (Statement statement : statements) {
			ret.addAll(exploreVariableNamespace(statement, test));
		}

		return ret;
	}

	private Set<VariableDeclaration> getTopLevelDeclarations(
			Collection<? extends ASTNode> statements) {
		Set<VariableDeclaration> declarations = new HashSet<VariableDeclaration>();
		for (ASTNode s : statements) {
			if (VariableDeclaration.isDeclaration(s)) {
				VariableDeclaration dec = VariableDeclaration.factory(s, info);
				declarations.add(dec);
			}
		}
		return declarations;
	}

	private Set<Identifier> exploreVariableNamespace_sub(ASTNode node,
			Filter<Identifier> test, Set<String> shadowedVars) {
		Queue<ASTNode> nodesToCheck = new LinkedList<ASTNode>();
		nodesToCheck.add(node);
		Set<Identifier> vars = new HashSet<Identifier>();
		while (!nodesToCheck.isEmpty()) {
			ASTNode child = nodesToCheck.poll();
			if (child instanceof Identifier) {
				Identifier id = (Identifier) child;
				if (!shadowedVars.contains(id.getName()) && test.test(id)) {
					vars.add(id);
				}
			} else if (child instanceof CompoundStatement
					|| child instanceof ForStatement) {
				// If Variable gets shadowed, it would be wrong to look for it.

				Set<String> child_shadowedVars = new HashSet<String>();

				if (child instanceof CompoundStatement) {
					CompoundStatement child_cs = (CompoundStatement) child;
					child_shadowedVars
							.addAll(getLocalyDefinedVariableNames(child_cs));
				}

				if (child instanceof ForStatement) {
					ForStatement child_for = (ForStatement) child;
					child_shadowedVars
							.addAll(getLocalyDefinedVariableNames(child_for
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
		CompoundStatement cs = astPos.getDeepedstSuperCompoundSuperstatement(info.getSelectionBegin());
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
		IFile inputFile = ActionHandlerUtil.getInputFile(info.getEditor());
		MultiTextEdit multiTextEdit = new MultiTextEdit();

		String changeName = "Extracting Code Block to new Function "
				+ info.getFunctionName();
		TextChange ret = new TextFileChange(changeName, inputFile);
		ret.setEdit(multiTextEdit);

		try {
			String newFunction = generateNewFunction();
			int newFunctionPos = getNewFunctionPos();
			multiTextEdit.addChild(new InsertEdit(newFunctionPos, "\n\n"+newFunction));

			TextEdit selectionReplacement = getSelection2FunctionCallReplacement();
			multiTextEdit.addChild(selectionReplacement);

		} catch (MissingNatureException e) {
			Status status = new Status(IStatus.ERROR,
					RefactoringPlugin.PLUGIN_ID,
					"Could not Read the Source file.(NatureMissing) "
							+ e.getLocalizedMessage());
			throw new CoreException(status);
		} catch (IOException e) {
			Status status = new Status(IStatus.ERROR,
					RefactoringPlugin.PLUGIN_ID,
					"Could not Read the Source file.(IO Error) "
							+ e.getLocalizedMessage());
			throw new CoreException(status);
		}
		return ret;
	}

	private TextEdit getSelection2FunctionCallReplacement() throws CoreException, MissingNatureException, IOException {
		String extractedDeclarations = getExtractedDeclarations();
		String newFunctionCall = getFunctionCall();
		String selectionReplacement = extractedDeclarations + "\n"
				+ newFunctionCall;

		int extractedCodeBegin = info.getSelectionBegin();
		int extractedCodeLenth = info.getSelectionEnd()
				- extractedCodeBegin;
		TextEdit selectionReplacemnet = new ReplaceEdit(extractedCodeBegin,
				extractedCodeLenth, selectionReplacement);
		return selectionReplacemnet;
	}
	
	/**
	 * Very simple way to get the right amount of newlines before return
	 */
	/*private String getNewLine(){
	
	}*/

	private String getExtractedDeclarations() throws CoreException, MissingNatureException, IOException {
		StringBuffer ret = new StringBuffer();
		Set<String> outputParameter = getOutputParameters();
		Set<String> localyUnused = getInternalyUnusedDeclarations();
		Set<String> potentialyExtractedDeclarations = new HashSet<String>();
		potentialyExtractedDeclarations.addAll(outputParameter);
		potentialyExtractedDeclarations.addAll(localyUnused);
		Collection<VariableDeclaration> variableDeclarations = getTopLevelDeclarations(info.getStatementsToExtract());
		for(VariableDeclaration dec : variableDeclarations){
			String partialDeclaration = dec.getPartialDeclaration(potentialyExtractedDeclarations);
			if(!partialDeclaration.equals("")){
				ret.append(partialDeclaration);
				ret.append("\n");
			}
			
		}
		return ret.toString();
	}
	
	private Set<String> getInternalyUnusedDeclarations(){
		Set<String> ret = new HashSet<String>();
		
		for(VariableDeclaration dec : getTopLevelDeclarations(info.getStatementsToExtract())){
			List<Statement> list = new LinkedList<Statement>(info.getStatementsToExtract());
			list.remove(dec.getAstNode());
			for(String var: dec.getVariableNames()){
				Set<Identifier> ids = exploreVariableNamespace(list, new Filter<Identifier>() {

					@Override
					public boolean test(Identifier toTest) {
						return (new ASTUtil4Variables()).isLocalVariable(toTest);
					}
				});
				if(!getNames(ids).contains(var)){
					ret.add(var);
				}
			}
		}
		
		return ret;
	}

	private String getFunctionCall() {
		StringBuffer ret = new StringBuffer();
		
		ret.append(info.getFunctionName());
		ret.append(" (");
		
		LinkedList<String> parameter = new LinkedList<String>();
		for(String outputParameter: getOutputParameters()){
			parameter.add("&"+outputParameter);
		}
		
		
		parameter.addAll(getInputParameter());
		
		ret.append(StringUtil.joinString(parameter, ", "));
		
		ret.append(");");
		
		return ret.toString();
	}

	private int getNewFunctionPos() {
		Statement aStatementWithingTheSelectedCode = info.getStatementsToExtract().iterator().next();
		FunctionDefinition functionFromWhichCodeGetsExtracted = astUtil.getParentForName(aStatementWithingTheSelectedCode, FunctionDefinition.class);
		int ret = 0;
		if(functionFromWhichCodeGetsExtracted == null){ 
			// Should never happen. Code in NesC has to be within a Function
			ret = 0; // begin of File
		} else{
			// at the Begin of the Function from which the code got extracted
			// The created Function has to be before the calling Function. Otherwise it's not visible
			ret = astPos.start(functionFromWhichCodeGetsExtracted);
		}
		
		return ret;
	}

	private String generateNewFunction() throws CoreException,
			MissingNatureException, IOException {
		info.setFunctionName("newFunction");
		Set<String> outputParameter = getOutputParameters();
		Set<String> inputParameter = getInputParameter();
		
		System.err.println("Output Paramteter: " + outputParameter
				+ "\nInput Parameter: " + inputParameter);

		String returnType = "void";

		// function header
		StringBuffer func = new StringBuffer();
		func.append(returnType);
		func.append(" ");
		func.append(info.getFunctionName());
		func.append("(");
		LinkedList<String> params = new LinkedList<String>();
		for (String var : outputParameter) {
			params.add(getVariableType(var) + " *" + var);
		}

		for (String var : inputParameter) {
			params.add(getVariableType(var) + " " + var);
		}
		func.append(StringUtil.joinString(params, ", "));
		func.append(")\n{\n");
		func.append(getFunctionBody(outputParameter, inputParameter));
		func.append("\n}\n");

		return func.toString();
	}

	private String getFunctionBody(Set<String> outputParameter,
			Set<String> inputParameter) throws CoreException,
			MissingNatureException, IOException {
		StringBuffer funcBody = new StringBuffer();
		List<Statement> origStatements = info.getStatementsToExtract();
		Statement lastStatement = null;
		for (Statement statement : origStatements) {
			// to preserve the programmers code Layout
			if (lastStatement != null) {
				funcBody.append(astPos.getSourceBetween(lastStatement,
						statement, info.getProjectUtil()));
			}

			if (VariableDeclaration.isDeclaration(statement)) {
				VariableDeclaration dec = VariableDeclaration.factory(
						statement, info);
				// Output Parameter don't need to be Declared, they get decleard
				// in the Header
				Set<String> varNamesToKeep = new HashSet<String>(dec
						.getVariableNames());
				varNamesToKeep.removeAll(outputParameter);
				varNamesToKeep.removeAll(inputParameter);
				varNamesToKeep.remove(getInternalyUnusedDeclarations());
				funcBody.append(dec.getPartialDeclaration(varNamesToKeep));
			} else {
				funcBody.append(replaceOutputParameter(statement,
						outputParameter));
			}

			lastStatement = statement;
		}

		return funcBody.toString();
	}

	private String replaceOutputParameter(Statement statement,
			Set<String> outputParameter) throws CoreException,
			MissingNatureException, IOException {
		int begin = astPos.start(statement);
		String ret = astPos.getSourceCode(statement, info.getProjectUtil());

		// We get the identifiers in order from getAllIdentifiers.
		List<Identifier> identifiers = getAllIdentifier(statement);
		// But we have to start with the last, cause then the position-offset of
		// the primer
		// Identifier does not change
		Collections.reverse(identifiers);

		for (Identifier identifier : identifiers) {
			if (outputParameter.contains(identifier.getName())) {
				int idOffsetBegin = astPos.start(identifier) - begin;
				int idOffsetEnd = astPos.end(identifier) - begin;
				ret = ret.substring(0, idOffsetBegin) + "(*"
						+ identifier.getName() + ")"
						+ ret.substring(idOffsetEnd);
			}
		}

		return ret;
	}

	/**
	 * Returns all the Identifier Childs of node in Depth-First-Search-Order
	 */
	private List<Identifier> getAllIdentifier(ASTNode node) {
		Set<Identifier> ids = exploreVariableNamespace(node,
				new Filter<Identifier>() {

					@Override
					public boolean test(Identifier toTest) {
						return true;
					}
				});

		List<Identifier> ret = new LinkedList<Identifier>(ids);

		Collections.sort(ret, new Comparator<Identifier>() {
			@Override
			public int compare(Identifier o1, Identifier o2) {
				Integer begin1 = astPos.start(o1);
				Integer begin2 = astPos.start(o2);
				return begin1.compareTo(begin2);
			}
		});

		return ret;
	}

	private String getVariableType(String var) throws CoreException,
			MissingNatureException, IOException {
		VariableDeclaration dec = findDeclaration(var, info.getStatementsToExtract()
				.iterator().next());
		if (dec == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					RefactoringPlugin.PLUGIN_ID,
					"Could not determine the Type of Variable " + var));
		}
		return dec.getType();
	}

	/**
	 * If the declaration of the Variable varName is found, it is returned
	 * Otherwise null is returned
	 */
	private VariableDeclaration findDeclaration(String varName, ASTNode node) {
		boolean found = false;
		while (!found && node.getParent() != null) {
			found = getLocalyDefinedVariableNames(node.getParent()).contains(
					varName);
			if (found) {
				for (VariableDeclaration dec : getLocalVariableDeclarations(node
						.getParent())) {
					if (dec.getVariableNames().contains(varName)) {
						return dec;
					}
				}
			}
			node = node.getParent();
		}
		return null;
	}

	private Set<String> getInputParameter() {
		Filter<Identifier> filter = new Filter<Identifier>() {
			@Override
			public boolean test(Identifier toTest) {
				return varUtil.isLocalVariable(toTest);
			}
		};

		Collection<Statement> selectedStatements = info.getStatementsToExtract();

		Set<String> ret = getNames(exploreVariableNamespace(selectedStatements,
				filter));
		ret.removeAll(getTopLevelDeclarations(selectedStatements));
		ret.removeAll(getOutputParameters());
		ret.removeAll(getInternalyUnusedDeclarations());
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
