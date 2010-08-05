package tinyos.yeti.refactoring.rename.alias.component;


import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ConfigurationAstAnalyzer;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.selection.AliasSelectionIdentifier;

public class Processor extends RenameProcessor {
	
	private RenameInfo info;
	
	private AstAnalyzerFactory factory4Selection;
	private AliasSelectionIdentifier selectionIdentifier;
	private IFile editedFile;
	
	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	@Override
	public RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			ret.addFatalError("Selection isnt accurate!");
			return ret;
		}
		
		//Check if there is a local component name with the same name.
		ConfigurationAstAnalyzer configurationAnalyzer=factory4Selection.getConfigurationAnalyzer();
		Identifier toRename=getSelectedIdentifier();
		Set<Identifier> localComponentNames=configurationAnalyzer.getComponentLocalName2ComponentGlobalName().keySet();
		Identifier sameName=getAstUtil().getIdentifierWithEqualName(info.getNewName(), localComponentNames);
		
		//Check if there is a local interface name with the same name.
		//This check is only done, if there is not allready a local component name with the same name.
		if(sameName==null){
			Set<Identifier> localInterfaceNames=configurationAnalyzer.getInterfaceLocalName2InterfaceGlobalName().keySet();
			sameName=getAstUtil().getIdentifierWithEqualName(info.getNewName(), localInterfaceNames);
		}
		
		if(sameName!=null){
			Region toRenameRegion= new Region(toRename.getRange().getLeft(),toRename.getName().length());
			Region sameNameRegion= new Region(sameName.getRange().getLeft(),sameName.getName().length());
			ret.addError("You intended to rename the component alias "+toRename.getName()+" to "+sameName.getName(),new FileStatusContext(editedFile, toRenameRegion));
			ret.addError("You have a collision with this identifier: "+sameName.getName(),new FileStatusContext(editedFile, sameNameRegion));
			return ret;
		}
		return ret;
	}
	
	/**
	 * If the selected alias identifier is a rename in a NesC "components" statement in a NesC Configuration, then the scope of the alias is the implementation of the given configuration.
	 * This Method will create these local changes.
	 * @param ret The CompositeChange where to add the changes.
	 */
	private void createConfigurationImplementationLocalChange(CompositeChange ret) {
		ConfigurationAstAnalyzer configurationAnalyzer=factory4Selection.getConfigurationAnalyzer();
		Collection<Identifier> identifiers2Change=configurationAnalyzer.getComponentAliasIdentifiersWithName(selectionIdentifier.getSelection().getName());
		NesC12AST ast=info.getAst();
		addMultiTextEdit(identifiers2Change, ast, editedFile, createTextChangeName("alias", editedFile), ret);
	}

	
	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		Identifier selectedIdentifier=getSelectedIdentifier();
		factory4Selection=new AstAnalyzerFactory(selectedIdentifier);
		selectionIdentifier=new AliasSelectionIdentifier(selectedIdentifier);
		editedFile=(IFile)info.getEditor().getResource();
		return new RefactoringStatus();
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {

		CompositeChange ret = new CompositeChange("Rename alias "+ info.getOldName() + " to " + info.getNewName());
		createConfigurationImplementationLocalChange(ret);
		return ret;
	}
}
