package tinyos.yeti.refactoring.entities.field.rename.global;


import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.rename.NesCComponentNameCollissionDetector;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class GlobalFieldRenameProcessor extends RenameProcessor {
	
	private RenameInfo info;
	
	private GlobalFieldFinder finder;
	private IFile selectedFile;
	private NesC12AST selectionAst;
	private FieldInfoSet fieldInfoSet4SelectedField;
	private Map<IFile, Collection<FieldInfo> > files2FieldInfos4SelectedField;
	
	private Map<IFile,Collection<Identifier>> affectedIdentifiers;

	public GlobalFieldRenameProcessor(RenameInfo info) {
		super(info);
		this.info = info;
	}

	/**
	 * Gathers all identifiers which are affected by renaming this field.
	 * @param ret
	 * @param pm
	 * @return
	 */
	private Map<IFile,Collection<Identifier>> gatherAffectedIdentifiers(RefactoringStatus ret,IProgressMonitor pm) {
		Map<IFile,Collection<Identifier>> file2Identifiers=new HashMap<IFile, Collection<Identifier>>();
		try {
			for(IFile file:files2FieldInfos4SelectedField.keySet()){
				NesC12AST ast=getAst(file, pm);
				ASTPositioning positioning=new ASTPositioning(ast);
				Collection<Identifier> identifiers=new LinkedList<Identifier>();
				Collection<FieldInfo> fieldInfos=files2FieldInfos4SelectedField.get(file);
				
				//Get the Identifiers of declarations and, if this field is a function, the identifiers of definitions in the file. 
				for(FieldInfo fieldInfo:fieldInfos){
					if(fieldInfo.getKind()!=FieldKind.INCLUDED_DECLARATION){	//Included Declarations don't have to be changed.
						Identifier id=getIdentifier4FieldInfo(fieldInfo,positioning);
						identifiers.add(id);
					}
				}
				
				//Get the Identifiers of References in the file. 
				Collection<IASTModelPath> paths=fieldInfoSet4SelectedField.getKnownPathsForFile(file);
				Collection<Identifier> referencesOfFile=getReferencingIdentifiersInFileForTargetPaths(file,paths,pm);
				identifiers.addAll(referencesOfFile);
				if(identifiers.size()>0){
					file2Identifiers.put(file, identifiers);
				}
			}
			return file2Identifiers;
		}
		catch (Exception e){
			ret.addFatalError("Exception occured while gathering affected Identifiers. See project log for more information.");
			getProjectUtil().log("Exception occured while gathering affected Identifiers", e);
			return file2Identifiers;
		}
	}
	
	/**
	 * Gather general information about the field which is to be refactored.
	 * @param pm
	 * @return false if there occured an exception, true if field information are available.
	 */
	private boolean getFieldInformation(RefactoringStatus ret, IProgressMonitor pm) {
		//Gather general information about the field.
		try {
			finder=new GlobalFieldFinder(info.getEditor(), pm);
			fieldInfoSet4SelectedField=finder.getFieldInfoSetAbout(getSelectedIdentifier().getName());
			files2FieldInfos4SelectedField=fieldInfoSet4SelectedField.getFiles2FieldInfos();
			return true;
		} catch (Exception e){
			ret.addFatalError("Failed to get field information. See project log for more information.");
			getProjectUtil().log("Failed to get field information", e);
			return false;
		}
	}
	
	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		if(!getFieldInformation(ret,pm)){
			return ret;
		}
		affectedIdentifiers=gatherAffectedIdentifiers(ret,pm);
		selectedFile=ActionHandlerUtil.getInputFile(info.getEditor());
		if(selectedFile==null){
			ret.addFatalError("Couldnt find the file which contains the selection.");
			return ret;
		}
		try {
			selectionAst=getAst(selectedFile, pm);
		} catch (Exception e){
			ret.addFatalError("Couldnt find the AST which contains the selection.");
		}
		return ret;
	}

	@Override
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm) {
		RefactoringStatus ret=new RefactoringStatus();
		ProjectUtil projectUtil=getProjectUtil();
		FieldInfoSet set=finder.getFieldInfoSetAbout(info.getNewName());
		boolean globalFieldConflictOccured=false;
		
		//Check if there is a global field like a function or a variable which already has the new name.
		if(!set.isEmpty()){
			globalFieldConflictOccured=true;
			ret.addError("You intended to rename the global field "+info.getOldName()+" to "+info.getNewName()+". There exists allready a global field with this name.");
			
			//Print information about the fields leading to the collision  
			Map<IFile,Collection<FieldInfo>> file2FieldInfos=set.getFiles2FieldInfos();
			Region region;
			int length=info.getNewName().length();
			for(IFile file:file2FieldInfos.keySet()){
				try {
					ASTPositioning positioning=new ASTPositioning(getAst(file, pm));
					if(projectUtil.isProjectFile(file)){
						for(FieldInfo info:file2FieldInfos.get(file)){
							if(info.getKind()!=FieldKind.INCLUDED_DECLARATION){
								int offset=positioning.start(getIdentifier4FieldInfo(info, positioning));
								region=new Region(offset, length);
								ret.addError("Declaration of field leading to the collission appears in the file: "+file.getName(),new FileStatusContext(file, region));

							}
						}
					}else{
						ret.addError("Declaration of field leading to the collission appears out of project range in the file: "+file.getName());
					}
				} catch (Exception e){
					ret.addError("Declaration of field leading to the collission appears in the file: "+file.getName());
				}
			}
			
			//Print information about the fields to be renamed
			file2FieldInfos=fieldInfoSet4SelectedField.getFiles2FieldInfos();
			length=info.getOldName().length();
			for(IFile file:file2FieldInfos.keySet()){
				try {
					ASTPositioning positioning=new ASTPositioning(getAst(file, pm));
					if(projectUtil.isProjectFile(file)){
						for(FieldInfo info:file2FieldInfos.get(file)){
							if(info.getKind()!=FieldKind.INCLUDED_DECLARATION){
								int offset=positioning.start(getIdentifier4FieldInfo(info, positioning));
								region=new Region(offset, length);
								ret.addError("Declaration of field to be renamed appears in the file: "+file.getName(),new FileStatusContext(file, region));

							}
						}
					}else{
						ret.addError("Declaration of field to be renamed appears out of project range in the file: "+file.getName());
					}
				} catch (Exception e){
					ret.addError("Declaration of field to be renamed appears in the file: "+file.getName());
				}
			}
		}
		
		//Print information about occurrences of shadowing. Just print if there was not already a global field conflict.
		if(!globalFieldConflictOccured){
			NesCComponentNameCollissionDetector detector=new NesCComponentNameCollissionDetector();
			for(IFile file:files2FieldInfos4SelectedField.keySet()){
				try {
					NesC12AST containingAst=getAst(file, pm);
					if(projectUtil.isProjectFile(file)){
						detector.handleCollisions4Scope(info.getOldName(),info.getNewName(), getSelectedIdentifier(),selectedFile,selectionAst, containingAst.getRoot(), file, containingAst, ret);
					}
				} catch (Exception e){
					Region region=new Region(0, 0);
					ret.addError("Wasnt able to check for occurrences of shadowing in file: "+file.getName(),new FileStatusContext(file, region));
					
				}
			}
		}
		return ret;
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		CompositeChange ret = createNewCompositeChange();
		try {
			super.addChanges(affectedIdentifiers, ret, pm);
		} catch (Exception e){
			ret.add(new NullChange("Failed to create change. See project log for more information."));
			getProjectUtil().log("Failed to create change", e);
		}
		return ret;
	}

	@Override
	public String getProcessorName() {
		return "global field";
	}



}
