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
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.ComponentAstAnalyser;
import tinyos.yeti.refactoring.ast.ConfigurationAstAnalyzer;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.selection.AliasSelectionIdentifier;
import tinyos.yeti.refactoring.utilities.DebugUtil;

public class Processor extends RenameProcessor {
	
	private RenameInfo info;
	
	private AstAnalyzerFactory factory4Selection;
	private AliasSelectionIdentifier selectionIdentifier;
	
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
	 * Looks for a interface definition with the given name.
	 * @param identifier
	 * @param editor
	 * @return
	 * @throws CoreException
	 * @throws MissingNatureException
	 */
	private IDeclaration getInterfaceDefinition(String interfaceName) throws CoreException, MissingNatureException{
		ProjectModel model=getModel();
		List<IDeclaration> declarations=model.getDeclarations(Kind.INTERFACE);
		for(IDeclaration declaration:declarations){
			if(interfaceName.equals(declaration.getName())){
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
		DebugUtil.printOutput();
		return ret;
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
		String sourceComponentName=getNameOFSourceComponent();
		DebugUtil.addOutput("sourceComponentName: "+sourceComponentName);
		
		//Get the ComponentAstAnalyzer of the defining component
		IDeclaration sourceDefinition=getComponentDefinition(sourceComponentName);
		if(sourceDefinition==null){
			throw new IllegalStateException("Could not find a definition for the source component.");
		}
		IFile declaringFile=getIFile4ParseFile(sourceDefinition.getParseFile());
		NesC12AST ast=getAst(declaringFile, pm);
		AstAnalyzerFactory factory4DefiningAst=new AstAnalyzerFactory(ast.getRoot());
		if(!factory4DefiningAst.hasComponentAnalyzerCreated()){
			ret.add(new NullChange("Implementation problem"));	//The alias definition has to be in a NesC component specification.
		}
		ComponentAstAnalyser componentAnalyzer=factory4DefiningAst.getComponentAnalyzer();

		//Get the Identifier in the defining component which stands for the alias in the alias definition.
		componentAnalyzer.getAliasIdentifier4InterfaceAliasName(selectedIdentifier.getName());
		Identifier aliasDefinition=componentAnalyzer.getAliasIdentifier4InterfaceAliasName(aliasName);
		DebugUtil.addOutput("aliasDefinition: "+aliasDefinition.getName());
		
		//Get the interface definition of the interface which is aliased.
		//Get all references to it and filter out the one which are aliases.
		//For those add changes.
		String aliasedInterfaceName=componentAnalyzer.getInterfaceName4InterfaceAliasName(aliasName);
		IDeclaration interfaceDefinition=getInterfaceDefinition(aliasedInterfaceName);
		Collection<IASTModelPath> paths=new LinkedList<IASTModelPath>();
		paths.add(interfaceDefinition.getPath());
		List<Identifier> identifiers=new LinkedList<Identifier>();
		for(IFile file:getAllFiles()){
			if(file.equals(declaringFile)){	//Add change for alias definition this is the only NesC Component which can be a module which can have references to the interface definition which belong to the given alias.
				identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
				identifiers=getAliasFreeList(identifiers,aliasName);
				identifiers.add(aliasDefinition);
				addMultiTextEdit(identifiers, getAst(file, pm), file, createTextChangeName("interface", file), ret);
			}
			else if(isConfigurationReferencingDefiningModule(sourceComponentName,file,pm)){	//All other references have to be in a NesC Configuration
				identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
				identifiers=getAliasFreeList(identifiers,aliasName);
				addMultiTextEdit(identifiers, getAst(file, pm), file, createTextChangeName("interface", file), ret);
			}
			
		}
		
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
	private String getNameOFSourceComponent(){
		if(selectionIdentifier.isInterfaceAliasingInSpecification()||selectionIdentifier.isInterfaceAliasInNescFunction()){	//In this case the selection is in the component which defines the alias.
			return factory4Selection.getComponentAnalyzer().getComponentName();
		}else if(selectionIdentifier.isInterfaceAliasInNescComponentWiring()){	
			DebugUtil.immediatePrint("isInterfaceAliasInNescComponentWiring");
			ConfigurationAstAnalyzer analyzer=factory4Selection.getConfigurationAnalyzer();
			Identifier associatedComponent=analyzer.getAssociatedComponentIdentifier4InterfaceIdentifierInWiring(selectionIdentifier.getSelection());
			DebugUtil.immediatePrint("Associated Component: "+((associatedComponent!=null)?associatedComponent.getName():"null"));
			if(associatedComponent==null){	//this should never happen
				throw new IllegalStateException("Could not find an associated Component for the given interface identifier");
			}
			if(associatedComponent==analyzer.getComponentIdentifier()){	//In this case the interfaceIdentifier has a implizit reference on the configuration itself.
				return associatedComponent.getName();
			}
			Identifier realComponent=analyzer.getComponentIdentifier4ComponentAliasIdentifier(associatedComponent.getName());
			DebugUtil.immediatePrint("Real Component: "+((realComponent!=null)?realComponent.getName():"null"));
			if(realComponent!=null){	//If the associatedComponent is allready the real component, then realComponent is null.
				return realComponent.getName();
			}
			return associatedComponent.getName();
			
		}
		return null;
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
		factory4Selection=new AstAnalyzerFactory(selectedIdentifier);
		selectionIdentifier=new AliasSelectionIdentifier(selectedIdentifier);
		if (!selectionIdentifier.isAlias(selectedIdentifier)) {
			ret.addFatalError("No Alias selected.");
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
		return "tinyos.yeti.refactoring.rename.alias.Processor";
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
