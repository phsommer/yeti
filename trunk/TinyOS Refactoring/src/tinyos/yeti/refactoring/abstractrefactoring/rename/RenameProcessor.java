package tinyos.yeti.refactoring.abstractrefactoring.rename;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.entities.field.rename.global.FieldInfo;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.DebugUtil;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

/**
 * This class is intended to be subclassed to introduce a new rename refactoring.
 * There is a little framework implemented in this class:
 * 
 * 1.The first function of a sublcass to be called is initializeRefactoring.
 * 	Here a subclass can gather all its information, to be sure, the refacoring is even possible or even has an effect.
 * 	Experience shows, that this is actually the function which gathers all Identifier AstNodes, which are affected by the renaming.
 * 	Errors in this function normally lead to adding an FatalError message to the returned RefactoringStatus, since the refactoring
 * 	will not be able to do any reasonable thing.
 * 
 * 2.The second function of a sublclass to be called is checkConditionsAfterNameSetting.
 * 	This function is called after the user entered a new name for the entity to be renamed.
 * 	In this function a sub class can check if the new name is reasonable choice.
 * 	This is the place were you should check, if renaming would lead to name collisions.
 * 	Errors in this place are often not reported back as FatalError but just as Error instead.
 * 	If the report is just error, then the user still has the choice to  proceed, the refactoring just informs, that proceeding
 * 	will change the semantics of the source, or will even lead to compile errors. 
 * 
 * 3.The function createChange is called.
 * 	Here the subclass can build the actual Change Object.
 * 
 * If you override the method  checkInitialConditions or checkFinalConditions the above life cycle is no more guaranteed.
 */
public abstract class RenameProcessor extends org.eclipse.ltk.core.refactoring.participants.RenameProcessor {
	
	private RenameInfo info;

	private ASTUtil astUtil;
	
	private boolean refactoringInitialized=false;
	private RefactoringStatus initializationStatus;

	public RenameProcessor(RenameInfo info) {
		super();
		this.info = info;
	}
	
	//Adapter implementation too release subclasses from doing this themself.
	@Override
	public Object[] getElements() {
		return new Object[] {};
	}
	
