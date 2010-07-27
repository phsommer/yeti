package tinyos.yeti.refactoring.rename.local.variable;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
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

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class RenameLocalVariableProcessor extends RenameProcessor {

	private RenameInfo info;

	public RenameLocalVariableProcessor(RenameInfo info) {
		super(info);
		this.info = info;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws CoreException,
			OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if (info.getNewName() == null) {
			ret.addFatalError("Please enter a new Name for the Variabel.");
		}

		return ret;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if (!isApplicable()) {
			ret.addFatalError("A local Variable must be selected.");
		}
		IFile sourceFile = ActionHandlerUtil.getInputFile(info.getEditor());
		if (sourceFile == null || !sourceFile.exists()) {
			ret.addFatalError("The File you want wo Refactor, does not exist.");
		} else if (sourceFile.isReadOnly()) {
			ret.addFatalError("The File you want to Refactor is read only.");
		}
		return ret;
	}

	@Override
	public Object[] getElements() {
		return new Object[] { info.getEditor().getEditorInput() };
	}

	@Override
	public String getIdentifier() {
		return "tinyos.yeti.refactoring.renameLocalVariable.RenameLocalVariableProcessor";
	}

	@Override
	public String getProcessorName() {
		return "Rename Local Variable Prozessor";
	}

	@Override
	public boolean isApplicable() throws CoreException {
		// Tests if a LOCAL Variable is selected
		Identifier identifier = getSelectedIdentifier();
		if (identifier == null)
			return false;
		CompoundStatement compound = ASTUtil4Variables.findDeclaringCompoundStatement(identifier);
		return compound != null;
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,
			SharableParticipants sharedParticipants) throws CoreException {
		return new RefactoringParticipant[0];
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {

		IFile inputFile = ActionHandlerUtil.getInputFile(info.getEditor());
		// Create The Changes
		MultiTextEdit multiTextEdit = new MultiTextEdit();
		String changeName = "Replacing Variable " + info.getOldName()+ " with " + info.getNewName() + " in Document " + inputFile;
		TextChange renameAllOccurences = new TextFileChange(changeName,
				inputFile);
		// IDocument document=info.getEditor().getDocument();
		// TextChange renameOneOccurence = new
		// DocumentChange(changeName,document);
		renameAllOccurences.setEdit(multiTextEdit);
		CompositeChange ret = new CompositeChange("Rename Local Variable "
				+ info.getOldName() + " to " + info.getNewName());
		ret.add(renameAllOccurences);
		Collection<Identifier> identifiers = this.selectedIdentifiersIfLocal();
		if (identifiers.size() == 0) {
			return new NullChange();
		}
		addChanges4Identifiers(identifiers,info.getNewName(),multiTextEdit,null);
		return ret;
	}

	/**
	 * Returns a list which contains all occurrences of the selected identifier.
	 * Checks if the selection is an identifier and if so if it is part of a
	 * local variable.
	 * 
	 * @return All Occurrences in the Method of the selected identifier,
	 *         immutable EmptyList if the above checks fail.
	 */
	private Collection<Identifier> selectedIdentifiersIfLocal() {
		// Find currently selected Element
		Identifier currentlySelected = getSelectedIdentifier();
		if (currentlySelected == null) { // The Selection is not an Identifier
			return Collections.emptyList();
		} else if (!ASTUtil4Variables.isLocalVariable(currentlySelected)) {
			return Collections.emptyList();
		}

		// Find the CompoundStatement which declares the identifier
		CompoundStatement declaringCompound = ASTUtil4Variables.findDeclaringCompoundStatement(currentlySelected);
		Collection<Identifier> identifiers = getVarUtil().getAllIdentifiersWithoutOwnDeclaration(
				declaringCompound, currentlySelected.getName());
		return identifiers;
	}

}
