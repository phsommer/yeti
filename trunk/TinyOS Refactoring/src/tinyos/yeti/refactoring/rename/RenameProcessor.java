package tinyos.yeti.refactoring.rename;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
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
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public abstract class RenameProcessor extends org.eclipse.ltk.core.refactoring.participants.RenameProcessor {
	
	private RenameInfo info;

	private ProjectUtil projectUtil;
	private ASTUtil astUtil;
	
	private boolean refactoringInitialized=false;
	private RefactoringStatus initializationStatus;

	public RenameProcessor(RenameInfo info) {
		super();
		this.info = info;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return (info.getAstPositioning() != null);
	}
	
	@Override
	public Object[] getElements() {
		// TODO Auto-generated method stub
		return new Object[] {};
	}

	@Override
	public String getIdentifier() {
		return getClass().getCanonicalName();
	}

	@Override
	public String getProcessorName() {
		return info.getInputPageName();
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,SharableParticipants sharedParticipants) 
	throws CoreException {
		// TODO Auto-generated method stub
		return new RefactoringParticipant[] {};
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
	throws CoreException, OperationCanceledException {
//		clearStatus();
		RefactoringStatus ret = new RefactoringStatus();
		if (!isApplicable()) {
			ret.addFatalError("The Refactoring is not Applicalbe");
			return ret;
		}
		if(!refactoringInitialized){
			refactoringInitialized=true;
			initializationStatus=initializeRefactoring(pm);
		}
		if(!initializationStatus.isOK()){
			return initializationStatus;
		}
		return ret;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,CheckConditionsContext context) 
	throws CoreException,OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if (!isApplicable()) {
			ret.addFatalError("The Refactoring is not Applicable");
		}
		
		//Check if the selected new name and the old name are different.
		String newName=info.getNewName();
		String oldName=info.getOldName();
		if(oldName.equals(newName)){
			ret.addFatalError("The old name and the new name are equal!\nThe refactoring want do anything!");
			return ret;
		}else
		ret=checkConditionsAfterNameSetting(pm);
		return ret;
	}
	
	/**
	 * This method is the first to be called for the whole refactoring.
	 * It is made sure, that it is only once called by the lifecycle.
	 * The idea is that subclasses use this method as constructor which has a IProgressMonitor parameter.
	 * @param pm
	 * @return
	 */
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		return new RefactoringStatus();
	}
	
	
	/**
	 * This method is called after the user has set the new name.
	 * If the user usese the back button, this method can be called multiple times.
	 * @param pm
	 * @return
	 */
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm) {
		return new RefactoringStatus();
	}

	/**
	 * 
	 * @return	The Currently Selected Identifier, null if not an Identifier is Selected.
	 */
	protected Identifier getSelectedIdentifier() {
		ITextSelection selection=info.getSelection();
		return info.getAstPositioning().getASTLeafAtPos(selection.getOffset(),selection.getLength(),Identifier.class);
	}
	
	/**
	 * Parses a given File and returns the Parser.
	 */
	protected Parser getParser(IFile iFile, IProgressMonitor monitor) throws IOException, MissingNatureException{
		return getProjectUtil().getParser(iFile, monitor);
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
	 * Returns a projectUtil.
	 * @return
	 * @throws MissingNatureException
	 */
	protected ProjectUtil getProjectUtil() {
		if(projectUtil==null){
			projectUtil=new ProjectUtil(info.getEditor());
		}
		return projectUtil;
	}
	
	/**
	 * Returns a ASTUtil instance.
	 * @return
	 */
	protected ASTUtil getAstUtil(){
		if(astUtil==null){
			astUtil=new ASTUtil();
		}
		return astUtil;
	}
	
	/**
	 * Returns the projectModel of this project.
	 * @return
	 * @throws MissingNatureException
	 */
	protected ProjectModel getModel() throws MissingNatureException{
		return getProjectUtil().getModel();
	}
	
	/**
	 * Gets all IFiles of this project.
	 * @return
	 * @throws CoreException
	 */
	protected Collection<IFile> getAllFiles() throws CoreException{
		return getProjectUtil().getAllFiles();
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
	 * Returns all Project Files.
	 * @param parseFile
	 * @return
	 * @throws CoreException
	 * @throws MissingNatureException
	 */
	protected IFile getIFile4ParseFile(IParseFile parseFile) throws CoreException, MissingNatureException{
		return info.getProjectUtil().getIFile4ParseFile(parseFile);
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
		IFile targetFile = info.getProjectUtil().getIFile4ParseFile(targetRegion.getParseFile());
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
	 * Checks if the target of the given path is inside the project.
	 * @param targetPath
	 * @return
	 */
	private boolean isPathTargetInsideProject(IASTModelPath targetPath){
		return getProjectUtil().isProjectFile(targetPath.getParseFile());
	}
	
	/**
	 * Adds just paths to the returned collection, which have a target file inside the project.
	 * @param paths
	 * @return
	 */
	protected Collection<IASTModelPath> filterOutNonProjectPaths(Collection<IASTModelPath> paths){
		Collection<IASTModelPath> validPaths=new LinkedList<IASTModelPath>();
		for(IASTModelPath path:paths){
			if(isPathTargetInsideProject(path)){
				validPaths.add(path);
			}
		}
		return validPaths;
	}
	
	/**
	 * Returns the identifiers which are part of a reference which points to one of the given paths.
	 * @param file	The file in which we are looking for referencing sources.
	 * @param tartgetPaths	 The paths for which we search referencing sources. If a paths target is not inside the project it will not be used to search for references.
	 * @param monitor
	 * @return	The identifiers contained in a reference pointing to one of the given paths.
	 * @throws MissingNatureException
	 * @throws IOException
	 * @throws CoreException 
	 */
	protected List<Identifier> getReferencingIdentifiersInFileForTargetPaths(IFile file,Collection<IASTModelPath> tartgetPaths, IProgressMonitor monitor)
	throws MissingNatureException, IOException, CoreException {
		
		//Gather all sources which reference this path
		IASTModelPath candidatePath;
		IASTReference[] referenceArray = getReferences(file,monitor);
		List<IASTReference> matchingSources=new LinkedList<IASTReference>();
		for (IASTReference ref : referenceArray) {
			candidatePath=ref.getTarget();
			if(candidatePath!=null){
				if (tartgetPaths.contains(candidatePath)) {
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
			if(identifier!=null){	//There appear sometimes null values which we dont care about.
				identifiers.add(identifier);
			}
		}
		return identifiers;
	}
	
	/**
	 * Adds a replace edit for every identifier in identifiers to the multitextedit.
	 * @param identifiers
	 * @param newName	the new name for the identifier.
	 * @param multiTextEdit
	 * @param ast the ast which includes the identifiers.
	 */
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
	 * Adds for every file in map a multitextedit for all its associated identifiers.
	 * @param files2Identifiers
	 * @param ret
	 * @param pm
	 * @throws IOException
	 * @throws MissingNatureException
	 */
	public void addChanges(String entityName,Map<IFile, Collection<Identifier>> files2Identifiers, CompositeChange ret, IProgressMonitor pm) throws IOException, MissingNatureException {
		for(IFile file:files2Identifiers.keySet()){
			addMultiTextEdit(files2Identifiers.get(file), getAst(file, pm), file, createTextChangeName(entityName, file), ret);
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
	
	/**
	 * Returns a list which only contains identifiers which have the same name as the given name.
	 * This is needed since aliases in event/command definitions and so on and the real entity names often reference the original entity.
	 * @param identifiers
	 */
	protected List<Identifier> throwAwayDifferentNames(List<Identifier> identifiers,String wishedName) {
		List<Identifier> result=new LinkedList<Identifier>();
		for(Identifier identifier:identifiers){
			if(wishedName.equals(identifier.getName())){
				result.add(identifier);
			}
		}
		return result;
	}	
}
