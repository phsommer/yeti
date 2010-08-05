package tinyos.yeti.refactoring.rename.alias.interfaces;


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

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ComponentAstAnalyser;
import tinyos.yeti.refactoring.ast.ConfigurationAstAnalyzer;
import tinyos.yeti.refactoring.rename.NameCollissionDetector;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.selection.AliasSelectionIdentifier;

public class Processor extends RenameProcessor {
	
	private RenameInfo info;
	
	private AstAnalyzerFactory factory4Selection;
	private AliasSelectionIdentifier selectionIdentifier;
	
	private String sourceComponentName;
	private AstAnalyzerFactory factory4DefiningAst;
	private IFile declaringFile;
	
	private Map<IFile,Collection<Identifier>> files2Identifiers;
	
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
	 * Collects all identifiers which are affected by this refactoring and associates them with the file they rest in.
	 * @param selectedIdentifier
	 * @param ret
	 * @throws MissingNatureException 
	 * @throws CoreException 
	 * @throws IOException 
	 */
	private Map<IFile,Collection<Identifier>> collectIdentifiersToChange(IProgressMonitor pm) throws CoreException, MissingNatureException, IOException {

		String aliasName=getSelectedIdentifier().getName();
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
		Map<IFile,Collection<Identifier>> files2Identifiers=new HashMap<IFile,Collection<Identifier>>();
		for(IFile file:getAllFiles()){
			
			if(file.equals(declaringFile)){	//Add change for alias definition this is the only NesC Component which can be a module which can have references to the interface definition which belong to the given alias.
				identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
				identifiers=throwAwayDifferentNames(identifiers,aliasName);
				identifiers.add(aliasDefinition);
				files2Identifiers.put(file,identifiers);
			}
			else if(isConfigurationReferencingDefiningModule(sourceComponentName,file,pm)){	//All other references have to be in a NesC Configuration
				identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
				identifiers=throwAwayDifferentNames(identifiers,aliasName);
				if(identifiers.size()>0){
					files2Identifiers.put(file,identifiers);
				}
			}	
		}
		return files2Identifiers;
	}
	

	@Override
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		NameCollissionDetector detector=new NameCollissionDetector();
		if(factory4DefiningAst.hasModuleAnalyzerCreated()){
			detector.newInterfaceNameWithLocalInterfaceName(factory4DefiningAst.getComponentAnalyzer(),declaringFile, info.getOldName(), info.getNewName(), ret);
		}else if(factory4DefiningAst.hasConfigurationAnalyzerCreated()){
			detector.handleCollisions4NewInterfaceNameWithConfigurationLocalName(factory4DefiningAst.getConfigurationAnalyzer(),declaringFile, info.getOldName(), info.getNewName(), ret);
		}
		return ret;
	}
	
	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		Identifier selectedIdentifier=getSelectedIdentifier();
		factory4Selection=new AstAnalyzerFactory(selectedIdentifier);
		selectionIdentifier=new AliasSelectionIdentifier(selectedIdentifier);
		try {
			//Get the name of the component which defines the alias
			sourceComponentName=getNameOFSourceComponent(pm);
			
			//Get the ComponentAstAnalyzer of the defining component
			IDeclaration sourceDefinition=getProjectUtil().getComponentDefinition(sourceComponentName);
			if(sourceDefinition==null){
				ret.addFatalError("Could not find a definition for the source component!");
				return ret;
			}
			declaringFile=getProjectUtil().getDeclaringFile(sourceDefinition);
			if(declaringFile==null||!getProjectUtil().isProjectFile(declaringFile)){	//If the source definition is not in this project, we are not allowed/able to rename the alias.
				ret.addFatalError("The component which defines the alias is out of project range!");
				return ret;
			}
			factory4DefiningAst=new AstAnalyzerFactory(declaringFile,getProjectUtil(), pm);
			if(!factory4DefiningAst.hasComponentAnalyzerCreated()){
				ret.addFatalError("Alias Definition was not in a NesC component specification!");
				return ret;
			}
			files2Identifiers=collectIdentifiersToChange(pm);
			return ret;
		} catch (Exception e) {
			ret.addFatalError("Exception Occured during initialization!");
			return ret;
		}
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {

		CompositeChange ret = new CompositeChange("Rename alias "+ info.getOldName() + " to " + info.getNewName());
		
		try {
			addChanges(files2Identifiers, ret, pm);
		} catch (Exception e){
			ret.add(new NullChange("Exception Occured!"));
		}
		return ret;
	}
}
