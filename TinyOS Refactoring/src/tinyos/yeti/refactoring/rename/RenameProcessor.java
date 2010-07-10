package tinyos.yeti.refactoring.rename;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.builder.ProjectResourceCollector;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ASTUtil;
import tinyos.yeti.refactoring.ASTUtil4Variables;
import tinyos.yeti.refactoring.ActionHandlerUtil;
import tinyos.yeti.refactoring.RefactoringPlugin;
import tinyos.yeti.refactoring.RefactoringPlugin.LogLevel;

public abstract class RenameProcessor extends org.eclipse.ltk.core.refactoring.participants.RenameProcessor {

	private RenameInfo info;
	private ASTUtil utility;
	private ITextSelection selection;
	private ASTUtil4Variables varUtil = new ASTUtil4Variables();

	public RenameProcessor(RenameInfo info) {
		super();
		this.info = info;

		selection = ActionHandlerUtil.getSelection(info.getEditor());
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return (getAstUtil() != null);
	}

	/**
	 * Often the first initialization of the Class is before the AST is ready.
	 * This getter makes sure the AST is used, as soon as it is available.
	 * 
	 * @return
	 */
	protected ASTUtil getAstUtil() {
		if (utility == null) {
			NesC12AST ast = (NesC12AST) info.getEditor().getAST();
			if (ast != null) {
				utility = new ASTUtil(ast);
			}
		}
		return utility;
	}

	/**
	 * 
	 * @return	The Currently Selected Identifier, null if not an Identifier is Selected.
	 */
	protected Identifier getSelectedIdentifier() {
		int selectionStart = getSelection().getOffset();
		return utility.getASTLeafAtPos(selectionStart,Identifier.class);
	}



	protected ITextSelection getSelection() {
		return selection;
	}


	protected ASTUtil4Variables getVarUtil() {
		return varUtil;
	}
	protected void addChanges4Identifiers(Collection<Identifier> identifiers,String newName,MultiTextEdit multiTextEdit,NesC12AST ast){
		ASTUtil util;
		if(ast==null){
			util=getAstUtil();
		}else{
			util=new ASTUtil(ast);
		}
		for (Identifier identifier : identifiers) {
			//TODO erase
			System.err.println("ID FOUND: "+identifier);
			int beginOffset = util.start(identifier);
			int endOffset = util.end(identifier);
			//TODO erase
			System.err.println("beginOffset "+beginOffset);
			System.err.println("endOffset "+endOffset);
			int length = endOffset - beginOffset;
			//TODO erase
			System.err.println("Länge der zu ersetzenden Indentifyer: "+length);
			multiTextEdit.addChild(new ReplaceEdit(beginOffset, length, newName));
		}
	}
	
	
	/**
	 * Returns the Ast for the currently selected Editor.
	 * @return
	 */
	protected NesC12AST getAst(){
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
	protected NesC12AST getAst(IFile iFile, IProgressMonitor monitor)
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

	/**
	 * Returns the projectModel of this project.
	 * @return
	 * @throws MissingNatureException
	 */
	protected ProjectModel getModel() throws MissingNatureException{
		return TinyOSPlugin.getDefault().getProjectTOS(info.getEditor().getProject()).getModel();
	}
	
	/**
	 * Returns all references which are found in the file.
	 * @param file
	 * @param monitor
	 * @return
	 * @throws MissingNatureException
	 */
	protected IASTReference[] getReferences(IFile file, IProgressMonitor monitor) throws MissingNatureException{
		ProjectModel model=getModel();
		return model.getReferences(model.parseFile(file), monitor);
	}
	
	/**
	 * Returns all references which are found in the file.
	 * @param file
	 * @param monitor
	 * @return
	 * @throws MissingNatureException
	 */
	protected IASTReference[] getReferences(IParseFile file, IProgressMonitor monitor) throws MissingNatureException{
		ProjectModel model=getModel();
		return model.getReferences(file, monitor);
	}
	
	/**
	 * The logical path of a "physical" path is the source where i.e. a declaration was found by the preprocessor.
	 * @param fPath
	 * @param monitor
	 * @return
	 * @throws MissingNatureException
	 */
	protected IASTModelPath getLogicalPath(IASTModelPath fPath,IProgressMonitor monitor) throws MissingNatureException{
		ProjectModel model=getModel();
		IASTModelNode node=model.getNode(fPath, monitor);
		if(node==null){
			System.err.println("Node of "+fPath+" is Null");
			return null;
		}
		return node.getLogicalPath();
	}
	
	/**
	 * Returns all files of this project.
	 * @return
	 * @throws CoreException
	 */
	protected Collection<IResource> getAllFiles() throws CoreException{
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
	
	/**
	 * Compares to IFileRegions
	 * @param a
	 * @param b
	 * @return
	 */
	protected boolean equalsIFileRegion(IFileRegion a,IFileRegion b){
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
}
