package tinyos.yeti.refactoring.renameFunction;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
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

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
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
	 */
	private List<Identifier> getReferencingIdentifiersInFileForTargetPath(IFile file,IASTModelPath path, IProgressMonitor monitor)
	throws MissingNatureException, IOException {
		//Gather all sources which reference this path
		System.err.println("Scanning File:"+file.getName());
		IASTModelPath candidatePath;
		IASTReference[] referenceArray = getReferences(file,monitor);
		List<IASTReference> matchingSources=new LinkedList<IASTReference>();
		for (IASTReference ref : referenceArray) {
			candidatePath=getLogicalPath(ref.getTarget(), monitor);
			if(candidatePath!=null){
				if (candidatePath.equals(path)) {
					matchingSources.add(ref);
					System.err.println("\tMatch found");
				}
			}else{
				//TODO erase
				System.err.println("Node is NULL!");
			}
		}

		//TODO erase
		System.err.println("Found Sources: "+matchingSources.size());
		
		//Find Identifiers which are part of the given Source.
		IFileRegion region;
		ASTUtil astUtil=new ASTUtil(getAst(file,monitor));
		List<Identifier> identifiers=new LinkedList<Identifier>();
		for(IASTReference ref:matchingSources){
			//TODO erase
			System.err.println(ref.getSource());
			region=ref.getSource();
			ASTNode node=astUtil.getASTLeafAtPos(region.getOffset());
			if(node instanceof Identifier){
				Identifier id=(Identifier)node;
				//TODO erase
				System.err.println("Node Id Name: "+id.getName());
			}else{
				System.err.println("Source Node is no Identifier ");
				System.err.println("Source Node: "+node.getASTNodeName());
			}
			System.err.println("Node Offset: "+region.getOffset());
			Identifier identifier=(Identifier)node;
			identifiers.add(identifier);
		}
		return identifiers;

		//		List<IDeclaration> declarations = model.getDeclarations(kind);
		//		System.err.println("#Declarations: "+declarations.size());
		//		System.err.println("Looking for:"+fPath);
		//		for(IDeclaration dec: declarations){
		//			System.err.println();
		//			if(model.getNode(dec.getPath(),monitor).getLogicalPath().equals(targetPath)){
		//				System.err.println("Found Dec");
		//				return true;
		//			}
		//		}
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
		System.err.println("call is in file: "+pFile);
		IASTReference[] referenceArray = getReferences(pFile,monitor);
		for(IASTReference ref:referenceArray){
			if(equalsIFileRegion(ref.getSource(),ast.getRegion(node))){
				return ref.getTarget();
			}
		}
		System.err.println("No Match Found!");
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
		System.err.println(identifier.getName());

		switch(selectedFunctionPart){

		case DECLARATION:
			InitDeclarator id = (InitDeclarator) ASTUtil.getParentForName(identifier, InitDeclarator.class);
			res = id.resolveField().getPath();
			System.err.println("isDECLARATION");
			break;

		case DEFINITION:
			//TODO Find the reference to the declaration of a function definition
			FunctionDefinition definition= (FunctionDefinition) ASTUtil.getParentForName(identifier, FunctionDefinition.class);
			res= definition.resolveNode().getPath();
			//TODO erase
			System.err.println("isDEFINITON");
			break;

		case CALL:
			IdentifierExpression call= (IdentifierExpression) ASTUtil.getParentForName(identifier, IdentifierExpression.class);
			IASTModelPath path= call.resolveField().getPath();
			res=getDeclaringPath(call, path, pm);
			//TODO erase
			System.err.println("isCALL");
			break;

		}
		addOutput("Target: "+getLogicalPath(res, pm));
		return getLogicalPath(res, pm);
	}

	/**
	 * Adds the changes for the identifiers to ret.
	 * @param identifiers
	 * @param mte
	 * @param ast
	 * @param file
	 * @param ret
	 */
	private void addChange(List<Identifier> identifiers,MultiTextEdit mte,NesC12AST ast,IFile file,CompositeChange ret){
		addChanges4Identifiers(identifiers, info.getNewName(), mte,ast);
		String changeName = "Replacing Function Name " + info.getOldName()+ " with " + info.getNewName() + " in Document " + file;
		TextChange textChange = new TextFileChange(changeName,file);
		textChange.setEdit(mte);
		ret.add(textChange);
	}
	
	private Change createChanges(Identifier identifier,IProgressMonitor monitor) 
	throws CoreException, MissingNatureException, IOException {
		CompositeChange ret = new CompositeChange("Rename Function "+ info.getOldName() + " to " + info.getNewName());
		MultiTextEdit mte;
		List<Identifier> identifiers;
		
		//Get the basic declaration, which declares the selected function
		IASTModelPath basicDeclarationPath= getPathOfReferencedDeclaration(identifier,monitor);
		
		//Get the Identifier of the basic declaration
		IFileRegion targetRegion=getModel().getNode(basicDeclarationPath, monitor).getRegion();
		IFile targetFile=parseFileToIFile(targetRegion.getParseFile());
		NesC12AST ast=getAst(targetFile,monitor);
		ASTUtil astUtil=new ASTUtil(ast);
		Identifier targetIdentifier=(Identifier)astUtil.getASTLeafAtPos(targetRegion.getOffset());
		mte=new MultiTextEdit();
		identifiers=new LinkedList<Identifier>();
		identifiers.add(targetIdentifier);
		addChange(identifiers,mte,ast,targetFile,ret);
		
		//Get the identifiers of function parts, which reference the basic declaration.
		for (IFile file : getAllFiles()) {
			ast=getAst(file, monitor);
			mte=new MultiTextEdit();
			identifiers=getReferencingIdentifiersInFileForTargetPath(file, basicDeclarationPath, monitor);
			if(identifiers.size()>0){
				addChange(identifiers,mte,ast,file,ret);
			}
		}
		System.err.println(endOutput);
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
		return ret;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,CheckConditionsContext context) 
	throws CoreException,OperationCanceledException {
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
