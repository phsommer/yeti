package tinyos.yeti.refactoring.rename;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public abstract class RenameProcessor extends org.eclipse.ltk.core.refactoring.participants.RenameProcessor {
	
	private RenameInfo info;
	private ASTUtil4Variables varUtil = new ASTUtil4Variables();

	public RenameProcessor(RenameInfo info) {
		super();
		this.info = info;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return (info.getAstPositioning() != null);
	}



	/**
	 * 
	 * @return	The Currently Selected Identifier, null if not an Identifier is Selected.
	 */
	protected Identifier getSelectedIdentifier() {
		ITextSelection selection=info.getSelection();
		return info.getAstPositioning().getASTLeafAtPos(selection.getOffset(),selection.getLength(),Identifier.class);
	}



	protected ASTUtil4Variables getVarUtil() {
		return varUtil;
	}
	
	protected void addChanges4Identifiers(Collection<Identifier> identifiers,String newName,MultiTextEdit multiTextEdit,NesC12AST ast){
		ASTPositioning util;
		if(ast==null){
			util=info.getAstPositioning();
		}else{
			util=new ASTPositioning(ast);
		}
		for (Identifier identifier : identifiers) {
			int beginOffset = util.start(identifier);
			int endOffset = util.end(identifier);
			int length = endOffset - beginOffset;
			multiTextEdit.addChild(new ReplaceEdit(beginOffset, length, newName));
		}
	}
	

	
	/**
	 * Parses a given File and returns the Parser.
	 */
	protected Parser getParser(IFile iFile, IProgressMonitor monitor) throws IOException, MissingNatureException{
		ProjectUtil projectUtil=new ProjectUtil(info.getEditor());
		return projectUtil.getParser(iFile, monitor);
	}
	
	protected NesC12AST getAst(IFile iFile, IProgressMonitor monitor) throws IOException, MissingNatureException{
		return info.getProjectUtil().getAst(iFile, monitor);
	}
	
	/**
	 * Returns the Ast for the given IFile and loads the given IASTModel for it.
	 * @param iFile
	 * @param monitor
	 * @return
	 * @throws IOException
	 * @throws MissingNatureException
	 */
	protected NesC12AST getAst(IFile iFile, IProgressMonitor monitor, IASTModel model)
			throws IOException, MissingNatureException {
		// Create Parser for File to construct an AST
		IProject project = info.getEditor().getProject();
		ProjectModel projectModel = TinyOSPlugin.getDefault().getProjectTOS(project).getModel();

		File file = iFile.getLocation().toFile();
		IParseFile parseFile = projectModel.parseFile(file);

		INesCParser parser = projectModel.newParser(parseFile, null, monitor);
		parser.setCreateAST(true);
		parser.setASTModel(model);
		parser.parse(new FileMultiReader(file), monitor);

		return (NesC12AST) parser.getAST();
	}

	/**
	 * Returns the projectModel of this project.
	 * @return
	 * @throws MissingNatureException
	 */
	protected ProjectModel getModel() throws MissingNatureException{
		ProjectUtil projectUtil=new ProjectUtil(info.getEditor());
		return projectUtil.getModel();
	}
	
	/**
	 * Returns all references which are found in the file.
	 * Empty Array if there are no references.
	 * @param file
	 * @param monitor
	 * @return
	 * @throws MissingNatureException
	 */
	protected IASTReference[] getReferences(IFile file, IProgressMonitor monitor) throws MissingNatureException{
		ProjectModel model=getModel();
		IASTReference[] references=model.getReferences(model.parseFile(file), monitor);
		if(references==null){
			references=new IASTReference[0];
		}
		return references;
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
			return null;
		}
		return node.getLogicalPath();
	}
	
	protected Collection<IFile> getAllFiles() throws CoreException{
		ProjectUtil projectUtil=new ProjectUtil(info.getEditor());
		return projectUtil.getAllFiles();
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
	
	/**
	 * Searchs all project files to find the matching IFile to the given IParseFile
	 * @param parseFile
	 * @return
	 * @throws CoreException
	 * @throws MissingNatureException
	 */
	protected IFile getIFile4ParseFile(IParseFile parseFile) throws CoreException, MissingNatureException{
		Collection<IFile> files = getAllFiles();
		for(IFile file:files){
			File f = file.getLocation().toFile();
			IParseFile otherPF = getModel().parseFile(f);
			if(parseFile.equals(otherPF)){
				return file;
			}
		}
		return null;
	}
	
	/**
	 * Looks for the Identifier of a given IASTModelPath.
	 * @param path
	 * @param monitor
	 * @return
	 * @throws MissingNatureException
	 * @throws CoreException
	 * @throws IOException
	 */
	protected Identifier getIdentifierForPath(IASTModelPath path,IProgressMonitor monitor) 
	throws MissingNatureException, CoreException, IOException{
		IFileRegion targetRegion = getModel().getNode(path, monitor).getRegion();
		IFile targetFile = getIFile4ParseFile(targetRegion.getParseFile());
		NesC12AST ast = info.getProjectUtil().getAst(targetFile,monitor);
		ASTPositioning astUtil = new ASTPositioning(ast);
		return astUtil.getASTLeafAtPos(targetRegion.getOffset(),targetRegion.getLength(),Identifier.class);
	}
	
	/**
	 * Looks for the Identifier of a given area.
	 * @param path
	 * @param monitor
	 * @return
	 * @throws MissingNatureException
	 * @throws CoreException
	 * @throws IOException
	 */
	protected Identifier getIdentifierForArea(int left,int right,NesC12AST ast,IProgressMonitor monitor) {
		ASTPositioning astUtil=new ASTPositioning(ast);
		return astUtil.getASTLeafAtPos(left,right-left,Identifier.class);
	}
	
	/**
	 * Tries to find the real Logical path of an reference, not just an intermediate node.
	 * @param path
	 * @param monitor
	 * @return
	 * @throws MissingNatureException
	 */
	protected IASTModelPath eagerResolveLogicalPath(IASTModelPath path,IProgressMonitor monitor) 
	throws MissingNatureException{
		ProjectModel model=getModel();
		IASTModelPath oldPath=null;
		while(!path.equals(oldPath)){
			oldPath=path;
			IASTModelNode node=model.getNode(oldPath, monitor);
			if(node==null){
				return path;
			}
			path=node.getLogicalPath();
		}
		return path;
	}
	
	/**
	 * Returns the identifiers which are part of a reference which points to one of the given paths.
	 * @param file	The file in which we are looking for referencing sources.
	 * @param paths	 The paths for which we search referencing sources.
	 * @param monitor
	 * @return	The identifiers contained in a reference pointing to one of the given paths.
	 * @throws MissingNatureException
	 * @throws IOException
	 * @throws CoreException 
	 */
	protected List<Identifier> getReferencingIdentifiersInFileForTargetPaths(IFile file,Collection<IASTModelPath> paths, IProgressMonitor monitor)
	throws MissingNatureException, IOException, CoreException {
		
		//Gather all sources which reference this path
		IASTModelPath candidatePath;
		IASTReference[] referenceArray = getReferences(file,monitor);
		List<IASTReference> matchingSources=new LinkedList<IASTReference>();
		for (IASTReference ref : referenceArray) {
			candidatePath=ref.getTarget();
			if(candidatePath!=null){
				if (paths.contains(candidatePath)) {
					matchingSources.add(ref);
				}
			}
		}
		
		//Find Identifiers which are part of the given Source.
		IFileRegion region;
		ASTPositioning astUtil=new ASTPositioning(info.getProjectUtil().getAst(file,monitor));
		List<Identifier> identifiers=new LinkedList<Identifier>();
		for(IASTReference reference:matchingSources){
			region=reference.getSource();
			ASTNode node=astUtil.getASTLeafAtPos(region.getOffset(),region.getLength());
			Identifier identifier=(Identifier)node;
			identifiers.add(identifier);
		}
		return identifiers;
	}
	
	/**
	 * Adds changes for all the given identifiers to the given CompositeChange ret.
	 * The given identifiers have to be in the given file.
	 * @param identifiers
	 * @param ast
	 * @param file
	 * @param ret
	 */
	protected void addMultiTextEdit(Collection<Identifier> identifiers, NesC12AST ast,IFile file,String textChangeName,CompositeChange ret) {
		if(identifiers.size()>0){
			MultiTextEdit multiTextEdit=new MultiTextEdit();
			addChanges4Identifiers(identifiers, info.getNewName(), multiTextEdit, ast);
			TextChange textChange = new TextFileChange(textChangeName,file);
			textChange.setEdit(multiTextEdit);
			ret.add(textChange);
		}
		
	}
	
	/**
	 * Creates a title for a text change.
	 * @param entityName The name of the entity we are renaming. I.e. global field or interface.
	 * @param file
	 * @return
	 */
	protected String createTextChangeName(String entityName,IFile file){
		return "Replacing "+entityName+" name " + info.getOldName()+ " with " + info.getNewName() + " in Document " + file;
	}
	
}
