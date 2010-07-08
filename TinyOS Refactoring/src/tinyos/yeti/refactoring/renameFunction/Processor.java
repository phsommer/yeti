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
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.builder.ProjectResourceCollector;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
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

	//TODO IS COPY
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

	
	//TODO IS COPY
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
	private boolean containsOccurenceOfIdentifier(IDeclaration.Kind kind,IFile file,IASTModelPath fPath, IProgressMonitor monitor)
			throws MissingNatureException {
		System.err.println("Scanning File:"+file.getName());
		ProjectModel model = TinyOSPlugin.getDefault().getProjectTOS(info.getEditor().getProject()).getModel();

		IASTReference[] referenceArray = model.getReferences(model.parseFile(file), monitor);
		for (IASTReference ref : referenceArray) {
			//TODO
			if(ref.getTarget().toString().contains("machWas")){
			System.err.println("ref name:"+ref);
			System.err.println("ref target:"+ref.getTarget());
			}
			if (ref.getTarget().equals(fPath)) {
				return true;
			}
		}
		
		
//		List<IDeclaration> declarations = model.getDeclarations(kind);
////		System.err.println("#Declarations: "+declarations.size());
////		System.err.println("Looking for:"+fPath);
//		for(IDeclaration dec: declarations){
////			System.err.println(dec.getPath());
//			if(dec.getPath().equals(fPath)){
//				System.err.println("DA PATH: "+dec.getPath());
//				return true;
//			}
//		}
		return false;
	}

	
	//TODO IS COPY
	/**
	 * Returns the ASTModelPath of the identifier or null if the Identifier does
	 * not represent a identifier or the identifier is unknown
	 * 
	 * @param identifier
	 * @return
	 */
	private IASTModelPath getPathOfIdentifier(Identifier identifier) {
		try {
			IASTModelPath res;
			System.err.println(identifier.getName());
			if (ASTUtil4Functions.isFunctionCall(identifier)) {
				IdentifierExpression call= (IdentifierExpression) ASTUtil.getParentForName(identifier, IdentifierExpression.class);
				res= call.resolveField().getPath();
				//TODO erase
				System.err.println("isCALL");
				System.err.println(res);
				return res;
			}
			if (ASTUtil4Functions.isFunctionDefinition(identifier)) {
				FunctionDefinition definition= (FunctionDefinition) ASTUtil.getParentForName(identifier, FunctionDefinition.class);
				res= definition.resolveNode().getPath();
				//TODO erase
				System.err.println("isDEFINITON");
				System.err.println(res);
				return res;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
	
	
	private Collection<IFile> getFilesContainingIdentifier(Identifier identifier, IDeclaration.Kind kind,
			IProgressMonitor pm) throws CoreException {
		Collection<IResource> rescources = getAllFiles();
		IASTModelPath fPath= getPathOfIdentifier(identifier);
		LinkedList<IFile> files = new LinkedList<IFile>();
		for (IResource resource : rescources) {
			if (resource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;
				try {
					if (containsOccurenceOfIdentifier(kind,file, fPath, pm)) {
						//TODO
						System.err.println("Found one");
						System.err.println(file);
						files.add((IFile) resource);
					}
				} catch (MissingNatureException e) {
					throw new CoreException(new Status(IStatus.ERROR,
							RefactoringPlugin.PLUGIN_ID,
							"Refactoring was called while Plagin was not ready: "
									+ e.getMessage()));
				}
			}
		}
		return files;
	}

	
	//TODO IS COPY
	private MultiTextEdit renameAllOccurencesInFile(IFile file, IProgressMonitor pm) {

		MultiTextEdit multiTextEdit = new MultiTextEdit();

		// BEGIN: getting AST and handling Errors
		NesC12AST ast = null;
		try {
			ast = getAst(file, pm);
		} catch (IOException e) {
			RefactoringPlugin.getDefault().log(
					LogLevel.WARNING,
					e.getClass().getCanonicalName() + " while parsing File: "
							+ file.getFullPath().toOSString());
		} catch (MissingNatureException e) {
			RefactoringPlugin.getDefault().log(
					LogLevel.WARNING,
					e.getClass().getCanonicalName() + " while parsing File: "
							+ file.getFullPath().toOSString());
		} finally {
			if (ast == null) {
				return multiTextEdit;
			}
		}
		// This ASTUtil operates on the AST of the File that is now processed
		ASTUtil astUtil4File = new ASTUtil(ast);

		ASTNode astRoot = ast.getRoot();
		// END: getting AST and handling Errors

		Collection<Identifier> occurences = getVarUtil().getAllIdentifiers(
				astRoot, info.getOldName());

		// changing the name of all occurrences in the file
		for (Identifier occurece : occurences) {

			int beginOffset = astUtil4File.start(occurece);
			int endOffset = astUtil4File.end(occurece);
			int length = endOffset - beginOffset;

			multiTextEdit.addChild(new ReplaceEdit(beginOffset, length, info
					.getNewName()));
		}
		
		
		
		return multiTextEdit;
	}

	
	//TODO IS COPY
	@Override
	public Change createChange(IProgressMonitor pm) 
			throws CoreException,OperationCanceledException {
		CompositeChange ret = new CompositeChange("Rename Function "+ info.getOldName() + " to " + info.getNewName());
		Identifier selectedIdentifier = getSelectedIdentifier();
		Collection<IFile> files = getFilesContainingIdentifier(selectedIdentifier, Kind.FUNCTION,pm);
		//System.err.println("Looking for Identifier: "+selectedIdentifier.getName());
		//System.err.println("Identifier contained in Files: "+files.size());
		for (IFile file : files) {
			System.err.println(file.getFullPath());
//			MultiTextEdit mte = renameAllOccurencesInFile(file, pm);
//			if(mte.getChildren().length != 0){
//				String changeName = "Replacing Variable " + info.getOldName()
//				+ " with " + info.getNewName() + " in Document " + file;
//
//				TextChange textChange = new TextFileChange(changeName,
//						file);
//				textChange.setEdit(mte);
//				ret.add(textChange);
//			}
			
		}
		return ret;
//		return null;
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
