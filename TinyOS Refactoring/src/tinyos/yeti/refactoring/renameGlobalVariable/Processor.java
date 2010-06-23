package tinyos.yeti.refactoring.renameGlobalVariable;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ActionHandlerUtil;
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
		return ret;
		// TODO checkInitialContions not yet implemented
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {

		CompositeChange ret = new CompositeChange("Rename Global Variable "
				+ info.getOldName() + " to " + info.getNewName());

		// TODO: Find all Files where the variable occures (4 now just the
		// editors file is used)
		Collection<IFile> files = new LinkedList<IFile>();
		files.add(ActionHandlerUtil.getInputFile(info.getEditor()));

		for (IFile file : files) {
			String changeName = "Replacing Variable " + info.getOldName()
					+ " with " + info.getNewName() + " in Document " + file;
			System.err.println(changeName);
			MultiTextEdit multiTextEdit = new MultiTextEdit();
			TextChange renameAllOccurencesInFile = new TextFileChange(
					changeName, file);
			renameAllOccurencesInFile.setEdit(multiTextEdit);
			ret.add(renameAllOccurencesInFile);

			// TODO: Get an AST of the File (4 now, just the ast of the open
			// file is used.)
			ASTNode astRoot = getAstUtil().getAST().getRoot();

			Collection<Identifier> occurences = getVarUtil().getAllIdentifiers(
					astRoot, info.getOldName());
			System.err.println("huj tja");
			for (Identifier occurece : occurences) {
				int beginOffset = getAstUtil().start(occurece);
				int endOffset = getAstUtil().end(occurece);
				int length = endOffset - beginOffset;
				multiTextEdit.addChild(new ReplaceEdit(beginOffset, length,
						info.getNewName()));
			}

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
