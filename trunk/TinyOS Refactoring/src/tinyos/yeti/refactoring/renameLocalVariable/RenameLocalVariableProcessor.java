package tinyos.yeti.refactoring.renameLocalVariable;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ASTUtil;

public class RenameLocalVariableProcessor extends RefactoringProcessor {

	private RenameLocalVariableInfo info;
	private NesC12AST ast;
	private ITextSelection selection;
	private ASTUtil utility;

	public RenameLocalVariableProcessor(RenameLocalVariableInfo info) {
		super();
		this.info = info;
		ast=(NesC12AST) info.getEditor().getAST();
		utility=new ASTUtil(ast);
		ISelection selectionTmp = info.getEditor().getSelectionProvider().getSelection();

		if (selectionTmp.isEmpty() || !(selectionTmp instanceof ITextSelection)) {
			throw new RuntimeException("----- Es war keine ITextSelection");
		}

		selection = (ITextSelection) selectionTmp;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws CoreException,
			OperationCanceledException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		// TODO Auto-generated method stub
		return new RefactoringStatus();
	}
	
	private Identifier getSelectedIdentifier(){
		int selectionStart = selection.getOffset();
		ASTNode currentlySelected = utility.getASTLeafAtPos(selectionStart);
		
		if(currentlySelected instanceof Identifier){
			return (Identifier) currentlySelected;
		}
		System.err.println("NOT IDENTIFIER Selected!!!");
		return null;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		
	//setup
		NesCEditor editor = info.getEditor();
		utility = new ASTUtil(ast);
		
		
	//Find currently selected Element
		Identifier currentlySelected=getSelectedIdentifier();
		
		
	//Find Enclosing Function Definition
		ASTNode parent = ASTUtil.getParentForName(currentlySelected,
				FunctionDefinition.class);
		FunctionDefinition functionDef = null;
		if (parent == null) {
			System.err.println("Selection not inside a Function!");
			return new NullChange();
		} else {
			functionDef = (FunctionDefinition) parent;
		}
		
		
	//Get Identifiers in Function with same Name
		 Collection<Identifier> identifiers=ASTUtil.getIncludedIdentifiers(parent, currentlySelected.getName());
		
		
	//Get the InputFile
		IEditorInput editorInput = editor.getEditorInput();
		if (!(editorInput instanceof IFileEditorInput)) {
			System.err.println("The Editor Input was not a File");
			return new NullChange();
		}
		IFile inputFile = ((IFileEditorInput) editorInput).getFile();
		
		
	//Create The Changes
		MultiTextEdit multiTextEdit=new MultiTextEdit();
		TextChange renameOneOccurence = new TextFileChange(
				"Replacing Variable " + info.getOldName() + " with "
						+ info.getNewName() + " in File " + inputFile,inputFile);
		renameOneOccurence.setEdit(multiTextEdit);
		CompositeChange ret = new CompositeChange("Rename Local Variable "+ info.getOldName() + " to " + info.getNewName());
		ret.add(renameOneOccurence);
		for (Identifier identifier : identifiers) {
			int beginOffset = utility.start(identifier);
			int endOffset=utility.end(identifier);
			int length = endOffset-beginOffset;
			multiTextEdit.addChild(new ReplaceEdit(beginOffset, length, info.getNewName()));
		}
		return ret;
	}

	@Override
	public Object[] getElements() {
		return new Object[]{info.getEditor().getEditorInput()};
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
		return true;
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,
			SharableParticipants sharedParticipants) throws CoreException {
		return new RefactoringParticipant[0];
	}

}