	//Adapter implementation
	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,SharableParticipants sharedParticipants) 
	throws CoreException {
		return new RefactoringParticipant[] {};
	}
	
	@Override
	public boolean isApplicable() throws CoreException {
		//If there is no AstPositioning, the plugin may not be fully loaded by now.
		return (info.getAstPositioning() != null);
	}

	@Override
	public String getIdentifier() {
		return getClass().getCanonicalName();
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
	throws CoreException, OperationCanceledException {
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
	
	@Override
	abstract public String getProcessorName();
	
	
	
	//The following three methods force every subclass to implement the given lifecycle shema.
	
	/**
	 * This method is the first to be called for the whole refactoring.
	 * It is made sure, that it is only once called by the lifecycle.
	 * The idea is that subclasses use this method as constructor which has a IProgressMonitor parameter.
	 * @param pm
	 * @return
	 */
	abstract protected RefactoringStatus initializeRefactoring(IProgressMonitor pm);
	
	
	/**
	 * This method is called after the user has set the new name.
	 * If the user usese the back button, this method can be called multiple times.
	 * @param pm
	 * @return
	 */
	abstract protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm);
	
	@Override
	abstract public Change createChange(IProgressMonitor pm) throws CoreException ,OperationCanceledException;

	
	
	//The remaining methods are for common use between several refactoring processors.
	
	/**
	 * 
	 * @return	The Currently Selected Identifier, null if not an Identifier is Selected.
	 */
	protected Identifier getSelectedIdentifier() {
		ITextSelection selection=info.getSelection();
		return info.getAstPositioning().getASTLeafAtPos(selection.getOffset(),selection.getLength(),Identifier.class);
	}
	
	protected NesC12AST getAst(IFile iFile, IProgressMonitor monitor) throws IOException, MissingNatureException{
		return info.getProjectUtil().getAst(iFile, monitor);
	}

	/**
	 * Returns a projectUtil.
	 * @return
	 * @throws MissingNatureException
	 */
	protected ProjectUtil getProjectUtil() {
		return info.getProjectUtil();
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
	 * Returns the identifiers which are part of a reference which points to one of the given paths.
	 * Use this function if you know, that a source of a reference only contains a single identifier.
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
		DebugUtil.immediatePrint("File "+file.getName());
		DebugUtil.immediatePrint("Path: "+tartgetPaths.iterator().next().toString());
		//Gather all sources which reference this paths
		IASTModelPath candidatePath;
		IASTReference[] referenceArray = getReferences(file,monitor);
		List<IASTReference> matchingSources=new LinkedList<IASTReference>();
		for (IASTReference ref : referenceArray) {
			candidatePath=ref.getTarget();
			if(candidatePath!=null){
				DebugUtil.immediatePrint("\t"+candidatePath.toString());
				if (tartgetPaths.contains(candidatePath)) {
					matchingSources.add(ref);
				}
			}
		}
		DebugUtil.immediatePrint("\tmatchingSources "+matchingSources.size());
		//Find Identifiers which are part of the given Source.
		IFileRegion region;
		ASTPositioning positioning=new ASTPositioning(info.getProjectUtil().getAst(file,monitor));
		List<Identifier> identifiers=new LinkedList<Identifier>();
		for(IASTReference reference:matchingSources){
			region=reference.getSource();
			ASTNode node=positioning.getASTLeafAtPos(region.getOffset(),region.getLength());
			Identifier identifier=(Identifier)node;
			if(identifier!=null){	
				identifiers.add(identifier);
			}
		}
		return identifiers;
	}
	
	/** 
	 * Same as getReferencingIdentifiersInFileForTargetPaths but fore the sources of the reference, to get its identifier the hole source range is used instead of just the start value.
	 * This means that there can actually be added more than one identifier per reference. I.E. an interface reference can also contain an interface alias besides the interface name itself.
	 * Therefore you have to filter out the Identifiers you really are interested in after calling this method.
	 * This is needed because there exist reference sources, which actually include much more than just one identifier and so the identifier we are looking for, is not clearly defined.
	 * I.E. a interface reference in a component specification is a source which can include the interface identifier itself and an interface alias.
	 */
	protected List<Identifier> getReferencingIdentifiersInFileForTargetPathsUseHoleRange(IFile file,Collection<IASTModelPath> tartgetPaths, IProgressMonitor monitor)
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
		ASTPositioning positioning=new ASTPositioning(info.getProjectUtil().getAst(file,monitor));
		List<Identifier> identifiers=new LinkedList<Identifier>();
		for(IASTReference reference:matchingSources){
			region=reference.getSource();
			ASTNode node=positioning.getDeepestAstNodeOverRange(region.getOffset(),region.getLength());
			ASTNode root=getAst(file, monitor).getRoot();
			if(node!=root){	//If we have gotten the root node, then the range is probably out of the source file, i.e. in a include statement.
				identifiers.addAll(getAstUtil().getAllNodesOfType(node, Identifier.class));
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
	public void addChanges(Map<IFile, Collection<Identifier>> files2Identifiers, CompositeChange ret, IProgressMonitor pm) throws IOException, MissingNatureException {
		for(IFile file:files2Identifiers.keySet()){
			addMultiTextEdit(files2Identifiers.get(file), getAst(file, pm), file, createTextChangeName(file), ret);
		}
	}
	
	/**
	 * Returns a list which only contains identifiers which have the same name as the given name.
	 * This is needed since aliases in event/command definitions and so on and the real entity names often reference the original entity.
	 * @param identifiers
	 */
	protected List<Identifier> throwAwayDifferentNames(Collection<Identifier> identifiers,String wishedName) {
		List<Identifier> result=new LinkedList<Identifier>();
		for(Identifier identifier:identifiers){
			if(wishedName.equals(identifier.getName())){
				result.add(identifier);
			}
		}
		return result;
	}	
	
	/**
	 * Creates a new CompositeChange.
	 * The main purpose for this method is to have a single place, where the composite change title is created.
	 * @param file
	 * @return
	 */
	protected CompositeChange createNewCompositeChange(){
		return new CompositeChange("Rename "+getProcessorName()+" \""+info.getOldName() + "\" to \"" + info.getNewName()+"\".");
	}
	
	/**
	 * Creates a title for a text change.
	 * @param entityName The name of the entity we are renaming. I.e. global field or interface.
	 * @param file
	 * @return
	 */
	protected String createTextChangeName(IFile file){
		return "Replacing "+getProcessorName()+" name \"" + info.getOldName()+ "\" with \"" + info.getNewName() + "\" in Document \"" + file+"\".";
	}

	/**
	 * Returns the associated identifier to a field.
	 * @param info
	 * @param positioning
	 * @return
	 */
	protected Identifier getIdentifier4FieldInfo(FieldInfo info, ASTPositioning positioning) {
		RangeDescription description=info.getField().getRange();
		return (Identifier)positioning.getASTLeafAtPreprocessedPos(description.getLeft());
	}
}
