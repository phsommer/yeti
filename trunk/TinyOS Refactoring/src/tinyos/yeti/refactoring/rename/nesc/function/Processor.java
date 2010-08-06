package tinyos.yeti.refactoring.rename.nesc.function;


import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.InterfaceAstAnalyzer;
import tinyos.yeti.refactoring.ast.ModuleAstAnalyzer;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.selection.NescFunctionSelectionIdentifier;
import tinyos.yeti.refactoring.utilities.DebugUtil;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class Processor extends RenameProcessor {
	
	private RenameInfo info;
	
	private AstAnalyzerFactory factory4Selection;
	private InterfaceAstAnalyzer interfaceDefinitionAnalyzer;
	private NescFunctionSelectionIdentifier selectionIdentifier;
	private IDeclaration definingInterfaceDeclaration;
	private IFile declaringFile;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	/**
	 * Returns the name of the interface which declares the selected function.
	 * @return
	 */
	private String getInterfaceDefinitionName(){
		if(selectionIdentifier.isFunctionDeclaration()){
			InterfaceAstAnalyzer analyzer=factory4Selection.getInterfaceAnalyzer();
			return analyzer.getEntityIdentifier().getName();
		}
		else if(selectionIdentifier.isFunctionDefinition()){
			ModuleAstAnalyzer analyzer=factory4Selection.getModuleAnalyzer();
			Identifier localInterfaceName=analyzer.getAssociatedInterfaceName4FunctionIdentifier(selectionIdentifier.getSelection());
			return analyzer.getInterfaceLocalName2InterfaceGlobalName().get(localInterfaceName).getName();
		}
		else if(selectionIdentifier.isFunctionCall()){
			Identifier associatedInterface=selectionIdentifier.getAssociatedInterface2FunctionCall();
			String localInterfaceName=associatedInterface.getName();
			ModuleAstAnalyzer analyzer=factory4Selection.getModuleAnalyzer();
			Identifier globalInterfaceName=analyzer.getInterfaceLocalName2InterfaceGlobalName().get(new Identifier(localInterfaceName));
			return globalInterfaceName.getName();
			
		}
		return null;
	}
	
	/**
	 * Initializes the fields, which hold information about the interface definition.
	 * @param pm
	 */
	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus initializationStatus=new RefactoringStatus();

		Identifier selectedIdentifier=getSelectedIdentifier();
		factory4Selection=new AstAnalyzerFactory(selectedIdentifier);
		selectionIdentifier=new NescFunctionSelectionIdentifier(selectedIdentifier,factory4Selection);

		try {
			//Get the InterfaceAstAnalyzer of the interface definition of the selected identifier 
			String definingInterfaceName=getInterfaceDefinitionName();
			ProjectUtil projectUtil=getProjectUtil();
			definingInterfaceDeclaration=projectUtil.getInterfaceDefinition(definingInterfaceName);
			declaringFile=getIFile4ParseFile(definingInterfaceDeclaration.getParseFile());
			if(!projectUtil.isProjectFile(declaringFile)){
				initializationStatus.addFatalError("Defining interface is out of project range!");
			}
			AstAnalyzerFactory factory4definingInterface=new AstAnalyzerFactory(declaringFile, projectUtil, pm);
			if(!factory4definingInterface.hasInterfaceAnalyzerCreated()){
				initializationStatus.addFatalError("The Interface Ast seems not to be valid!");
			}
			interfaceDefinitionAnalyzer=factory4definingInterface.getInterfaceAnalyzer();
		}
		catch(Exception e){
			initializationStatus.addFatalError("Exception occured during initialization! See project log for more information.");
			getProjectUtil().log("Exception during initialization.", e);
		}
		return initializationStatus;
	}
	
	/**
	 * Checks if the user input new function name and some existing name are equal and so have a collision.
	 * Returns true if there is a collision.
	 * @param pm
	 */
	@Override
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		String newName=info.getNewName();
		String oldName=info.getOldName();
		Identifier toRename=null;
		Identifier sameName=null;
		for(Identifier identifier:interfaceDefinitionAnalyzer.getNesCFunctionIdentifiers()){
			if(newName.equals(identifier.getName())){
				sameName=identifier;
			}
			if(oldName.equals(identifier.getName())){
				toRename=identifier;
			}
		}
		if(sameName!=null){	//The new name already exists and it will lead to an compile error if we do introduce the same name a second time.
			Identifier interfaceIdentifier=interfaceDefinitionAnalyzer.getEntityIdentifier();
			Region interfaceRegion=new Region(interfaceIdentifier.getRange().getLeft(),interfaceIdentifier.getName().length());
			Region toRenameRegion= new Region(toRename.getRange().getLeft(),toRename.getName().length());
			Region sameNameRegion= new Region(sameName.getRange().getLeft(),sameName.getName().length());
			ret.addError("The new name you selected for the nesc function is already used in the defining interface!",new FileStatusContext(declaringFile, interfaceRegion));
			ret.addError("You intended to rename the identifier "+toRename.getName()+" to "+sameName.getName(),new FileStatusContext(declaringFile, toRenameRegion));
			ret.addError("You have a collision with this identifier: "+sameName.getName(),new FileStatusContext(declaringFile, sameNameRegion));
		}
		return ret;
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		CompositeChange ret = new CompositeChange("Rename Nesc Function "+ info.getOldName() + " to " + info.getNewName());
		Identifier selectedIdentifier=getSelectedIdentifier();
		try{
			//Add change for the function name identifier in the interface definition.
			Identifier definingIdentifier=getAstUtil().getIdentifierWithEqualName(selectedIdentifier.getName(),interfaceDefinitionAnalyzer.getNesCFunctionIdentifiers());
			List<Identifier> identifiers=new LinkedList<Identifier>();
			identifiers.add(definingIdentifier);
			NesC12AST ast=getAst(declaringFile,pm);
			addMultiTextEdit(identifiers, ast, declaringFile, createTextChangeName("nesc function", declaringFile), ret);
			
			//Add Changes for function name identifiers in function definitions.
			Collection<IASTModelPath> paths=new LinkedList<IASTModelPath>();
			InitDeclarator declarator=getAstUtil().getParentForName(definingIdentifier, InitDeclarator.class);
			paths.add(declarator.resolveField().getPath());
			for(IFile file:getAllFiles()){
				identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
				identifiers=filterFunctionReferences(identifiers);
				if(identifiers.size()>0){
					addMultiTextEdit(identifiers, getAst(file, pm), file, createTextChangeName("nesc function", file), ret);
				}
			}
			
		} catch (Exception e){
			ret.add(new NullChange("Exception during change creation. See project log for more information."));
			getProjectUtil().log("Exception during change creation.", e);
		}
		return ret;
	}
	
	/**
	 * Creates a new list which just includes references, which must be renamed.
	 * I.E. the interface part of a function call has not to be renamed, but it references the function too.
	 * @param identifiers
	 * @return
	 */
	private List<Identifier> filterFunctionReferences(List<Identifier> identifiers) {
		if(identifiers.size()==0){
			return Collections.emptyList();
		}
		AstAnalyzerFactory analyzerFactory=new AstAnalyzerFactory(identifiers.get(0));
		if(!analyzerFactory.hasModuleAnalyzerCreated()){
			return Collections.emptyList();
		}
		List<Identifier> wantedReferences=new LinkedList<Identifier>();
		for(Identifier id:identifiers){
			NescFunctionSelectionIdentifier selectionIdentifier=new NescFunctionSelectionIdentifier(id,analyzerFactory);
			if(selectionIdentifier.isFunctionDefinition()||selectionIdentifier.isFunctionCall()){
				wantedReferences.add(id);
			}
		}
		return wantedReferences;
	}

}
