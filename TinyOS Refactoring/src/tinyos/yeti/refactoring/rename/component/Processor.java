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
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ASTUtil4Components;
import tinyos.yeti.refactoring.utilities.DebugUtil;

public class Processor extends RenameProcessor {

	private IDeclaration componentDefinition;
	
	private RenameInfo info;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	/**
	 * Looks for a component definition with the given name.
	 * @param identifier
	 * @param editor
	 * @return
	 * @throws CoreException
	 * @throws MissingNatureException
	 */
	private IDeclaration getComponentDefinition(String componentName) throws CoreException, MissingNatureException{
		ProjectModel model=getModel();
		List<IDeclaration> declarations=new LinkedList<IDeclaration>();
		declarations.addAll(model.getDeclarations(Kind.MODULE));
		declarations.addAll(model.getDeclarations(Kind.CONFIGURATION));
		for(IDeclaration declaration:declarations){
			if(componentName.equals(declaration.getName())){
				return declaration;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns a list which doesnt contain aliases which have a different name then the component to be refactored and therefore dont have to be touched.
	 * This is needed since aliases in configuration definitions also reference the original component.
	 * @param identifiers
	 */
	private List<Identifier> getAliasFreeList(List<Identifier> identifiers,String interfaceNameToChange) {
		List<Identifier> result=new LinkedList<Identifier>();
		for(Identifier identifier:identifiers){
			if(interfaceNameToChange.equals(identifier.getName())){
				result.add(identifier);
			}
		}
		return result;
	}

	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		DebugUtil.clearOutput();
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
				identifiers=getAliasFreeList(identifiers,declaringIdentifier.getName());
				addMultiTextEdit(identifiers, getAst(file, pm), file, createTextChangeName("interface", file), ret);
			}
			
			//Adds the change for renaming the file which contains the definition.
			RenameResourceChange resourceChange=new RenameResourceChange(declaringFile.getFullPath(), info.getNewName()+".nc");
			ret.add(resourceChange);
			
		} catch (Exception e){
			e.printStackTrace();
		}
		DebugUtil.printOutput();
		return ret;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,CheckConditionsContext context) 
	throws CoreException,OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if (!isApplicable()) {
			ret.addFatalError("The Refactoring is not Applicable");
		}
		return ret;
		// TODO checkFinalConditions not yet implemented

	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
	throws CoreException, OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if (!isApplicable()) {
			ret.addFatalError("The Refactoring is no Accessable");
		}
		Identifier selectedIdentifier=getSelectedIdentifier();
		ASTUtil4Components astUtil4Components=new ASTUtil4Components();
		if (!astUtil4Components.isComponent(selectedIdentifier)) {
			ret.addFatalError("No Interface selected.");
		}

		try {
			componentDefinition = getComponentDefinition(selectedIdentifier.getName());
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

	@Override
	public Object[] getElements() {
		// TODO Auto-generated method stub
		return new Object[] {};
	}

	@Override
	public String getIdentifier() {
		return "tinyos.yeti.refactoring.renameFunction.Processor";
	}

	@Override
	public String getProcessorName() {
		return info.getInputPageName();
	}

	@Override
	public boolean isApplicable() 
	throws CoreException {
		return super.isApplicable();
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,SharableParticipants sharedParticipants) 
	throws CoreException {
		// TODO Auto-generated method stub
		return new RefactoringParticipant[] {};
	}

}
