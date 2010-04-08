package tinyos.yeti.refactoring.renameLocalVariable;

import java.util.LinkedList;
import java.util.List;

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
import tinyos.yeti.preprocessor.PreprocessorReader;
import tinyos.yeti.refactoring.ASTUtil;

public class RenameLocalVariableProcessor extends RefactoringProcessor {

	private RenameLocalVariableInfo info;
	private ITextSelection selection;

	public RenameLocalVariableProcessor(RenameLocalVariableInfo info) {
		super();
		this.info = info;
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

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		pm.beginTask("ah", 3);

		CompositeChange ret = new CompositeChange("Rename Local Variable "
				+ info.getOldName() + " to " + info.getNewName());
		NesCEditor editor = info.getEditor();
		
		int selectionStart = selection.getOffset();
		NesC12AST ast = (NesC12AST) editor.getAST();
		

		ASTUtil utility=new ASTUtil(ast);
		
		ASTNode ours = utility.getASTLeafAtPos(selectionStart);
		System.err.println("Found Area of marked ASTNode: "
				+ ours.getRange().getLeft() + " <-> "
				+ ours.getRange().getRight());

		if(!(ours instanceof Identifier)){
			System.err.println("NOT IDENTIFIER!!!");
			return new NullChange();
		}
		Identifier id = (Identifier) ours;
		System.err.println("Name: " + id.getName());
		System.err.println("ASTNodeName: " + id.getASTNodeName());
		System.err.println("Class: " + id.getClass().getName());

		System.err.println("Selection: " + selection.getText());

		ASTNode parent = ASTUtil.getParentForName(ours,
				new FunctionDefinition().getASTNodeName());
		FunctionDefinition functionDef = null;
		if (parent == null) {
			System.err.println("Selection not inside a Function!");
			return new NullChange();
		} else {
			functionDef = (FunctionDefinition) parent;
		}

		System.err.println("/n/nNEW IDENTIFIERS: ");
		List<Identifier> identifiers = new LinkedList<Identifier>();
		ASTUtil.getIncludedIdentifiers(parent, id.getName(), identifiers);
		IEditorInput editorInput = editor.getEditorInput();
		if (!(editorInput instanceof IFileEditorInput)) {
			System.err.println("The Editor Input was not a File");
			return new NullChange();
		}
		IFile inputFile = ((IFileEditorInput) editorInput).getFile();

		MultiTextEdit multiTextEdit=new MultiTextEdit();
		TextChange renameOneOccurence = new TextFileChange(
				"Replacing Variable " + info.getOldName() + " with "
						+ info.getNewName() + " in File " + inputFile,
				inputFile);
		renameOneOccurence.setEdit(multiTextEdit);
		PreprocessorReader reader=ast.getReader();
		for (Identifier identifier : identifiers) {
			System.err.println("Name: " + identifier.getName());
			System.err.println("Range: " + ast.getOffsetAtBegin(identifier).getInputfileOffset()
					+ "<-->" + ast.getOffsetAtEnd(identifier).getInputfileOffset());

			int beginOffset = utility.start(identifier);
			int endOffset=utility.end(identifier);
			int length = endOffset-beginOffset;
			
			System.err.println("Start: "+beginOffset);
			System.err.println("End: "+endOffset);
			System.err.println("Length: "+length);
			multiTextEdit.addChild(new ReplaceEdit(beginOffset, length, info.getNewName()));
		}
		ret.add(renameOneOccurence);
		System.err.println("Name: " + functionDef.resolveName());
		System.err.println("END IDENTIFIERS: ");

		/*
		 * for(IASTModelElement element:editor.getSelectedElements()){
		 * if(element instanceof Identifier){ Identifier identifier =
		 * (Identifier) element; oldName=identifier.getName();
		 * System.err.println("Jubidubidei es isch ein Identifyer."); } else {
		 * // TODO: This happens if not an identifier is selected. //throw new
		 * IllegalStateException
		 * ("Only an Identifyer is allowed to be selected.");
		 * System.out.println(
		 * "Element hat den Type:"+element.getClass().getName());
		 * System.out.println
		 * ("The Identifier is:"+((IASTModelElement)element).getIdentifier()); }
		 * }
		 */
		pm.done();
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
