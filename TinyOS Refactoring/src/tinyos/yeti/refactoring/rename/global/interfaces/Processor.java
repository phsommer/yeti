package tinyos.yeti.refactoring.rename.global.interfaces;


import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.selection.AliasSelectionIdentifier;
import tinyos.yeti.refactoring.selection.InterfaceSelectionIdentifier;

public class Processor extends RenameProcessor {

	boolean selectionisInterfaceAliasInNesCComponentWiring=false;
	private tinyos.yeti.refactoring.rename.alias.Processor aliasProcessor;
	
	private IDeclaration interfaceDeclaration;
	
	private RenameInfo info;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
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
		CompositeChange ret = new CompositeChange("Rename Interface "+ info.getOldName() + " to " + info.getNewName());
		try {
			//Add Change for interface definition
			IFile declaringFile=getIFile4ParseFile(interfaceDeclaration.getParseFile());
			Identifier declaringIdentifier=getIdentifierForPath(interfaceDeclaration.getPath(), pm);
			List<Identifier> identifiers=new LinkedList<Identifier>();
			identifiers.add(declaringIdentifier);
			addMultiTextEdit(identifiers, getAst(declaringFile, pm), declaringFile, createTextChangeName("interface", declaringFile), ret);
			
			//Add Changes for referencing elements
			Collection<IASTModelPath> paths=new LinkedList<IASTModelPath>();
			paths.add(interfaceDeclaration.getPath());
			for(IFile file:getAllFiles()){
				identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
				identifiers=throwAwayDifferentNames(identifiers,declaringIdentifier.getName());
				addMultiTextEdit(identifiers, getAst(file, pm), file, createTextChangeName("interface", file), ret);
			}
			
			//Adds the change for renaming the file which contains the definition.
			RenameResourceChange resourceChange=new RenameResourceChange(declaringFile.getFullPath(), info.getNewName()+".nc");
			ret.add(resourceChange);
			
		} catch (Exception e){
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
	throws CoreException, OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if (!isApplicable()) {
			ret.addFatalError("The Refactoring is no Accessable");
		}
		Identifier selectedIdentifier=getSelectedIdentifier();
		InterfaceSelectionIdentifier selectionIdentifier=new InterfaceSelectionIdentifier(selectedIdentifier);
		if (!selectionIdentifier.isInterface()) {
			ret.addFatalError("No Interface selected.");
		}

		try {
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
				boolean isAlias=handleCaseInterfaceAliasInNesCComponentWiring(selectedIdentifier, pm);
				if(!isAlias){
					ret.addFatalError(message);
				}
			}
		} catch (MissingNatureException e) {
			ret.addFatalError("Project is not ready for refactoring!");
		}
		return ret;
	}
	
	/**
	 * Since we have no process monitor when we are deciding the processor type, we have to handle this case in the interface processor.
	 * Without process monitor we are unable to get an ast and without ast or at least a node we cant create a AstAnalyzer of the defining file
	 *  => we cannot decide if it is an interface or actually an alias.
	 * @param selectedIdentifier
	 * @param monitor
	 * @return
	 */
	private boolean handleCaseInterfaceAliasInNesCComponentWiring(Identifier selectedIdentifier,IProgressMonitor monitor){
		AliasSelectionIdentifier selectionIdentifier=new AliasSelectionIdentifier(selectedIdentifier);
		if(!selectionIdentifier.isInterfaceAliasInNescComponentWiring(getProjectUtil(),monitor)){
			return false;
		}
		selectionisInterfaceAliasInNesCComponentWiring=true;
		aliasProcessor=new tinyos.yeti.refactoring.rename.alias.Processor(info);
		return true;
	}

}
