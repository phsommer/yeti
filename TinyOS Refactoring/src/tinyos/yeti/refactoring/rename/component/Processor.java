package tinyos.yeti.refactoring.rename.component;


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
import tinyos.yeti.refactoring.selection.ComponentSelectionIdentifier;

public class Processor extends RenameProcessor {

	private IDeclaration componentDefinition;
	
	private RenameInfo info;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}

	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		CompositeChange ret = new CompositeChange("Rename Interface "+ info.getOldName() + " to " + info.getNewName());
		try {
			//Add Change for component definition
			IFile declaringFile=getIFile4ParseFile(componentDefinition.getParseFile());
			Identifier declaringIdentifier=getIdentifierForPath(componentDefinition.getPath(), pm);
			List<Identifier> identifiers=new LinkedList<Identifier>();
			identifiers.add(declaringIdentifier);
			addMultiTextEdit(identifiers, getAst(declaringFile, pm), declaringFile, createTextChangeName("component", declaringFile), ret);
			
			//Add Changes for referencing elements
			Collection<IASTModelPath> paths=new LinkedList<IASTModelPath>();
			paths.add(componentDefinition.getPath());
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
		ComponentSelectionIdentifier selectionIdentifier=new ComponentSelectionIdentifier(selectedIdentifier);
		if (!selectionIdentifier.isComponent(selectedIdentifier)) {
			ret.addFatalError("No Component selected.");
		}

		try {
			componentDefinition = getProjectUtil().getComponentDefinition(selectedIdentifier.getName());
			if(componentDefinition==null){
				ret.addFatalError("Did not find an component Definition, for selection: "+selectedIdentifier.getName()+"!");
			}
			else if(!componentDefinition.getParseFile().isProjectFile()){
				ret.addFatalError("Component definition is out of project range!");
			}
		} catch (MissingNatureException e) {
			ret.addFatalError("Project is not ready for refactoring!");
		}
		return ret;
	}

}