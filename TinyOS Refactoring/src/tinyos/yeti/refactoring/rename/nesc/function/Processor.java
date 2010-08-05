package tinyos.yeti.refactoring.rename.nesc.function;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.InterfaceAstAnalyzer;
import tinyos.yeti.refactoring.ast.ModuleAstAnalyzer;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.selection.InterfaceSelectionIdentifier;
import tinyos.yeti.refactoring.selection.NescFunctionSelectionIdentifier;
import tinyos.yeti.refactoring.utilities.ASTUtil;
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
			initializationStatus.addFatalError("Exception occured!");
		}
		return initializationStatus;
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		CompositeChange ret = new CompositeChange("Rename Nesc Function "+ info.getOldName() + " to " + info.getNewName());
		Identifier selectedIdentifier=getSelectedIdentifier();
		try{
			//Add change for the function name identifier in the interface definition.
			Identifier definingIdentifier=null;
			for(Identifier id:interfaceDefinitionAnalyzer.getNesCFunctionIdentifiers()){
				if(id.equals(selectedIdentifier)){
					definingIdentifier=id;
				}
			}
			if(definingIdentifier==null){
				//Function Declaration not found!
				return new NullChange();
			}
			List<Identifier> identifiers=new LinkedList<Identifier>();
			identifiers.add(definingIdentifier);
			addMultiTextEdit(identifiers, getAst(declaringFile, pm), declaringFile, createTextChangeName("nesc function", declaringFile), ret);
			
			//Add Changes for function name identifiers in function definitions.
			Collection<IASTModelPath> paths=new LinkedList<IASTModelPath>();
			paths.add(definingInterfaceDeclaration.getPath());
			for(IFile file:getAllFiles()){
				identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
				if(identifiers.size()>0){
					identifiers=figureOutWishedFunctionNamesFromInterfaceReferences(selectedIdentifier.getName(),identifiers);
					if(identifiers.size()>0){
						addMultiTextEdit(identifiers, getAst(file, pm), file, createTextChangeName("nesc function", file), ret);
					}
				}
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}finally{
			DebugUtil.printOutput();
		}
		return ret;
	}

	/**
	 * Tries to find for every identifier in identifiers, which are expected to be references to an interface definition,
	 * the associated function identifier.
	 * Throws away identifiers, which are not part of a function definition in a NesC module or function name identifiers which do not match functionName. 
	 * @param functionName
	 * @param identifiers	This identifiers are supposed to be in the same AST.
	 * @return
	 */
	private List<Identifier> figureOutWishedFunctionNamesFromInterfaceReferences(String functionName, List<Identifier> identifiers) {
		if(identifiers.size()==0){
			return Collections.emptyList();
		}
		AstAnalyzerFactory analyzerFactory=new AstAnalyzerFactory(identifiers.get(0));
		if(!analyzerFactory.hasModuleAnalyzerCreated()){
			return Collections.emptyList();
		}
		Set<Identifier> differentInterfaceIntstances=new HashSet<Identifier>();	//We add for every interface instance just one name. 
		for(Identifier interfaceRef:identifiers){	//Every interface instance in a module does at most once implement a function of a specific interface. Therefore we need to check every local name just once.
			InterfaceSelectionIdentifier selectionIdentifier=new InterfaceSelectionIdentifier(interfaceRef,analyzerFactory);
			if(selectionIdentifier.isInterfaceImplementation()){	//We add just references which are interface names in a function implementation. This references are local names for the interface, which means they can be aliases as well as the real interface name.
				differentInterfaceIntstances.add(interfaceRef);	
			}
		}
		List<Identifier> functionNames=new LinkedList<Identifier>();
		for(Identifier interfaceRef:differentInterfaceIntstances){
			ModuleAstAnalyzer analyzer=analyzerFactory.getModuleAnalyzer();
			Map<Identifier,Collection<Identifier>> interface2Functions=analyzer.getLocalInterfaceName2AssociatedFunctionNames();
			Collection<Identifier> functions=interface2Functions.get(interfaceRef);
			ASTUtil astUtil=new ASTUtil();
			Identifier candidate=astUtil.getIdentifierWithEqualName(functionName,functions);
			if(candidate!=null){
				functionNames.add(candidate);
			}
		}
		return functionNames;
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
		return null;
	}

}
