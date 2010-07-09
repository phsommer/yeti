package tinyos.yeti.refactoring.renameFunction;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
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
		if (!ASTUtil4Functions.isFunction(getSelectedIdentifier())) {
			ret.addFatalError("No Function Name selected.");
		}
		return ret;
	}

	/**
	 * Returns the Ast for the currently selected Editor.
	 * @return
	 */
	private NesC12AST getAst(){
		return (NesC12AST)info.getEditor().getAST();
	}
	
	/**
	 * Returns the Ast for the given IFile.
	 * @param iFile
	 * @param monitor
	 * @return
	 * @throws IOException
	 * @throws MissingNatureException
	 */
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

	
	private ProjectModel getModel() throws MissingNatureException{
		return TinyOSPlugin.getDefault().getProjectTOS(info.getEditor().getProject()).getModel();
	}
	
	private IASTReference[] getReferences(IFile file, IProgressMonitor monitor) throws MissingNatureException{
		ProjectModel model=getModel();
		return model.getReferences(model.parseFile(file), monitor);
	}
	private IASTReference[] getReferences(IParseFile file, IProgressMonitor monitor) throws MissingNatureException{
		ProjectModel model=getModel();
		return model.getReferences(file, monitor);
	}
	
	private IASTModelPath getLogicalPath(IASTModelPath fPath,IProgressMonitor monitor) throws MissingNatureException{
		ProjectModel model=getModel();
		IASTModelNode node=model.getNode(fPath, monitor);
		if(node==null){
			System.err.println("Node of "+fPath+" is Null");
			return null;
		}
		return node.getLogicalPath();
	}
	

	
	//TODO IS COPY
	/**
	 * Returns the Logical ASTModelPath of the declaration which is referenced by this identifier.
	 * 
	 * @param identifier
	 * @return
	 * @throws MissingNatureException 
	 */
	private IASTModelPath getPathOfReferencedDeclaration(Identifier identifier,IProgressMonitor pm) throws MissingNatureException {
		IASTModelPath res=null;
		System.err.println(identifier.getName());
		if (ASTUtil4Functions.isFunctionDeclaration(identifier)) {
			InitDeclarator id = (InitDeclarator) ASTUtil.getParentForName(identifier, InitDeclarator.class);
			res = id.resolveField().getPath();
			System.err.println("isDECLARATION");
		}
		//TODO Find the reference to the declaration of a function definition
		else if (ASTUtil4Functions.isFunctionDefinition(identifier)) {
			FunctionDefinition definition= (FunctionDefinition) ASTUtil.getParentForName(identifier, FunctionDefinition.class);
			res= definition.resolveNode().getPath();
			//TODO erase
			System.err.println("isDEFINITON");
		}
		else if (ASTUtil4Functions.isFunctionCall(identifier)) {
			IdentifierExpression call= (IdentifierExpression) ASTUtil.getParentForName(identifier, IdentifierExpression.class);
			IASTModelPath path= call.resolveField().getPath();
			res=getDeclaringPath(call, path, pm);
			//TODO erase
			System.err.println("isCALL");
		}
		return getLogicalPath(res, pm);
	}
	
	private IASTModelPath getDeclaringPath(ASTNode node,IASTModelPath path, IProgressMonitor monitor) throws MissingNatureException{
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
	
	private boolean equalsIFileRegion(IFileRegion a,IFileRegion b){
		if(!a.getParseFile().equals(b.getParseFile()))
			return false;
		if(a.getLength()!=b.getLength())
			return false;
		if(a.getLine()!=b.getLine())
			return false;
		if(a.getOffset()!=b.getOffset())
			return false;
		return true;
	}

	
	private Collection<IResource> getAllFiles() throws CoreException{
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
		
		return collector.resources;
	}

	private Change createChanges(Identifier identifier, IDeclaration.Kind kind,IProgressMonitor pm) throws CoreException, MissingNatureException {
		Collection<IResource> rescources = getAllFiles();
		IASTModelPath fPath= getPathOfReferencedDeclaration(identifier,pm);
		CompositeChange ret = new CompositeChange("Rename Function "+ info.getOldName() + " to " + info.getNewName());
		List<Identifier> identifiers=new LinkedList<Identifier>();
		NesC12AST ast;
		for (IResource resource : rescources) {
			if (resource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;
				try {
					ast=getAst(file, pm);
					MultiTextEdit mte=new MultiTextEdit();
					identifiers=getReferencingIdentifiersInFile(kind, file, fPath, pm);
					if(identifiers.size()>0){
						super.addChanges4Identifiers(identifiers, info.getNewName(), mte,ast);
						String changeName = "Replacing Function Name " + info.getOldName()+ " with " + info.getNewName() + " in Document " + file;
						TextChange textChange = new TextFileChange(changeName,file);
						textChange.setEdit(mte);
						ret.add(textChange);
					}

				} catch (MissingNatureException e) {
					throw new CoreException(new Status(IStatus.ERROR,RefactoringPlugin.PLUGIN_ID,"Refactoring was called while Plagin was not ready: "+ e.getMessage()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param file
	 *            The File which you would like to know whether the variable
	 *            occurs in
	 * @param identifier
	 *            The Identifier which represents the occurences in the currently
	 *            edited File
	 * @param monitor
	 * @return
	 * @throws MissingNatureException
	 */
	private List<Identifier> getReferencingIdentifiersInFile(IDeclaration.Kind kind,IFile file,IASTModelPath logicalPath, IProgressMonitor monitor)
			throws MissingNatureException {
			System.err.println("Scanning File:"+file.getName());
			IASTModelPath candidatePath;
			IASTReference[] referenceArray = getReferences(file,monitor);
			List<IASTReference> matchingSources=new LinkedList<IASTReference>();
			for (IASTReference ref : referenceArray) {
				candidatePath=getLogicalPath(ref.getTarget(), monitor);
				if(candidatePath!=null){
					if (candidatePath.equals(logicalPath)) {
						matchingSources.add(ref);
						System.err.println("\tMatch found");
					}
				}else{
					//TODO erase
					System.err.println("Node is NULL!");
				}
			}
			
			
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
		//TODO erase
			System.err.println("Found Sources: "+matchingSources.size());
			IFileRegion region;
			ASTUtil astUtil=new ASTUtil(getAst());
			List<Identifier> identifiers=new LinkedList<Identifier>();
			for(IASTReference ref:matchingSources){
				System.err.println(ref.getSource());
				region=ref.getSource();
				ASTNode node=astUtil.getASTLeafAtPos(region.getOffset());
				System.err.println("Source Node: "+node.getASTNodeName());
				Identifier identifier=(Identifier)node;
				identifiers.add(identifier);
			}
		return identifiers;
	}

	
	//TODO IS COPY
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,OperationCanceledException {
		Change ret;
		Identifier selectedIdentifier = getSelectedIdentifier();
		try {
			ret=createChanges(selectedIdentifier, Kind.FUNCTION,pm);
		} catch (MissingNatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret= new NullChange();
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
