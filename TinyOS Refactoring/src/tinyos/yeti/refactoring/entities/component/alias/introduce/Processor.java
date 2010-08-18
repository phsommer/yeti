package tinyos.yeti.refactoring.entities.component.alias.introduce;


import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.Refactoring;
import tinyos.yeti.refactoring.abstractrefactoring.rename.NesCComponentNameCollissionDetector;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ConfigurationAstAnalyzer;

public class Processor extends RenameProcessor {
	
	private RenameInfo info;
	
	private AstAnalyzerFactory factory4Selection;
	private IFile editedFile;
	
	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	@Override
	public String getProcessorName() {
		return "introduce "+Refactoring.INTRODUCE_COMPONENT_ALIAS.getEntityName();
	}
	
	/**
	 * If the selected alias identifier is a rename in a NesC "components" statement in a NesC Configuration, then the scope of the alias is the implementation of the given configuration.
	 * This Method will create these local changes.
	 * @param ret The CompositeChange where to add the changes.
	 */
	private void createConfigurationImplementationLocalChange(CompositeChange ret) {
		//Add change for new alias definition
		TextChange textChange = new TextFileChange("component alias introduction",editedFile);
		MultiTextEdit edit=new MultiTextEdit();
		textChange.setEdit(edit);
		ret.add(textChange);
		NesC12AST ast=info.getAst();
		Collection<Identifier> definition=new LinkedList<Identifier>();
		definition.add(getSelectedIdentifier());
		addChanges4Identifiers(definition, info.getOldName()+" as "+info.getNewName(), edit, ast);
		
		//Add rename changes for component references.
		ConfigurationAstAnalyzer configurationAnalyzer=factory4Selection.getConfigurationAnalyzer();
		Collection<Identifier> identifiers2Change=configurationAnalyzer.getWiringComponentPartIdentifiers();
		identifiers2Change=throwAwayDifferentNames(identifiers2Change, info.getOldName());
		addChanges4Identifiers(identifiers2Change,info.getNewName(),edit,ast);
	}

	
	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		Identifier selectedIdentifier=getSelectedIdentifier();
		factory4Selection=new AstAnalyzerFactory(selectedIdentifier);
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			ret.addFatalError("Selection isnt accurate!");
			return ret;
		}
		editedFile=(IFile)info.getEditor().getResource();
		return ret;
	}
	
	@Override
	public RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		NesCComponentNameCollissionDetector detector=new NesCComponentNameCollissionDetector();
		detector.handleCollisions4NewComponentNameWithConfigurationLocalName(factory4Selection.getConfigurationAnalyzer(),editedFile,info.getOldName(),info.getNewName(),ret);
		return ret;
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {

		CompositeChange ret = new CompositeChange("Introducing component alias \""+info.getNewName()+"\" for component \""+info.getOldName()+"\".");
		createConfigurationImplementationLocalChange(ret);
		return ret;
	}
}
