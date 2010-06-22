package tinyos.yeti.refactoring.renameLocalVariable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.refactoring.ASTUtil;
import tinyos.yeti.refactoring.ActionHandlerUtil;
import tinyos.yeti.refactoring.rename.RenameInfo;

public class RenameLocalVariableProcessor extends RenameProcessor {

	private RenameInfo info;
	private ITextSelection selection;
	private ASTUtil utility;

	public RenameLocalVariableProcessor(RenameInfo info) {
		super();
		this.info = info;

		selection = ActionHandlerUtil.getSelection(info.getEditor());
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws CoreException,
			OperationCanceledException {
	    RefactoringStatus ret = new RefactoringStatus();
	    if(info.getNewName() == null){
	    	ret.addFatalError("Please enter a new Name for the Variabel.");
	    }
	    
	    return ret;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if(!isApplicable()){
			ret.addFatalError("A local Variable must be selected.");
		}
	    IFile sourceFile = ActionHandlerUtil.getInputFile(info.getEditor());
	    if( sourceFile == null || !sourceFile.exists() ) {
	      ret.addFatalError( "The File you want wo Refactor, does not exist." );
	    } else if( sourceFile.isReadOnly() ) {
	      ret.addFatalError( "The File you want to Refactor is read only." );
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
		// If the AST Util is not available, the refactoring is not available
		if(getAstUtil() == null) return false;
		//Tests if a LOCAL Variable is selected
		Identifier identifier=getSelectedIdentifier();
		if(identifier==null)return false;
		CompoundStatement compound=findDeclaringCompoundStatement(identifier);
		return compound!=null;
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
	//Create The Changes
		MultiTextEdit multiTextEdit=new MultiTextEdit();
		String changeName="Replacing Variable " + info.getOldName() + " with "+ info.getNewName() + " in Document " + inputFile;
		TextChange renameOneOccurence = new TextFileChange(changeName,inputFile);
//		IDocument document=info.getEditor().getDocument();
//      TextChange renameOneOccurence = new DocumentChange(changeName,document);
		renameOneOccurence.setEdit(multiTextEdit);
		CompositeChange ret = new CompositeChange("Rename Local Variable "+ info.getOldName() + " to " + info.getNewName());
		ret.add(renameOneOccurence);
		Collection<Identifier> identifiers=this.selectedIdentifiersIfLocal();
		if(identifiers.size()==0){
			return new NullChange();
		}
		for (Identifier identifier : identifiers) {
			int beginOffset = getAstUtil().start(identifier);
			int endOffset=getAstUtil().end(identifier);
			int length = endOffset-beginOffset;
			multiTextEdit.addChild(new ReplaceEdit(beginOffset, length, info.getNewName()));
		}
		return ret;
	}
	
	/**
	 * 
	 * @return	The Currently Selected Identifier, null if not an Identifier is Selected.
	 */
	private Identifier getSelectedIdentifier(){
		int selectionStart = selection.getOffset();
		try{
			return utility.getASTLeafAtPos(selectionStart,Identifier.class);
		} catch (ClassCastException e) {
			return null;
		}
	}
	
	/**
	 * @param node
	 * @return the FunctionDefinition which encloses the given Node, null if the Node is not in a Function.
	 */
	private CompoundStatement getEnclosingCompound(ASTNode node) {
		ASTNode parent = ASTUtil.getParentForName(node,CompoundStatement.class);
		if (parent == null) {
			System.err.println("NOT IN A CompoundStatement!!!");
			return null;
		} else {
			return (CompoundStatement) parent;
		}
	}
	
	/**
	 * returns all CompoundStatement one level deeper in the tree then the given.
	 * This means that a CompoundStatement that is enclosed in a CompountStatement enclosed by the given CompoundStatement will not be added to the result.
	 * @param parent
	 * @return 
	 */
	private Collection<CompoundStatement> getEnclosedCompounds(ASTNode parent){
		List<CompoundStatement> result=new LinkedList<CompoundStatement>();
		getEnclosedCompounds_sub(parent, result);
		return result;
	}
	
	private void getEnclosedCompounds_sub(ASTNode parent,Collection<CompoundStatement> result){
		ASTNode child=null;
		for(int i=0;i<parent.getChildrenCount();++i){
			child=parent.getChild(i);
			if(child!=null){
				if(child instanceof CompoundStatement){
					result.add((CompoundStatement)child);
				} else {
					getEnclosedCompounds_sub(child,result);
				}
			}
		}
	}
	
	/**
	 * Returns the Identifiers in the given CompoundStatement which are not in a sub CompountStatement of the given CompountStatement and contain no own Declarator.
	 * @param compound
	 * @param identifierName
	 * @return The identifiers in the given Compound, which are not in a sub-Compound. EmptyList, if there are no identifiers, null if there are identifiers with an own declarator.
	 */
	private Collection<Identifier> getIdentifiersWithoutOwnDeclaration(CompoundStatement compound,String identifierName){
		Collection<Identifier> identifiers=ASTUtil.getIncludedIdentifiers(compound, identifierName,CompoundStatement.class);
		if(identifiers.size()==0){
			return Collections.emptyList();
		}
		if(getDeclaratorName(identifiers)==null){
			return identifiers;
		}else{
			return null;
		}
	}
	
	/**
	 * It is supposed, that the declaration is in the given CompoundStatement.
	 * To find the declaring CompoundStatement, use findDeclaringCompoundStatement.
	 * Then the Method collects all Occurrences of this identifier, which have no own declaration, in all subCompoundStatements.
	 * @param compount
	 * @param identifierName
	 * @param result
	 * @return
	 */
	private Collection<Identifier> getAllIdentifiers(CompoundStatement compound,String identifierName){
		//Add identifiers of the current Compound. This Compound must declare The identifier.
		Collection<Identifier> identifiers=ASTUtil.getIncludedIdentifiers(compound, identifierName, CompoundStatement.class);
		Collection<CompoundStatement> candidates=getEnclosedCompounds(compound);
		Collection<CompoundStatement> newCandidates=null;
		Collection<Identifier> currentIdentifiers=null;
		while(candidates.size()>0){
			newCandidates=new LinkedList<CompoundStatement>();
			currentIdentifiers=null;
			for(CompoundStatement candidate:candidates){
				currentIdentifiers=getIdentifiersWithoutOwnDeclaration(candidate, identifierName);
				if(currentIdentifiers!=null){	//If identifiers==null then there was an own declaration in the compound
					if(currentIdentifiers.size()>0){
						identifiers.addAll(currentIdentifiers);
					}
					newCandidates.addAll(getEnclosedCompounds(candidate));
				}
			}
			candidates=newCandidates;
		}
		return identifiers;
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
	 * Looks for the CompoundStatement which declares the given Identifier.
	 * @param identifier
	 * @return
	 */
	private CompoundStatement findDeclaringCompoundStatement(Identifier identifier){
		String name=identifier.getName();
		ASTNode child=identifier;
		CompoundStatement parent=null;
		Collection<Identifier> identifiers=null;
		boolean extendedToFunctionBorder=false;
		boolean foundDeclaration=false;
		//Search for the declaration in the current and upper CompountStatements, add all found Identifiers.
		while(!foundDeclaration){

			//Find Enclosing CompoundStatement
			parent = getEnclosingCompound(child);
			if(parent==null)return null;
			if(parent.getParent() instanceof FunctionDefinition){
				extendedToFunctionBorder=true;
			}
			//Get Identifiers in Compound with same Name
			identifiers=ASTUtil.getIncludedIdentifiers(parent, name,CompoundStatement.class);

			//Check if the declaration is in the actual compound statement. If so, this is a local variable
			if(getDeclaratorName(identifiers)!=null){
				foundDeclaration=true;
			}
			else{
				if(extendedToFunctionBorder&&!foundDeclaration){	//This is not a local variable
					return null;
				} 
			}
			//Maybe the declaration of the variable is in an CompountStatement outside the actual one but inside the FunctionDefinition-->Do another round
			child=parent;
		}
		return parent;
	}
	
	
	/**
	 * Returns a list which contains all occurrences of the selected identifier.
	 * Checks if the selection is an identifier and if so if it is part of a local variable. 
	 * @return All Occurrences in the Method of the selected identifier, immutable EmptyList if the above checks fail.
	 */
	private Collection<Identifier> selectedIdentifiersIfLocal(){
		//Find currently selected Element
		Identifier currentlySelected=getSelectedIdentifier();
		if(currentlySelected==null)	//The Selection is not an Identifier
			return Collections.emptyList();
		
		//Find the CompoundStatement which declares the identifier
		CompoundStatement declaringCompound=findDeclaringCompoundStatement(currentlySelected);
		if(declaringCompound==null){	//Declaration is not within Function.
			return Collections.emptyList();
		}
		Collection<Identifier> identifiers=getAllIdentifiers(declaringCompound, currentlySelected.getName());
		return identifiers;
	}
	
	
	/**
	 * Often the first initialization of the Class is before the AST is ready.
	 * This getter makes sure the AST is used, as soon as it is available.
	 * @return
	 */
	private ASTUtil getAstUtil(){
		if(utility == null){
			NesC12AST ast=(NesC12AST) info.getEditor().getAST();
			if(ast != null){
				utility=new ASTUtil(ast);
			}
		}
		return utility;
	}

}
