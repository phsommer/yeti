package tinyos.yeti.refactoring.rename.global.interfaces;


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
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
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
import tinyos.yeti.refactoring.rename.alias.AliasSelectionIdentifier;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class Processor extends RenameProcessor {
	
	boolean selectionisInterfaceAliasInNesCComponentWiring=false;
	private tinyos.yeti.refactoring.rename.alias.interfaces.Processor aliasProcessor;
	
	private IDeclaration interfaceDeclaration;
	private IFile declaringFile;
	private Identifier declaringIdentifier;
	private String newFileName;
	
	private Map<IFile,Collection<Identifier>> affectedIdentifiers;
	
	private RenameInfo info;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	/**
	 * Sets the field selecionIsInterfaceAliasInNesCComponentWiring, and if this is true the field aliasProcessor.
	 * 
	 * Reason for this method:
	 * Since we have no process monitor when we are deciding the processor type, we have to handle this case in the interface processor.
	 * Without process monitor we are unable to get an ast and without ast or at least a node we cant create a AstAnalyzer of the alias defining file.
	 *  => we cannot decide if it is an interface or actually an alias.
	 * @param selectedIdentifier
	 * @param monitor
	 * @return true if the selection is actually an interface alias
	 */
	private void handleCaseInterfaceAliasInNesCComponentWiring(Identifier selectedIdentifier,IProgressMonitor monitor){
		AliasSelectionIdentifier selectionIdentifier=new AliasSelectionIdentifier(selectedIdentifier);
		if(!selectionIdentifier.isInterfaceAliasInNescComponentWiring(getProjectUtil(),monitor)){
			return;
		}
		selectionisInterfaceAliasInNesCComponentWiring=true;
		aliasProcessor=new tinyos.yeti.refactoring.rename.alias.interfaces.Processor(info);
	}
	
	/**
	 * Collects all identifiers which are affected by the renaming.
	 * @param pm
	 * @return
	 * @throws CoreException
	 * @throws MissingNatureException
	 * @throws IOException
	 */
	private Map<IFile,Collection<Identifier>> gatherAffectedIdentifiers(IProgressMonitor pm) throws CoreException, MissingNatureException, IOException{
		Map<IFile,Collection<Identifier>> file2Identifiers=new HashMap<IFile, Collection<Identifier>>();
		//Add Change for interface definition
		List<Identifier> identifiers=new LinkedList<Identifier>();
		identifiers.add(declaringIdentifier);
		file2Identifiers.put(declaringFile, identifiers);
		
		//Add Changes for referencing elements
		Collection<IASTModelPath> paths=new LinkedList<IASTModelPath>();
		paths.add(interfaceDeclaration.getPath());
		for(IFile file:getAllFiles()){
			identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
			identifiers=throwAwayDifferentNames(identifiers,declaringIdentifier.getName());
			if(identifiers.size()>0){
				file2Identifiers.put(file,identifiers);
			}
		}
		return file2Identifiers;
	}

	/**
	 * This method either finds the interface definition of the defining interface, 
	 * or if the selection is actually an alias, sets this processor up for delegating methods to an AliasProcessor.
	 * @param pm
	 * @return	true if a interface definition could be found or the selection is actually an alias, false otherwise.
	 * @throws CoreException 
	 * @throws CoreException
	 * @throws MissingNatureException 
	 * @throws IOException 
	 * @throws OperationCanceledException
	 */
	public boolean findInterfaceDefinitionOrDecideAlias(RefactoringStatus ret,IProgressMonitor pm) throws CoreException, MissingNatureException, IOException{

		if (!isApplicable()) {
			ret.addFatalError("The Refactoring is no Accessable");
		}
		Identifier selectedIdentifier=getSelectedIdentifier();
		InterfaceSelectionIdentifier selectionIdentifier=new InterfaceSelectionIdentifier(selectedIdentifier);
		if (!selectionIdentifier.isInterface()) {
			ret.addFatalError("No Interface selected.");
		}

		boolean isInterface=true;
		String message="If you see that, there happened something unexpected!";
		interfaceDeclaration = getProjectUtil().getInterfaceDefinition(selectedIdentifier.getName());
		if(interfaceDeclaration==null){
			isInterface=false;
			message="Did not find an Interface Definition, for selection!";
		}
		else if(!interfaceDeclaration.getParseFile().isProjectFile()){
			isInterface=false;
			message="Interface definition is out of project range!";
		}
		if(!isInterface){
			handleCaseInterfaceAliasInNesCComponentWiring(selectedIdentifier, pm);
			if(!selectionisInterfaceAliasInNesCComponentWiring){
				ret.addFatalError(message);
				return false;
			}
			return true;
		}
		declaringFile=getIFile4ParseFile(interfaceDeclaration.getParseFile());
		declaringIdentifier=getIdentifierForPath(interfaceDeclaration.getPath(), pm);
		return true;
	}

	@Override
	public String getProcessorName() {
		return Refactoring.RENAME_INTERFACE.getEntityName();
	}
	
	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		try {
			if(!findInterfaceDefinitionOrDecideAlias(ret,pm)){
				return ret;
			}
			if(selectionisInterfaceAliasInNesCComponentWiring){
				return aliasProcessor.checkInitialConditions(pm);
			}
			affectedIdentifiers=gatherAffectedIdentifiers(pm);
		} catch (Exception e){
			ret.addFatalError("Exception occured during initialization. See project log.");
			getProjectUtil().log("Exception occured during initialization.",e);
		}
		return ret;

	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,CheckConditionsContext context) throws OperationCanceledException, CoreException{
		RefactoringStatus ret=new RefactoringStatus();
		if(selectionisInterfaceAliasInNesCComponentWiring){
			try {
				return aliasProcessor.checkFinalConditions(pm, context);
			} catch (Exception e){
				ret.addFatalError("Error propagated back from aliasProcessor. See project log.");
				getProjectUtil().log("Error propagated back from aliasProcessor",e);
				return ret;
			}
		}else{
			return super.checkFinalConditions(pm, context);	//If this call is omitted checkConditionsAfterNameSetting will not be called.
		}	
	}
	
	@Override
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm) {
		RefactoringStatus ret=new RefactoringStatus();
		ProjectUtil projectUtil=getProjectUtil();
		newFileName=projectUtil.appendFileExtension(info.getNewName());
		NesCComponentNameCollissionDetector detector=new NesCComponentNameCollissionDetector();
		detector.handleCollisions4NewFileName(newFileName, declaringIdentifier, declaringFile, projectUtil, ret, pm);
		try {	//Handle Collisions in affected components.
			String newName=info.getNewName();
			String oldName=info.getOldName();
			for(IFile file:affectedIdentifiers.keySet()){
				if(!declaringFile.equals(file)){	//The interface itself cannot reference itself.
					AstAnalyzerFactory factory=new AstAnalyzerFactory(file,projectUtil,pm);
					if(factory.hasConfigurationAnalyzerCreated()){
						detector.handleCollisions4NewInterfaceNameWithConfigurationLocalName(factory.getConfigurationAnalyzer(), file, oldName, newName, ret);
					}else if(factory.hasModuleAnalyzerCreated()){
						detector.newInterfaceNameWithLocalInterfaceName(factory.getModuleAnalyzer(), file, oldName, newName, ret);
					}
				}
			}
		} catch (Exception e){
			ret.addFatalError("Exception during condition checking. See project log for more information.");
			projectUtil.log("Exception during condition checking.", e);
		}
		return ret;
	}

	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		//If the selection was identified as interface alias, the alias processor is used.
		//This alias is handled by the interface processor, since we have no process monitor when we are deciding the processor type.
		//Without process monitor we are unable to get an ast and without ast or at least a node we cant create a AstAnalyzer => we cannot decide if it is an interface or actually an alias.
		if(selectionisInterfaceAliasInNesCComponentWiring){	
			return aliasProcessor.createChange(pm);
		}
		CompositeChange ret = createNewCompositeChange();
		try {
			
			//Add changes for affected identifiers
			addChanges(affectedIdentifiers, ret, pm);
			//Adds the change for renaming the file which contains the definition.
			RenameResourceChange resourceChange=new RenameResourceChange(declaringFile.getFullPath(), newFileName);
			ret.add(resourceChange);
			
		} catch (Exception e){
			ret.add(new NullChange("Exception occured during change creation. See project log."));
			getProjectUtil().log("Exception occured during change creation.",e);
		}
		return ret;
	}

}
