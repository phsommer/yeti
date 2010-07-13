package tinyos.yeti.refactoring.renameFunction;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.declarations.FieldDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ASTUtil;
import tinyos.yeti.refactoring.rename.RenameProcessor;

public class Processor extends RenameProcessor {

	private Info info;
	private FunctionPart selectedFunctionPart;	//The function part type of the selection

	public Processor(Info info) {
		super(info);
		this.info = info;
	}

	/**
	 * Returns the identifiers which are part of a reference to the given path.
	 * @param file	The file in which we are looking for referencing sources.
	 * @param path	The logical Path for which we search referencing sources.
	 * @param monitor
	 * @return	The identifiers contained in the sources which reference the given path.
	 * @throws MissingNatureException
	 * @throws IOException
	 * @throws CoreException 
	 */
	private List<Identifier> getReferencingIdentifiersInFileForTargetPath(IFile file,IASTModelPath path, IProgressMonitor monitor)
	throws MissingNatureException, IOException, CoreException {
		//Gather all sources which reference this path
		IASTModelPath candidatePath;
		IASTReference[] referenceArray = getReferences(file,monitor);
		List<IASTReference> matchingSources=new LinkedList<IASTReference>();
		for (IASTReference ref : referenceArray) {
			candidatePath=getLogicalPath(ref.getTarget(), monitor);
			if(candidatePath!=null){
				if (candidatePath.equals(path)) {
					matchingSources.add(ref);
				}
			}
		}
		
		//Find Identifiers which are part of the given Source.
		IFileRegion region;
		ASTUtil astUtil=new ASTUtil(getAst(file,monitor));
		List<Identifier> identifiers=new LinkedList<Identifier>();
		for(IASTReference reference:matchingSources){
			region=reference.getSource();
			ASTNode node=astUtil.getASTLeafAtPos(region.getOffset());
			Identifier identifier=(Identifier)node;
			identifiers.add(identifier);
		}
		return identifiers;
	}
	
	private boolean getFilesContainingDeclarationForPath(IASTModelPath path,IProgressMonitor monitor, CompositeChange ret) 
	throws MissingNatureException, CoreException, IOException{
		//Get Declarations for this path
		ProjectModel model=getModel();
		List<IDeclaration> declarations = model.getDeclarations(Kind.FUNCTION);
		addOutput("#Declarations: "+declarations.size());
		addOutput("Looking for:"+path);
		NesC12AST ast;
		ASTUtil astUtil;
		Identifier functionDefinition=null;
		for(IDeclaration dec: declarations){
			IASTModelNode node=model.getNode(dec.getPath(),monitor);
				addOutput("Candidate Node: "+node.getIdentifier());
				addOutput("\t"+node.getLogicalPath());
				if(node.getLogicalPath().equals(path)){
					IFile file=super.getIFile4ParseFile(dec.getParseFile());
					FieldDeclaration functionDeclaration=(FieldDeclaration)dec;
					if(functionDeclaration.getFileRegion()==null){
						addOutput("FILE REGION IS NULL");
					}else{
						addOutput("FILE REION NOT NULL");
					}
					addOutput("Found Definitiion in File: "+file.getName().toString());
					ast=getAst(file,monitor);
					Collection<FunctionDefinition> definitions=ASTUtil.getAllNodesOfType(ast.getRoot(), FunctionDefinition.class);
					addOutput("Definitions in this file:");
					for(FunctionDefinition def:definitions){
						addOutput("\t"+def.getASTNodeName());
					}
//					astUtil=new ASTUtil(ast);
//					functionDefinition=(Identifier)astUtil.getASTLeafAtPos(	node.getRegion().getOffset());
//					addChange(functionDefinition, ast, file, ret);
					return true;
			}
		}
		return false;
	}

	/**
	 * Tries to find the Declaration of the given function call.
	 * @param node
	 * @param path
	 * @param monitor
	 * @return
	 * @throws MissingNatureException
	 */
	private IASTModelPath getDeclaringPath(ASTNode node,IASTModelPath path, IProgressMonitor monitor) 
	throws MissingNatureException{
		NesC12AST ast=getAst();
		IParseFile pFile=path.getParseFile();
		IASTReference[] referenceArray = getReferences(pFile,monitor);
		for(IASTReference ref:referenceArray){
			if(equalsIFileRegion(ref.getSource(),ast.getRegion(node))){
				return ref.getTarget();
			}
		}
		return null;
	}

