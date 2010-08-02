package tinyos.yeti.refactoring.rename.alias;


import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ComponentAstAnalyser;
import tinyos.yeti.refactoring.ast.ConfigurationAstAnalyzer;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.selection.AliasSelectionIdentifier;

public class Processor extends RenameProcessor {
	
	private RenameInfo info;
	
	private AstAnalyzerFactory factory4Selection;
	private AliasSelectionIdentifier selectionIdentifier;
	
	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	/**
	 * Checks if the given file includes a configuration which references the defining module.
	 * The reason for this check is, that there can be other modules which rename the same interface with the same alias, the references to this alias would without this check also be changed.
	 * @param file
	 * @return
	 * @throws MissingNatureException 
	 * @throws IOException 
	 */
	private boolean isConfigurationReferencingDefiningModule(String definingModuleName,IFile file,IProgressMonitor pm) throws IOException, MissingNatureException {
		NesC12AST ast=getAst(file, pm);
		AstAnalyzerFactory factory4referencingEntity=new AstAnalyzerFactory(ast.getRoot());
		if(!factory4referencingEntity.hasConfigurationAnalyzerCreated()){	//The defining module is treated separatly.
			return false;
		}
		ConfigurationAstAnalyzer analyzer=factory4referencingEntity.getConfigurationAnalyzer();
		Collection<String> referencedComponents=analyzer.getNamesOfReferencedComponents();
		return referencedComponents.contains(definingModuleName);
	}

	/**
	 * Tries to find the name of the component, in whichs specification the alias is defined.
	 * Returns null if the selected identifier is not an interfaceIdentifier.
	 * @param selectedIdentifier
	 * @return
	 */
	private String getNameOFSourceComponent(IProgressMonitor monitor){
		if(selectionIdentifier.isInterfaceAliasingInSpecification()||selectionIdentifier.isInterfaceAliasInNescFunction()){	//In this case the selection is in the component which defines the alias.
			return factory4Selection.getComponentAnalyzer().getEntityName();
		}else if(selectionIdentifier.isInterfaceAliasInNescComponentWiring(getProjectUtil(),monitor)){	
			ConfigurationAstAnalyzer analyzer=factory4Selection.getConfigurationAnalyzer();
			return analyzer.getUseDefiningComponent4InterfaceInWiring(selectionIdentifier.getSelection());
			
		}
		return null;
	}

	/**
	 * If the selected alias is an alias for a interface, in a module/configuration specification, then this method creates the appropriate changes.
	 * @param selectedIdentifier
	 * @param ret
	 * @throws MissingNatureException 
	 * @throws CoreException 
	 * @throws IOException 
	 */
	private void createInterfaceAliasChange(CompositeChange ret,IProgressMonitor pm) throws CoreException, MissingNatureException, IOException {
		Identifier selectedIdentifier=selectionIdentifier.getSelection();
		String aliasName=selectedIdentifier.getName();
		
		//Get the name of the component which defines the alias
		String sourceComponentName=getNameOFSourceComponent(pm);
		
		//Get the ComponentAstAnalyzer of the defining component
		IDeclaration sourceDefinition=getProjectUtil().getComponentDefinition(sourceComponentName);
		if(sourceDefinition==null){
			String reason="Could not find a definition for the source component.";
			markRefactoringAsInfeasible(reason);
			ret.add(new NullChange(reason));
			return;
		}
		IFile declaringFile=getProjectUtil().getDeclaringFile(sourceDefinition);
		if(declaringFile==null||!getProjectUtil().isProjectFile(declaringFile)){	//If the source definition is not in this project, we are not allowed/able to rename the alias.
			String reason="The component which defines the alias is out of project range!";
			markRefactoringAsInfeasible(reason);
			ret.add(new NullChange(reason));
			return;
		}
		AstAnalyzerFactory factory4DefiningAst=new AstAnalyzerFactory(declaringFile,getProjectUtil(), pm);
		if(!factory4DefiningAst.hasComponentAnalyzerCreated()){
			String reason="Alias Definition was not in a NesC component specification!";
			markRefactoringAsInfeasible(reason);
			ret.add(new NullChange(reason));
			return;	//The alias definition has to be in a NesC component specification.
		}
		ComponentAstAnalyser componentAnalyzer=factory4DefiningAst.getComponentAnalyzer();

		//Get the Identifier in the defining component which stands for the alias in the alias definition.
		Identifier aliasDefinition=componentAnalyzer.getAliasIdentifier4InterfaceAliasName(aliasName);
		
		//Get the interface definition of the interface which is aliased.
		//Get all references to it and filter out the one which are aliases.
		//For those add changes.
		String aliasedInterfaceName=componentAnalyzer.getInterfaceName4InterfaceAliasName(aliasName);
		IDeclaration interfaceDefinition=getProjectUtil().getInterfaceDefinition(aliasedInterfaceName);
		Collection<IASTModelPath> paths=new LinkedList<IASTModelPath>();
		paths.add(interfaceDefinition.getPath());
		List<Identifier> identifiers=new LinkedList<Identifier>();
		for(IFile file:getAllFiles()){
			
			if(file.equals(declaringFile)){	//Add change for alias definition this is the only NesC Component which can be a module which can have references to the interface definition which belong to the given alias.
				identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
				identifiers=throwAwayDifferentNames(identifiers,aliasName);
				identifiers.add(aliasDefinition);
				addMultiTextEdit(identifiers, getAst(file, pm), file, createTextChangeName("interface", file), ret);
			}
			else if(isConfigurationReferencingDefiningModule(sourceComponentName,file,pm)){	//All other references have to be in a NesC Configuration
				identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
				identifiers=throwAwayDifferentNames(identifiers,aliasName);
				addMultiTextEdit(identifiers, getAst(file, pm), file, createTextChangeName("interface", file), ret);
			}	
		}
	}

	/**
	 * If the selected alias identifier is a rename in a NesC "components" statement in a NesC Configuration, then the scope of the alias is the implementation of the given configuration.
	 * This Method will create these local changes.
	 * @param ret The CompositeChange where to add the changes.
	 */
	private void createConfigurationImplementationLocalChange(CompositeChange ret) {
		if(!factory4Selection.hasConfigurationAnalyzerCreated()){
			throw new IllegalStateException("This method should never be called, if the given identifier is not in a configuration ast!");
		}
		Collection<Identifier> identifiers2Change=factory4Selection.getConfigurationAnalyzer().getComponentAliasIdentifiersWithName(selectionIdentifier.getSelection().getName());
		
		NesCEditor editor=info.getEditor();
		IFile editedFile=(IFile)editor.getResource();
		NesC12AST ast=info.getAst();
		addMultiTextEdit(identifiers2Change, ast, editedFile, createTextChangeName("alias", editedFile), ret);
	}

	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		Identifier selectedIdentifier=getSelectedIdentifier();
		factory4Selection=new AstAnalyzerFactory(selectedIdentifier);
		selectionIdentifier=new AliasSelectionIdentifier(selectedIdentifier);
		CompositeChange ret = new CompositeChange("Rename alias "+ info.getOldName() + " to " + info.getNewName());
		
		//If it is a component alias, this is a pure local change.
		if(selectionIdentifier.isComponentAlias()){
			createConfigurationImplementationLocalChange(ret);
			return ret;
		}
		
		//Else, if it is a interface alias, it is a global matter
		try {
			createInterfaceAliasChange(ret,pm);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		return ret;
	}

}
