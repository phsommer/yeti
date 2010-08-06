package tinyos.yeti.refactoring.rename.global.field;


import java.io.IOException;
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
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.rename.global.FieldInfo;
import tinyos.yeti.refactoring.rename.global.FieldInfoSet;
import tinyos.yeti.refactoring.rename.global.FieldKind;
import tinyos.yeti.refactoring.rename.global.GlobalFieldFinder;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class GlobalFieldRenameProcessor extends RenameProcessor {

	private RenameInfo info;
	
	private GlobalFieldFinder finder;
	private FieldInfoSet fieldInfoSet4SelectedField;
	private Map<IFile, Collection<FieldInfo> > files2FieldInfos4SelectedField;
	
	private Map<IFile,Collection<Identifier>> affectedIdentifiers;

	public GlobalFieldRenameProcessor(RenameInfo info) {
		super(info);
		this.info = info;
	}

	/**
	 * Returns the associated identifier to a field.
	 * @param info
	 * @param positioning
	 * @return
	 */
	private Identifier getIdentifier4FieldInfo(FieldInfo info,ASTPositioning positioning){
		RangeDescription description=info.getField().getRange();
		return (Identifier)positioning.getASTLeafAtPreprocessedPos(description.getLeft());
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
		return ret;
	}

	@Override
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm) {
		RefactoringStatus ret=new RefactoringStatus();
		FieldInfoSet set=finder.getFieldInfoSetAbout(info.getNewName());
		//Check if there is a global field like a function or a variable which already has the new name.
		if(!set.isEmpty()){
			ret.addError("You intended to rename the global field "+info.getOldName()+" to "+info.getNewName()+". There exists allready a global field with this name.");
			Map<IFile,Collection<FieldInfo>> file2FieldInfos=set.getFiles2FieldInfos();
			Region sameNameRegion= new Region(0,0);
			ProjectUtil projectUtil=new ProjectUtil(info.getEditor());
			for(IFile file:file2FieldInfos.keySet()){
				//TODO vielleicht kann man die identifier anzeigen, die den Konflikt verursachen, leider funktioniert das mitdem positioning aber nicht richtig. Preprocesse <-> nicht preprocessed problem.
				if(projectUtil.isProjectFile(file)){
					try {
						NesC12AST ast=getAst(file, pm);
						for(FieldInfo info:file2FieldInfos.get(file)){
							if(info.getKind()!=FieldKind.INCLUDED_DECLARATION){
								ret.addError("The field generating the collission appears in the file: "+file.getName(),new FileStatusContext(file, sameNameRegion));
							}
						}
					} catch (Exception e){
						ret.addError("The field generating the collission appears in the file: "+file.getName());
					}
				}else{
					ret.addError("The field generating the collission appears out of project range in the file: "+file.getName());
				}
			}
		}
		return ret;
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		CompositeChange ret = new CompositeChange("Rename Field "+ info.getOldName() + " to " + info.getNewName());
		try {
			super.addChanges("global field", affectedIdentifiers, ret, pm);
		} catch (Exception e){
			ret.add(new NullChange("Failed to create change. See project log for more information."));
			getProjectUtil().log("Failed to create change", e);
		}
		return ret;
	}



}