	//TODO IS COPY
	/**
	 * Returns the Logical ASTModelPath of the declaration which is referenced by this identifier.
	 * 
	 * @param identifier
	 * @return
	 * @throws MissingNatureException 
	 */
	private IASTModelPath getPathOfReferencedDeclaration(Identifier identifier,IProgressMonitor pm) 
	throws MissingNatureException {
		IASTModelPath res=null;

		switch(selectedFunctionPart){

		case DECLARATION:
			InitDeclarator id = (InitDeclarator) ASTUtil.getParentForName(identifier, InitDeclarator.class);
			res = id.resolveField().getPath();
			addOutput("isDECLARATION");
			
			break;

		case DEFINITION:
			//TODO Find the reference to the declaration of a function definition
			FunctionDefinition definition= (FunctionDefinition) ASTUtil.getParentForName(identifier, FunctionDefinition.class);
			res= definition.resolveNode().getPath();
			//TODO erase
			addOutput("isDEFINITON");
			break;

		case CALL:
			IdentifierExpression call= (IdentifierExpression) ASTUtil.getParentForName(identifier, IdentifierExpression.class);
			IASTModelPath path= call.resolveField().getPath();
			res=getDeclaringPath(call, path, pm);
			//TODO erase
			addOutput("isCALL");
			break;

		}
		res=getLogicalPath(res,pm);
		return res;
	}

	/**
	 * Adds the changes for the identifier to ret.
	 * @param identifier
	 * @param mte
	 * @param ast
	 * @param file
	 * @param ret
	 */
	private void addChange(Identifier identifier,NesC12AST ast,IFile file,CompositeChange ret){
		List<Identifier> identifiers=new LinkedList<Identifier>();
		identifiers.add(identifier);
		addChange(identifiers, ast, file, ret);
	}
	
	/**
	 * Adds the changes for the identifiers to ret.
	 * @param identifiers
	 * @param mte
	 * @param ast
	 * @param file
	 * @param ret
	 */
	private void addChange(List<Identifier> identifiers,NesC12AST ast,IFile file,CompositeChange ret){
		MultiTextEdit mte=new MultiTextEdit();
		addChanges4Identifiers(identifiers, info.getNewName(),mte,ast);
		String changeName = "Replacing Function Name " + info.getOldName()+ " with " + info.getNewName() + " in Document " + file;
		TextChange textChange = new TextFileChange(changeName,file);
		textChange.setEdit(mte);
		ret.add(textChange);
	}
	
	private Change createChanges(Identifier identifier,IProgressMonitor monitor) 
	throws CoreException, MissingNatureException, IOException {
		CompositeChange ret = new CompositeChange("Rename Function "+ info.getOldName() + " to " + info.getNewName());
		List<Identifier> identifiers;
		
		//Get the basic declaration, which declares the selected function
		IASTModelPath basicDeclarationPath= getPathOfReferencedDeclaration(identifier,monitor);
		
		//Get the Identifier of the basic declaration and add it to the change
		getFilesContainingDeclarationForPath(basicDeclarationPath, monitor,ret);
		Identifier targetIdentifier=super.getIdentifierForPath(basicDeclarationPath, monitor);
		IFileRegion targetRegion=getModel().getNode(basicDeclarationPath, monitor).getRegion();
		IFile targetFile=getIFile4ParseFile(targetRegion.getParseFile());
		NesC12AST ast=getAst(targetFile,monitor);
		addChange(targetIdentifier,ast,targetFile,ret);
		
		//Get the identifiers of function parts, which reference the basic declaration and add them to the change.
		for (IFile file : getAllFiles()) {
			ast=getAst(file, monitor);
			identifiers=getReferencingIdentifiersInFileForTargetPath(file, basicDeclarationPath, monitor);
			if(identifiers.size()>0){
				addChange(identifiers,ast,file,ret);
			}
		}
		return ret;
	}

	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		Change ret;
		Identifier selectedIdentifier = getSelectedIdentifier();
		selectedFunctionPart=ASTUtil4Functions.identifyFunctionPart(selectedIdentifier);
		try {
			ret=createChanges(selectedIdentifier,pm);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret= new NullChange();
		}
		//TODO erase
		System.err.println(endOutput);
		return ret;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,CheckConditionsContext context) 
	throws CoreException,OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if (!isApplicable()) {
			ret.addFatalError("The Refactoring is not Applicable");
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
		if (!ASTUtil4Functions.isFunction(getSelectedIdentifier())) {
			ret.addFatalError("No Function Name selected.");
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
		return "tinyos.yeti.refactoring.renameFunction.Processor";
	}

	@Override
	public String getProcessorName() {
		return info.getInputPageName();
	}

	@Override
	public boolean isApplicable() 
	throws CoreException {
		return super.isApplicable();
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,SharableParticipants sharedParticipants) 
	throws CoreException {
		// TODO Auto-generated method stub
		return new RefactoringParticipant[] {};
	}

}
