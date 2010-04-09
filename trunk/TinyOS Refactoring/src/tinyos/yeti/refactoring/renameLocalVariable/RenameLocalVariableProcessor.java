package tinyos.yeti.refactoring.renameLocalVariable;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
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
	    RefactoringStatus result = new RefactoringStatus();
	    IFile sourceFile = info.getInputFile();
	    if( sourceFile == null || !sourceFile.exists() ) {
	      result.addFatalError( "The File you want wo Refactor, does not exist." );
	    } else if( sourceFile.isReadOnly() ) {
	      result.addFatalError( "The File you want to Refactor is read only." );
	    } 

	    return result;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if(getSelectedIdentifier() != null){
			ret.addFatalError("An Identifier must be selected.");
		}
		return new RefactoringStatus();
	}
	
	
	/**
	 * 
	 * @return	The Currently Selected Identifier, null if not an Identifier is Selected.
	 */
	private Identifier getSelectedIdentifier(){
		int selectionStart = selection.getOffset();
		ASTNode currentlySelected = utility.getASTLeafAtPos(selectionStart);
		
		if(currentlySelected instanceof Identifier){
			return (Identifier) currentlySelected;
		}else{
			System.err.println("SELECTION IS NOT AN IDENTIFIER!!!");
			return null;
		}
	}
	
	/**
	 * @param node
	 * @return the FunctionDefinition which encloses the given Node, null if the Node is not in a Function.
	 */
	private FunctionDefinition getEnclosingFunction(ASTNode node) {
		ASTNode parent = ASTUtil.getParentForName(node,FunctionDefinition.class);
		if (parent == null) {
			return null;
		} else {
			System.err.println("NOT IN A FUNCTION!!!");
			return (FunctionDefinition) parent;
		}
	}
	
	/**
	 * This Method can be used to Check, if these identifiers are part of a local variable.
	 * @param identifiers
	 * @return Returns a DeclaratorName, if some of the given Identifiers has such a parent. Null if there is no such parent.
	 */
	private DeclaratorName getDeclaratorName(Collection<Identifier> identifiers){
		ASTNode candidate=null;
		for(Identifier identifier:identifiers){
			candidate=ASTUtil.getParentForName(identifier, DeclaratorName.class);
			if(candidate!=null){
				return (DeclaratorName)candidate;
			}
		}
		return null;
	}
	

	/**
	 * Returns a list which contains all occurrences of the selected identifier.
	 * Checks if the selection is an identifier and if so if it is part of a local variable. 
	 * @return All Occurrences in the Method of the selected identifier, immutable EmptyList if the above checks fail.
	 */
	private Collection<Identifier> selectedIdentifiersIfLocal(){
		//setup
		utility = new ASTUtil(ast);
		
		
		
	//Find currently selected Element
		Identifier currentlySelected=getSelectedIdentifier();
		if(currentlySelected==null)return Collections.emptyList();;
		
		
	//Find Enclosing Function Definition
		FunctionDefinition parent = getEnclosingFunction(currentlySelected);
		if(parent==null)return Collections.emptyList();;
		
	//Get Identifiers in Function with same Name
		 Collection<Identifier> identifiers=ASTUtil.getIncludedIdentifiers(parent, currentlySelected.getName());

	//Check if this is a Local Variable.
		 if(getDeclaratorName(identifiers)==null)return Collections.emptyList();
		 return identifiers;
	}
	

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		

		IFile inputFile = info.getInputFile();
		
	//Create The Changes
		MultiTextEdit multiTextEdit=new MultiTextEdit();
		TextChange renameOneOccurence = new TextFileChange(
				"Replacing Variable " + info.getOldName() + " with "
						+ info.getNewName() + " in File " + inputFile,inputFile);
		renameOneOccurence.setEdit(multiTextEdit);
		CompositeChange ret = new CompositeChange("Rename Local Variable "+ info.getOldName() + " to " + info.getNewName());
		ret.add(renameOneOccurence);
		Collection<Identifier> identifiers=this.selectedIdentifiersIfLocal();
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
