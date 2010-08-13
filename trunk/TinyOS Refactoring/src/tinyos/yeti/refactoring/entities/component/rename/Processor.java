package tinyos.yeti.refactoring.entities.component.rename;


import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.Refactoring;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.rename.NesCComponentNameCollissionDetector;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class Processor extends RenameProcessor {
	
	private IDeclaration componentDefinition;
	private IFile declaringFile;
	private Identifier declaringIdentifier;
	private Map<IFile,Collection<Identifier>> affectedIdentifiers;
	
	private String newFileName;
	
	private RenameInfo info;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	/**
	 * Find the component definition.
	 * If couldnt find fatalError is addet to Refactoringstatus.
	 * @return false if couldnt find definition, true otherwise.
	 * @param ret
	 * @throws CoreException
	 * @throws MissingNatureException
	 */
	private boolean findComponentDefinition(RefactoringStatus ret) throws CoreException,MissingNatureException {
		Identifier selectedIdentifier=getSelectedIdentifier();
		ComponentSelectionIdentifier selectionIdentifier=new ComponentSelectionIdentifier(selectedIdentifier);
		if (!selectionIdentifier.isComponent(selectedIdentifier)) {
			ret.addFatalError("No Component selected.");
			return false;
		}
		componentDefinition = getProjectUtil().getComponentDefinition(selectedIdentifier.getName());
		if(componentDefinition==null){
			ret.addFatalError("Did not find an component Definition, for selection: "+selectedIdentifier.getName()+"!");
			return false;
		}
		else if(!componentDefinition.getParseFile().isProjectFile()){
			ret.addFatalError("Component definition is out of project range!");
			return false;
		}
		return true;
	}

	/**
	 * Collects all identifiers which are affected by the renaming of this component, grouped by the file, which defines them.
	 * @param pm
	 * @return
	 * @throws CoreException
	 * @throws MissingNatureException
	 * @throws IOException
	 */
	private Map<IFile,Collection<Identifier>> gatherAffectedIdentifiers(IProgressMonitor pm) throws CoreException, MissingNatureException,IOException {
		Map<IFile,Collection<Identifier>> files2Identifiers=new HashMap<IFile, Collection<Identifier>>();
		
		//Add Identifier of component definition
		declaringIdentifier=getIdentifierForPath(componentDefinition.getPath(), pm);
		List<Identifier> identifiers=new LinkedList<Identifier>();
		identifiers.add(declaringIdentifier);
		files2Identifiers.put(declaringFile, identifiers);
		
		//Add Identifiers of referencing elements
		Collection<IASTModelPath> paths=new LinkedList<IASTModelPath>();
		paths.add(componentDefinition.getPath());
		for(IFile file:getAllFiles()){
			identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
			identifiers=throwAwayDifferentNames(identifiers,declaringIdentifier.getName());
			if(identifiers.size()>0){
				files2Identifiers.put(file,identifiers);
			}
		}
		return files2Identifiers;
	}

	@Override
	public String getProcessorName() {
		return Refactoring.RENAME_COMPONENT.getEntityName();
	}
	
	@Override
	public RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus ret = new RefactoringStatus();
		ProjectUtil projectUtil=getProjectUtil();
		try {
			if (!isApplicable()) {
				ret.addFatalError("The Refactoring is no Accessable");
			}
			if(!findComponentDefinition(ret)){
				return ret;
			}
			declaringFile=getIFile4ParseFile(componentDefinition.getParseFile());
			affectedIdentifiers = gatherAffectedIdentifiers(pm);
			
		} catch (Exception e) {
			ret.addFatalError("Exception during initialization. See project log for more information.");
			projectUtil.log("Exception during initialization.", e);
		}
		return ret;
	}
	
	@Override
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm) {
		RefactoringStatus status= new RefactoringStatus();
		ProjectUtil projectUtil=getProjectUtil();
		newFileName=projectUtil.appendFileExtension(info.getNewName());
		NesCComponentNameCollissionDetector detector=new NesCComponentNameCollissionDetector();
		detector.handleCollisions4NewFileName(newFileName,declaringIdentifier,declaringFile,getProjectUtil(),status,pm);
		try {	//Handle Collisions in affected configurations.
			for(IFile file:affectedIdentifiers.keySet()){
				if(!declaringFile.equals(file)){	//The component itself cannot reference itself.
					AstAnalyzerFactory factory=new AstAnalyzerFactory(file,projectUtil,pm);
					if(factory.hasConfigurationAnalyzerCreated()){	//Only configurations can reference other modules.
						detector.handleCollisions4NewComponentNameWithConfigurationLocalName(factory.getConfigurationAnalyzer(), file, info.getOldName(), info.getNewName(), status);
					}
				}
			}
		} catch (Exception e){
			status.addFatalError("Exception during condition checking. See project log for more information.");
			projectUtil.log("Exception during condition checking.", e);
		}
		return status;
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		CompositeChange ret = createNewCompositeChange();
		try {
			addChanges(affectedIdentifiers, ret, pm);
			//Adds the change for renaming the file which contains the definition.
			RenameResourceChange resourceChange=new RenameResourceChange(declaringFile.getFullPath(), newFileName);
			ret.add(resourceChange);
			
		} catch (Exception e){
			ret.add(new NullChange("Exception Occured during change creation! See project log for more information."));
			getProjectUtil().log("Exception Occured during change creation.", e);
		}
		return ret;
	}
}
