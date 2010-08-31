package tinyos.yeti.refactoring.entities.function.extract;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ForStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.Statement;
import tinyos.yeti.refactoring.RefactoringPlugin;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.ast.CompoundStatementAnalyzer;
import tinyos.yeti.refactoring.ast.StatementListAnalyzer;
import tinyos.yeti.refactoring.ast.VariableDeclaration;
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
	private StatementListAnalyzer partToExtractAlalyzer;

	public Processor(Info info) {
		this.info = info;
		this.astUtil = new ASTUtil();
		this.varUtil = new ASTUtil4Variables(astUtil);
		this.astPos = new ASTPositioning(info.getAst());
		partToExtractAlalyzer = new StatementListAnalyzer(info
				.getStatementsToExtract(), info);
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
		if (info.isVaidSelection()) {
			int begin = info.getSelectionBegin();
			int end = info.getSelectionEnd();
			if (!isInSameCompoundStatement(begin, end)) {
				status
						.addFatalError("Begin and End of your Selection have to be within the same Block to allow the Extract Function Refactoring");
			}
		} else {
			status.addFatalError("The selected Code is not extractable.");
		}

		return status;
	}

	/**
	 * To extract a Function the whole extracted Area should be part of one
	 * Block. Otherwise you would extract eg. half of a loop body. This Method
	 * tests whether begin and end are in the same Block.
	 */
	private boolean isInSameCompoundStatement(int begin, int end) {
		// System.err.println("Begin:"+begin+" End:"+end);
		// makes shure, that if the selection ends after a sub block, this is
		// correct seen.
		end++;
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
		Set<String> changedVariablesInAreaToExtract = partToExtractAlalyzer
				.getPotentionalyChangedLocalVariables();

		// Get all Identifiers used after the Area to extract.
		CompoundStatement cs = astPos
				.getDeepedstSuperCompoundSuperstatement(info
						.getSelectionBegin());
		CompoundStatementAnalyzer csa = new CompoundStatementAnalyzer(cs, info);
		Statement lastStatementToExtract = (new LinkedList<Statement>(info
				.getStatementsToExtract())).getLast();
		Set<String> varibalesReadAfterAreaToExtract = csa
				.getReadLocalVariablesAfter(Collections
						.unmodifiableSet(changedVariablesInAreaToExtract),
						lastStatementToExtract);

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
			multiTextEdit.addChild(new InsertEdit(newFunctionPos, newFunction
					+ "\n\n"));

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

	private TextEdit getSelection2FunctionCallReplacement()
			throws CoreException, MissingNatureException, IOException {
		String extractedDeclarations = getExtractedDeclarations();
		String newFunctionCall = getNewLine() + getFunctionCall();
		String newLine = getNewLine();
		String selectionReplacement = newLine + extractedDeclarations + newLine
				+ newFunctionCall;

		int extractedCodeBegin = info.getSelectionBegin();
		int extractedCodeLenth = info.getSelectionEnd() - extractedCodeBegin;
		TextEdit selectionReplacemnet = new ReplaceEdit(extractedCodeBegin,
				extractedCodeLenth, selectionReplacement);
		return selectionReplacemnet;
	}

	/**
	 * Very simple way to get the right amount of tabs before return
	 */
	private String getNewLine() {
		String ret = "\n";
		int levels = getLevelsOfAcestorCompoundStatements(info
				.getStatementsToExtract().iterator().next());
		for (int i = 1; i < levels; i++) {
			ret += "\t";
		}
		return ret;
	}

	private int getLevelsOfAcestorCompoundStatements(ASTNode node) {
		int levels;
		for (levels = 0; node != null; levels++) {
			node = astUtil.getParentForName(node, CompoundStatement.class);
		}
		return levels;
	}

	private String getExtractedDeclarations() throws CoreException,
			MissingNatureException, IOException {
		StringBuffer ret = new StringBuffer();
		Set<String> outputParameter = getOutputParameters();
		Set<String> localyUnused = partToExtractAlalyzer
				.getInternalyUnusedDeclarations();
		Set<String> potentialyExtractedDeclarations = new HashSet<String>();
		potentialyExtractedDeclarations.addAll(outputParameter);
		potentialyExtractedDeclarations.addAll(localyUnused);
		Collection<VariableDeclaration> variableDeclarations = partToExtractAlalyzer
				.getTopLevelDeclarations();
		for (VariableDeclaration dec : variableDeclarations) {
			String partialDeclaration = dec
					.getPartialDeclaration(potentialyExtractedDeclarations);
			if (!partialDeclaration.equals("")) {
				ret.append(getNewLine());
				ret.append(partialDeclaration);
			}

		}
		return ret.toString();
	}

	private String getFunctionCall() {
		StringBuffer ret = new StringBuffer();

		ret.append(info.getFunctionName());
		ret.append(" (");

		LinkedList<String> parameter = new LinkedList<String>();
		for (String outputParameter : getOutputParameters()) {
			parameter.add("&" + outputParameter);
		}

		parameter.addAll(getInputParameter());

		ret.append(StringUtil.joinString(parameter, ", "));

		ret.append(");");

		return ret.toString();
	}

	private int getNewFunctionPos() {
		Statement aStatementWithingTheSelectedCode = info
				.getStatementsToExtract().iterator().next();
		FunctionDefinition functionFromWhichCodeGetsExtracted = astUtil
				.getParentForName(aStatementWithingTheSelectedCode,
						FunctionDefinition.class);
		int ret = 0;
		if (functionFromWhichCodeGetsExtracted == null) {
			// Should never happen. Code in NesC has to be within a Function
			ret = 0; // begin of File
		} else {
			// at the Begin of the Function from which the code got extracted
			// The created Function has to be before the calling Function.
			// Otherwise it's not visible
			ret = astPos.start(functionFromWhichCodeGetsExtracted);
		}

		return ret;
	}

	private String generateNewFunction() throws CoreException,
			MissingNatureException, IOException {
		Set<String> outputParameter = getOutputParameters();
		Set<String> inputParameter = getInputParameter();

		String returnType = "void";

		// function header
		StringBuffer func = new StringBuffer();
		func.append(returnType);
		func.append(" ");
		func.append(info.getFunctionName());
		func.append("(");
		LinkedList<String> params = new LinkedList<String>();

		for (String var : outputParameter) {
			params.add(getParameterDeclaration(var, true));
		}

		for (String var : inputParameter) {
			params.add(getParameterDeclaration(var, false));
		}

		func.append(StringUtil.joinString(params, ", "));
		func.append(")\n{" + getNewLine());
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
				varNamesToKeep.removeAll(partToExtractAlalyzer
						.getInternalyUnusedDeclarations());
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
		Set<Identifier> ids = varUtil.exploreVariableNamespace(node,
				new Filter<Identifier>() {

					@Override
					public boolean test(Identifier toTest) {
						return true;
					}
				}, info);

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

	private String getParameterDeclaration(String var, boolean outputParameter)
			throws CoreException, MissingNatureException, IOException {
		StringBuffer ret = new StringBuffer();
		VariableDeclaration dec = findDeclaration(var, info
				.getStatementsToExtract().iterator().next());
		if (dec == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					RefactoringPlugin.PLUGIN_ID,
					"Could not determine the Type of Variable " + var));
		}
		ret.append(dec.getType());
		ret.append(" ");
		if (outputParameter) {
			ret.append("*");
		}
		ret.append(dec.getPointerName(var));

		return ret.toString();
	}

	/**
	 * If the declaration of the Variable varName is found, it is returned
	 * Otherwise null is returned
	 */
	private VariableDeclaration findDeclaration(String varName, ASTNode node) {
		boolean found = false;
		LinkedList<ASTNode> specials = new LinkedList<ASTNode>();
		LinkedList<VariableDeclaration> declarations = new LinkedList<VariableDeclaration>();
		VariableDeclaration ret = null;

		// For Loop header Declarations (if node is element of the init of a for
		// loop)
		ForStatement forStatement = astUtil.getParentForName(node,
				ForStatement.class);
		if (forStatement != null) {
			specials.add(forStatement.getInit());
		}

		// For FunctionDeclarations (if node is element of a function header)
		FunctionDefinition funcDef = astUtil.getParentForName(node,
				FunctionDefinition.class);
		if (forStatement != null) {
			Collection<ASTNode> defs = astUtil.getChilds(funcDef
					.getDeclarator().getFunction().getParameters());
			specials.addAll(defs);
		}
		declarations.addAll(VariableDeclaration.getTopLevelDeclarations(
				specials, info));

		// Traversing upper Compound Statements and there Variable declarations
		CompoundStatement cs = astUtil.getParentForName(node,
				CompoundStatement.class);
		while (!found && cs != null) {
			CompoundStatementAnalyzer csa = new CompoundStatementAnalyzer(cs,
					info);
			declarations.addAll(csa.getLocalVariableDeclarations());
			cs = astUtil.getParentForName(cs, CompoundStatement.class);
		}

		for (VariableDeclaration dec : declarations) {
			if (dec.getVariableNames().contains(varName)) {
				ret = dec;
			}
		}

		return ret;
	}

	private Set<String> getInputParameter() {
		Filter<Identifier> filter = new Filter<Identifier>() {
			@Override
			public boolean test(Identifier toTest) {
				return varUtil.isLocalVariableOrFunctionParameter(toTest);
			}
		};

		Collection<Statement> selectedStatements = info
				.getStatementsToExtract();

		Set<String> ret = varUtil.getNames(varUtil.exploreVariableNamespace(
				selectedStatements, filter, info));
		ret.removeAll(partToExtractAlalyzer.getTopLevelDeclarations());
		ret.removeAll(getOutputParameters());
		ret.removeAll(partToExtractAlalyzer.getInternalyUnusedDeclarations());
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
