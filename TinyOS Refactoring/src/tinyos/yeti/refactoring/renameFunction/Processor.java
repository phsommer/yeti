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
import tinyos.yeti.ep.parser.IASTModelAttribute;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.model.ProjectModel;
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
	 * @throws CoreException 
	 */
	private List<Identifier> getReferencingIdentifiersInFileForTargetPath(IFile file,IASTModelPath path, IProgressMonitor monitor)
	throws MissingNatureException, IOException, CoreException {
		//Gather all sources which reference this path
		IASTModelPath candidatePath;
		IASTReference[] referenceArray = getReferences(file,monitor);
		List<IASTReference> matchingSources=new LinkedList<IASTReference>();
		addOutput("Ref:");
		for (IASTReference ref : referenceArray) {
			if(ref.getTarget().toString().contains("KERMIT")){
				addOutput(ref.getTarget().toString());
			}
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

	/**
	 * Adds the Change for the Identifier of the function definition with some kind of magic.
	 * @param path	The path of the basic declaration
	 * @param monitor
	 * @param ret	The Change in which the new Change has to be placed.
	 * @return
	 * @throws MissingNatureException
	 * @throws CoreException
	 * @throws IOException
	 */
	private boolean addChange4FunctionDefinition(IASTModelPath path,IProgressMonitor monitor, CompositeChange ret) 
	throws MissingNatureException, CoreException, IOException{
		//Get Declarations for this path, which are all function definitions
		addOutput("The search Path: "+path.toString());
		ProjectModel model=getModel();
		List<IDeclaration> declarations = model.getDeclarations(Kind.FUNCTION);
		NesC12AST ast;
		for(IDeclaration dec: declarations){
			IASTModelNode node=model.getNode(dec.getPath(),monitor);
			//If the declaration references the target declaration, Search the declaring file for the matching function definition
			if(node.getLogicalPath().equals(path)){
				IFile file=getIFile4ParseFile(dec.getParseFile());
				ast=getAst(file,monitor,model.getModel(dec.getParseFile(), true, monitor));
				Collection<FunctionDefinition> definitions=ASTUtil.getAllNodesOfType(ast.getRoot(), FunctionDefinition.class);
				addOutput("searching definitions in file:"+file.getName());
				addOutput("definitions: "+definitions.size());
				for(FunctionDefinition def:definitions){
					IASTModelNode functionNodeCandidate=def.resolveNode();
					if(functionNodeCandidate==null){
						addOutput("NULL is it");
					}
					if(functionNodeCandidate!=null){
						addOutput("One is not null");
						//This statement gets the basic declaration out of a function definition. 
						//The logical path of the definition references the included declaration, whichs logical path in turn references the basic declaration.
						IASTModelNode declaringNode=getModel().getNode(functionNodeCandidate.getLogicalPath(),monitor);
						IASTModelPath basicPath=declaringNode.getLogicalPath();
						addOutput("The Candidate: "+functionNodeCandidate);
						addOutput("The BasicNode: "+declaringNode);
						addOutput("Candidate path: "+functionNodeCandidate.getLogicalPath());
						addOutput("The basic Path: "+basicPath);
						if(functionNodeCandidate!=null&&path.equals(basicPath)){
							Identifier functionIdentifier=(Identifier)def.getDeclarator().getChild(0).getChild(0);
							addChange(functionIdentifier, ast, file, ret);
							return true;
						}
					}
				}
				addOutput("didnt find matching definition in file");
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
	private IASTModelPath getPathOfReferencedDeclaration(Identifier identifier,IProgressMonitor monitor) 
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
			res=getDeclaringPath(call, path, monitor);
			//TODO erase
			addOutput("isCALL");
			break;

		}
		res=getLogicalPath(res,monitor);
		addOutput("Selected Node: "+getModel().getNode(res, monitor));
		addOutput("logical Path: "+res);
		addOutput("eager resolved: "+eagerResolveLogicalPath(res,monitor).toString());
		return res;
	}
	
	private IASTModelPath eagerResolveLogicalPath(IASTModelPath path,IProgressMonitor monitor) 
	throws MissingNatureException{
		ProjectModel model=getModel();
		IASTModelPath oldPath=null;
//		while(!path.equals(oldPath)){
		for(int i=0;i<10;++i){
			addOutput("path: "+path);
			oldPath=path;
			path=model.getNode(oldPath, monitor).getLogicalPath();
		}
		return path;
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
		//Add the change for the function definition
		addChange4FunctionDefinition(basicDeclarationPath, monitor,ret);
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
