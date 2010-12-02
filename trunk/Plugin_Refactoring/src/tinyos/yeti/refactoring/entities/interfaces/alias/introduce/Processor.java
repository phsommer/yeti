package tinyos.yeti.refactoring.entities.interfaces.alias.introduce;


import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.Refactoring;
import tinyos.yeti.refactoring.abstractrefactoring.rename.NesCComponentNameCollissionDetector;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.abstractrefactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ConfigurationAstAnalyzer;
import tinyos.yeti.refactoring.entities.interfaces.rename.InterfaceSelectionIdentifier;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class Processor extends RenameProcessor {
	
	private RenameInfo info;
	
	private String sourceComponentName;
	private AstAnalyzerFactory factory4DefiningAst;
	private IFile declaringFile;
	
	private Map<IFile,Collection<Identifier>> files2Identifiers;

	private Identifier declaringIdentifier;
	
	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	/**
	 * Before this call we have references to the aliased interface which are named after the old alias name.
	 * The problem is, that these interface aliases can actually be part of a different component, which aliases the same interface with the same alias.
	 * This function makes sure, that just references to the real defining component are included in the change.
	 * @param potentialReferences
	 * @param configurationAnalyzer
	 * @return	Just identifiers which really are associated to the defining component.
	 */
	private List<Identifier> getIdentifiersReferencingComponent(Collection<Identifier> potentialReferences,ConfigurationAstAnalyzer configurationAnalyzer) {
		Collection<String> referencedComponents=configurationAnalyzer.getNamesOfReferencedComponents();
		if(!referencedComponents.contains(sourceComponentName)){
			return Collections.emptyList();
		}
		Map<Identifier,Identifier> localComponentName2GlobalComponentName=configurationAnalyzer.getComponentLocalName2ComponentGlobalName();
		List<Identifier> realReferences=new LinkedList<Identifier>();
		String configurationName=configurationAnalyzer.getEntityName();
		for(Identifier candidate:potentialReferences){
			InterfaceSelectionIdentifier selectionIdentifier=new InterfaceSelectionIdentifier(candidate);
			if(selectionIdentifier.isComponentWiringInterfacePart()){
				Identifier associatedLocalComponentNameIdentifier=configurationAnalyzer.getAssociatedComponentIdentifier4InterfaceIdentifierInWiring(candidate);
				if(!configurationName.equals(associatedLocalComponentNameIdentifier.getName())){ //If the interface belongs to this configuration then it is for sure not associated to the alias defining component
					Identifier associatedGlobalComponentNameIdentifier=localComponentName2GlobalComponentName.get(associatedLocalComponentNameIdentifier);	//We have to resolve aliases.
					if(sourceComponentName.equals(associatedGlobalComponentNameIdentifier.getName())){
						realReferences.add(candidate);
					}
				}
			}
		}
		return realReferences;
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

		String interfaceName=getSelectedIdentifier().getName();
		
		//Get the interface definition of the interface which is to be aliased.
		//Get all references to it and filter out the ones which are associated with the component which now defines a new alias for it.
		//For those add changes.
		IDeclaration interfaceDefinition=getProjectUtil().getInterfaceDefinition(interfaceName);
		Collection<IASTModelPath> paths=new LinkedList<IASTModelPath>();
		paths.add(interfaceDefinition.getPath());
		Collection<Identifier> identifiers=new LinkedList<Identifier>();
		Map<IFile,Collection<Identifier>> files2Identifiers=new HashMap<IFile,Collection<Identifier>>();
		for(IFile file:getAllFiles()){
			identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
			identifiers=throwAwayDifferentNames(identifiers,interfaceName);
			if(identifiers.size()>0){
				if(file.equals(declaringFile)){
					AstAnalyzerFactory factory=new AstAnalyzerFactory(declaringIdentifier);
					InterfaceSelectionIdentifier selectionIdentifier;
					Collection<Identifier> identifiers2Change=new LinkedList<Identifier>();
					if(factory.hasModuleAnalyzerCreated()){
						for(Identifier id:identifiers){
							selectionIdentifier=new InterfaceSelectionIdentifier(id);
							if(selectionIdentifier.isInterfaceImplementation()||selectionIdentifier.isInterfacePartInNesCFunctionCallAndNoAlias()){
								identifiers2Change.add(id);
							}
						}
					}else if(factory.hasConfigurationAnalyzerCreated()){
						ConfigurationAstAnalyzer configurationAnalyzer=factory.getConfigurationAnalyzer(); 
						identifiers=configurationAnalyzer.getWiringInterfacePartIdentifiers();
						Identifier component=configurationAnalyzer.getEntityIdentifier();
						for(Identifier id:identifiers){
							if(id.getName().equals(interfaceName)){
								if(component==configurationAnalyzer.getAssociatedComponentIdentifier4InterfaceIdentifierInWiring(id)){
									identifiers2Change.add(id);
								}
							}
						}
					}
					files2Identifiers.put(declaringFile, identifiers2Change);
				}
				else{
					AstAnalyzerFactory factory=new AstAnalyzerFactory(file,getProjectUtil(),pm);
					if(factory.hasConfigurationAnalyzerCreated()){
						identifiers=getIdentifiersReferencingComponent(identifiers,factory.getConfigurationAnalyzer());
						files2Identifiers.put(file,identifiers);
					}
				}
			}	
		}
		return files2Identifiers;
	}

	@Override
	public String getProcessorName() {
		return Refactoring.RENAME_INTERFACE_ALIAS.getEntityName();
	}
	
	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		Identifier selectedIdentifier=getSelectedIdentifier();
		factory4DefiningAst=new AstAnalyzerFactory(selectedIdentifier);
		try {
			sourceComponentName=factory4DefiningAst.getComponentAnalyzer().getEntityName();
			declaringIdentifier=getSelectedIdentifier();
			declaringFile=ActionHandlerUtil.getInputFile(info.getEditor());
			files2Identifiers=collectIdentifiersToChange(pm);
		} catch (Exception e) {
			ret.addFatalError("Exception Occured during initialization. See project log for more information.");
			getProjectUtil().log("Exception Occured during initialization", e);
		}
		return ret;
	}
	
	@Override
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		NesCComponentNameCollissionDetector detector=new NesCComponentNameCollissionDetector();
		if(factory4DefiningAst.hasModuleAnalyzerCreated()){
			detector.newInterfaceNameWithLocalInterfaceName(factory4DefiningAst.getComponentAnalyzer(),declaringFile, info.getOldName(), info.getNewName(), ret);
		}else if(factory4DefiningAst.hasConfigurationAnalyzerCreated()){
			detector.handleCollisions4NewInterfaceNameWithConfigurationLocalName(factory4DefiningAst.getConfigurationAnalyzer(),declaringFile, info.getOldName(), info.getNewName(), ret);
		}
		return ret;
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {

		CompositeChange ret = createNewCompositeChange();
		
		try {
			//Add change for new alias definition
			TextChange textChange = new TextFileChange("interface alias introduction",declaringFile);
			MultiTextEdit edit=new MultiTextEdit();
			textChange.setEdit(edit);
			ret.add(textChange);
			NesC12AST ast=info.getAst();
			Collection<Identifier> definition=new LinkedList<Identifier>();
			definition.add(getSelectedIdentifier());
			addChanges4Identifiers(definition, info.getOldName()+" as "+info.getNewName(), edit, ast);
			
			//Add rename changes for component references.
			Collection<Identifier> identifiers2Change=files2Identifiers.get(declaringFile);
			if(identifiers2Change!=null&&identifiers2Change.size()>0){
				addChanges4Identifiers(identifiers2Change,info.getNewName(),edit,ast);
			}
			
			files2Identifiers.remove(declaringFile);
			files2Identifiers.keySet().remove(declaringFile);
			
			addChanges(files2Identifiers, ret, pm);
		} catch (Exception e){
			ret.add(new NullChange("Exception Occured! See project log for more information."));
			getProjectUtil().log("Exception Occured during change creation.", e);
		}
		return ret;
	}
}
