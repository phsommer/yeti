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
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceReference;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory.AstType;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ASTUTil4Interfaces;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ASTUtil4Aliases;
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
		CompositeChange ret = new CompositeChange("Rename Interface "+ info.getOldName() + " to " + info.getNewName());
		Identifier selectedIdentifier=getSelectedIdentifier();
		
		//If it is a component alias, this is a pure local change.
		if(ASTUtil4Aliases.isComponentAlias(selectedIdentifier)){
			createConfigurationImplementationLocalChange(selectedIdentifier,ret);
			return ret;
		}
		
		//Else, if it is a interface alias, it is a global matter
		try {
			createInterfaceAliasChange(selectedIdentifier,ret,pm);
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
	private void createInterfaceAliasChange(Identifier selectedIdentifier, CompositeChange ret,IProgressMonitor pm) throws CoreException, MissingNatureException, IOException {
		//Get the name of the component which defines the alias
		String sourceComponentName=getNameOFSourceComponent(selectedIdentifier);
		if(sourceComponentName==null){
			ret.add(new NullChange("Implementation problem"));
			return;
		}
		
		//Get the ast of the defining component
		IDeclaration sourceDefinition=getComponentDefinition(sourceComponentName);
		IFile declaringFile=getIFile4ParseFile(sourceDefinition.getParseFile());
		NesC12AST ast=getAst(declaringFile, pm);
		
		//Get the InterfaceRerefence of the aliased interface in the ast which defines the alias
		InterfaceReference interfaceReference=ASTUtil4Aliases.getInterfaceNameWithAlias(selectedIdentifier.getName(),ast.getRoot());

		//Add Changes for referencing elements. Also the aliases reference the interface which they alias.
		Identifier aliasDefinition=ASTUTil4Interfaces.getInterfaceAliasIdentifier(interfaceReference);
		List<Identifier> identifiers=new LinkedList<Identifier>();

		Identifier aliasedInterfaceNameIdentifier=ASTUTil4Interfaces.getInterfaceNameIdentifier(interfaceReference);
		IDeclaration interfaceDefinition=getInterfaceDefinition(aliasedInterfaceNameIdentifier.getName());
		Collection<IASTModelPath> paths=new LinkedList<IASTModelPath>();
		paths.add(interfaceDefinition.getPath());
		for(IFile file:getAllFiles()){
			identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
			identifiers=getAliasFreeList(identifiers,selectedIdentifier.getName());
			if(file.equals(declaringFile)){	//Add change for alias definition
				identifiers.add(aliasDefinition);
			}
			addMultiTextEdit(identifiers, getAst(file, pm), file, createTextChangeName("interface", file), ret);
		}
		
	}
	
	/**
	 * Tries to find the name of the component, in whichs specification the alias is defined.
	 * @param selectedIdentifier
	 * @return
	 */
	private String getNameOFSourceComponent(Identifier selectedIdentifier){
		String sourceComponentName=null;
		if(ASTUtil4Aliases.isInterfaceAliasingInSpecification(selectedIdentifier)){
			Identifier componentIdentifier=ASTUtil4Components.getIdentifierOFComponentDefinition(selectedIdentifier);
			if(componentIdentifier==null){	//Should never happen.
				return null;
			}
			sourceComponentName=componentIdentifier.getName();
		}
		return sourceComponentName;
	}
	


	/**
	 * If the selected alias identifier is a rename in a NesC "components" statement in a NesC Configuration, then the scope of the alias is the implementation of the given configuration.
	 * This Method will create these local changes.
	 * @param selectedIdentifier
	 * @param ret The CompositeChange where to add the changes.
	 */
	private void createConfigurationImplementationLocalChange(Identifier selectedIdentifier, CompositeChange ret) {
		AstAnalyzerFactory analyzerFactory=new AstAnalyzerFactory();
		AstType createdType=analyzerFactory.createAnalyzer(selectedIdentifier);
		if(createdType!=AstType.CONFIGURATION){
			throw new IllegalStateException("This method should never be called, if the given identifier is not in a configuration ast!");
		}
		
		
		
		
		
		ConfigurationDeclarationList implementationRoot=ASTUtil4Components.getConfigurationImplementationNodeIfInside(selectedIdentifier);
		if(implementationRoot==null){	//Should never happen since the selected identifier has to be in a NesC "components" statement which only can appear in a Implementation of a NesC Configuration.
			DebugUtil.addOutput("RootNode is Null");
			ret.add(new NullChange("There is a implementation problem!"));
			return;
		}
		Collection<Identifier> localIdentifiers=ASTUtil.getAllNodesOfType(implementationRoot, Identifier.class);
		Collection<Identifier> identifiers2Change=new LinkedList<Identifier>();
		String targetName=selectedIdentifier.getName();
		for(Identifier identifier:localIdentifiers){
			if(targetName.equals(identifier.getName())){
				identifiers2Change.add(identifier);
			}
		}
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
		if (!ASTUtil4Aliases.isAlias(selectedIdentifier)) {
			ret.addFatalError("No Alias selected.");
		}

		//TODO
		if(true){
			return ret;
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
