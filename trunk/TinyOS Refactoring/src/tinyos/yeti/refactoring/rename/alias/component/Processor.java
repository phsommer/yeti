package tinyos.yeti.refactoring.rename.alias.component;


import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.Refactoring;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ConfigurationAstAnalyzer;
import tinyos.yeti.refactoring.rename.NesCComponentNameCollissionDetector;
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
	public String getProcessorName() {
		return Refactoring.RENAME_COMPONENT_ALIAS.getEntityName();
	}
	
	@Override
	public RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			ret.addFatalError("Selection isnt accurate!");
			return ret;
		}
		NesCComponentNameCollissionDetector detector=new NesCComponentNameCollissionDetector();
		detector.handleCollisions4NewComponentNameWithConfigurationLocalName(factory4Selection.getConfigurationAnalyzer(),editedFile,info.getOldName(),info.getNewName(),ret);
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
		addMultiTextEdit(identifiers2Change, ast, editedFile, createTextChangeName(editedFile), ret);
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

		CompositeChange ret = createNewCompositeChange();
		createConfigurationImplementationLocalChange(ret);
		return ret;
	}
}
