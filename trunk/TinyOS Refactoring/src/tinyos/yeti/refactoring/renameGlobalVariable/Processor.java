package tinyos.yeti.refactoring.renameGlobalVariable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.builder.ProjectResourceCollector;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ASTUtil;
import tinyos.yeti.refactoring.RefactoringPlugin;
import tinyos.yeti.refactoring.RefactoringPlugin.LogLevel;
import tinyos.yeti.refactoring.rename.RenameProcessor;

public class Processor extends RenameProcessor {

	private Info info;

	public Processor(Info info) {
		super(info);
		this.info = info;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws CoreException,
			OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if (!isApplicable()) {
			ret.addFatalError("The Refactoring is no Accessable");
		}
		return ret;
		// TODO checkFinalConditions not yet implemented

	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if (!isApplicable()) {
			ret.addFatalError("The Refactoring is no Accessable");
		}
		if (!isGlobalVariable(getSelectedIdentifier())) {
			ret.addFatalError("No global variable selected.");
		}
		return ret;
	}

	private NesC12AST getAst(IFile iFile, IProgressMonitor monitor)
			throws IOException, MissingNatureException {
		// Create Parser for File to construct an AST
		IProject project = info.getEditor().getProject();
		ProjectModel projectModel = TinyOSPlugin.getDefault().getProjectTOS(
				project).getModel();

		File file = iFile.getLocation().toFile();
		IParseFile parseFile = projectModel.parseFile(file);

		INesCParser parser = projectModel.newParser(parseFile, null, monitor);
		parser.setCreateAST(true);
		parser.parse(new FileMultiReader(file), monitor);

		return (NesC12AST) parser.getAST();
	}

	private boolean isGlobalVariable(Identifier variable) {
		// Local Variable are of corse no globle variables
		if (getVarUtil().isLocalVariable(variable)) {
			return false;
		}

		// Is true in case a variable is used.
		IdentifierExpression ie = (IdentifierExpression) ASTUtil
				.getParentForName(variable, IdentifierExpression.class);
		if (ie != null) {
			return true;
		}

		// Is true in case a variable is defined.
		DeclaratorName dn = (DeclaratorName) ASTUtil.getParentForName(variable,
				DeclaratorName.class);
		if (dn != null) {
			return true;
		}

		return false;
	}

	private boolean containsOccurenceOfVariable(IFile file,
			Identifier variable, IProgressMonitor monitor)
			throws MissingNatureException {
		ASTModelPath fPath = getPathOfVariable(variable);
		ProjectModel model = TinyOSPlugin.getDefault().getProjectTOS(
				info.getEditor().getProject()).getModel();
		IASTReference[] referenceArray = model.getReferences(model
				.parseFile(file), monitor);
		for (IASTReference ref : referenceArray) {
			if (ref.getTarget().equals(fPath)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the ASTModelPath of the variable or null if the Identifier does
	 * not represent a Variable or the Variable is unknown
	 * 
	 * @param variable
	 * @return
	 */
	private ASTModelPath getPathOfVariable(Identifier variable) {
		IdentifierExpression ie = (IdentifierExpression) ASTUtil
				.getParentForName(variable, IdentifierExpression.class);
		if (ie != null) {
			// this might return null if the node is unknown
			return ie.resolveField().getPath();
		}

		InitDeclarator id = (InitDeclarator) ASTUtil.getParentForName(variable,
				InitDeclarator.class);
		if (id != null) {
			// this might return null if the node is unknown
			return id.resolveField().getPath();
		}

		return null;
	}

	private Collection<IFile> getFilesContainingVariable(Identifier variable,
			IProgressMonitor pm) throws CoreException {
		IProject project = info.getEditor().getProject();
		ProjectResourceCollector collector = new ProjectResourceCollector();
		try {
			TinyOSPlugin.getDefault().getProjectTOS(project).acceptSourceFiles(
					collector);
		} catch (MissingNatureException e) {
			RefactoringPlugin.getDefault().log(
					LogLevel.WARNING,
					"Refactroing was called while Plugin was not ready: "
							+ e.getMessage());
			throw new CoreException(new Status(IStatus.ERROR,
					RefactoringPlugin.PLUGIN_ID,
					"Plugin wasn't ready while calling Rename global Variable Refactoring: "
							+ e.getMessage()));
		}

		LinkedList<IFile> files = new LinkedList<IFile>();
		for (IResource resource : collector.resources) {
			if (resource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;
				try {
					if (containsOccurenceOfVariable(file, variable, pm)) {
						files.add((IFile) resource);
					}
				} catch (MissingNatureException e) {
					throw new CoreException(new Status(IStatus.ERROR,
							RefactoringPlugin.PLUGIN_ID,
							"Refactoring was called while Plagin was not ready: "
									+ e.getMessage()));
				}
			}
		}
		return files;
	}

	private Change renameAllOccurencesInFile(IFile file,IProgressMonitor pm) {
		String changeName = "Replacing Variable " + info.getOldName()
				+ " with " + info.getNewName() + " in Document " + file;

		System.err.println(changeName);

		MultiTextEdit multiTextEdit = new MultiTextEdit();
		TextChange renameAllOccurencesInFile = new TextFileChange(changeName,
				file);
		renameAllOccurencesInFile.setEdit(multiTextEdit);
		

		// BEGIN: getting AST and handling Errors
		NesC12AST ast = null;
		try {
			ast = getAst(file, pm);
		} catch (IOException e) {
			RefactoringPlugin.getDefault().log(
					LogLevel.WARNING,
					e.getClass().getCanonicalName() + " while parsing File: "
							+ file.getFullPath().toOSString());
		} catch (MissingNatureException e) {
			RefactoringPlugin.getDefault().log(
					LogLevel.WARNING,
					e.getClass().getCanonicalName() + " while parsing File: "
							+ file.getFullPath().toOSString());
		} finally {
			if (ast == null) {
				return new NullChange();
			}
		}
		ASTNode astRoot = ast.getRoot();
		// END: getting AST and handling Errors
		
		Collection<Identifier> occurences = getVarUtil().getAllIdentifiers(
				astRoot, info.getOldName());

		// changing the name of all occurrences in the file
		for (Identifier occurece : occurences) {

			int beginOffset = getAstUtil().start(occurece);
			int endOffset = getAstUtil().end(occurece);
			int length = endOffset - beginOffset;
			multiTextEdit.addChild(new ReplaceEdit(beginOffset, length, info
					.getNewName()));
		}
		return null;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {

		Identifier selectedVariable = getSelectedIdentifier();

		CompositeChange ret = new CompositeChange("Rename Global Variable "
				+ info.getOldName() + " to " + info.getNewName());

		Collection<IFile> files = getFilesContainingVariable(selectedVariable,
				pm);

		for (IFile file : files) {
			ret.add(renameAllOccurencesInFile(file,pm));
		}

		return ret;
	}

	@Override
	public Object[] getElements() {
		// TODO Auto-generated method stub
		return new Object[] {};
	}

	@Override
	public String getIdentifier() {
		return "tinyos.yeti.refactoring.renameGlobalVariable.Processor";
	}

	@Override
	public String getProcessorName() {
		return info.getInputPageName();
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return super.isApplicable();
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,
			SharableParticipants sharedParticipants) throws CoreException {
		// TODO Auto-generated method stub
		return new RefactoringParticipant[] {};
	}

}
