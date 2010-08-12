package tinyos.yeti.refactoring.rename.local.functionparameter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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

import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.Refactoring;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.rename.NesCComponentNameCollissionDetector;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.rename.global.FieldInfo;
import tinyos.yeti.refactoring.rename.global.FieldInfoSet;
import tinyos.yeti.refactoring.rename.global.FieldKind;
import tinyos.yeti.refactoring.rename.global.GlobalFieldFinder;
import tinyos.yeti.refactoring.selection.FunctionSelectionIdentifier;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class Processor extends RenameProcessor {
	
	private ASTUtil astUtil=new ASTUtil();
	private ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables(astUtil);
	private ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions(astUtil);
	
	private Map<IFile,FunctionDefinition> file2definitions=new HashMap<IFile,FunctionDefinition>();
	private Map<IFile,FunctionDeclarator> file2declarations=new HashMap<IFile,FunctionDeclarator>();
	
	Map<IFile,Collection<Identifier>> file2affectedIdentifiers=new HashMap<IFile,Collection<Identifier>>();
	
	private String oldName;
	private Integer toRenameIndex;

	private RenameInfo info;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}

	/**
	 * If this is a local function handling is much more easier then for global functions.
	 * @param ret
	 * @param functionIdentifier
	 */
	private void findAffectedDeclarationsAndDefinitionsIfLocal(
			RefactoringStatus ret, Identifier functionIdentifier) {
		IFile containingFile=ActionHandlerUtil.getInputFile(info.getEditor());
		if(containingFile==null){
			ret.addFatalError("Couldnt find the file which contains the selection.");
			return;
		}
		Identifier definingIdentifier=astUtil4Functions.getLocalFunctionDefinitionIdentifier(functionIdentifier);
		file2definitions.put(containingFile, astUtil.getParentForName(definingIdentifier,FunctionDefinition.class));
		Identifier localDeclarationId=astUtil4Functions.getLocalFunctionDeclarationIdentifier(functionIdentifier);
		if(localDeclarationId!=null){
			file2declarations.put(containingFile, astUtil.getParentForName(localDeclarationId,FunctionDeclarator.class));
		}
	}

	/**
	 * If we are going to rename a parameter of a global function look for all declarations and definitions. 
	 * @param pm
	 * @param ret
	 * @param functionIdentifier
	 * @throws CoreException
	 * @throws IOException
	 * @throws MissingNatureException
	 */
	private void findAffectedDeclarationsAndDefinitionsIfGlobal(
			IProgressMonitor pm, RefactoringStatus ret,
			Identifier functionIdentifier) throws CoreException, IOException,
			MissingNatureException {
		GlobalFieldFinder finder=new GlobalFieldFinder(info.getEditor(), pm);
		FieldInfoSet infoSet=finder.getFieldInfoSetAbout(functionIdentifier.getName());
		if(infoSet.isEmpty()){
			ret.addFatalError("Couldn't find function declaratioins");
			return;
		}
		Map<IFile,Collection<FieldInfo>> file2FieldInfo=infoSet.getFiles2FieldInfos();
		for(IFile file:file2FieldInfo.keySet()){
			NesC12AST ast= getAst(file, pm);
			ASTPositioning positioning=new ASTPositioning(ast);
			for(FieldInfo info:file2FieldInfo.get(file)){
				if(info.getKind()!=FieldKind.INCLUDED_DECLARATION){
					Identifier identifier=getIdentifier4FieldInfo(info, positioning);
					FunctionDefinition definition=astUtil.getParentForName(identifier, FunctionDefinition.class);
					if(definition!=null){
						file2definitions.put(file,definition);
					}else{
						FunctionDeclarator declarator=astUtil.getParentForName(identifier, FunctionDeclarator.class);
						file2declarations.put(file,declarator);
					}
				}
				
			}
		}
	}
	
	/**
	 * Collects the identifiers in the function declarations affected by renaming the given parameter.
	 * @param parameterName
	 */
	private void collectAffectedIdentifiersInFunctionDeclarations(){
		Collection<IFile> toRemove=new LinkedList<IFile>();
		for(IFile file:file2declarations.keySet()){
			FunctionDeclarator declarator=file2declarations.get(file);
			Identifier idOfParamList=astUtil4Functions.getIdentifierOfParameterWithIndex(toRenameIndex, declarator);
			if(oldName.equals(idOfParamList.getName())){	//We dont modify parameters whichs name is different from the selected.
				Collection<Identifier> identifiers=file2affectedIdentifiers.get(file);
				if(identifiers==null){
					identifiers=new LinkedList<Identifier>();
					file2affectedIdentifiers.put(file, identifiers);
				}
				identifiers.add(idOfParamList);
			}else{
				toRemove.add(file);
			}
		}
		for(IFile file:toRemove){
			file2declarations.keySet().remove(file);
		}
	}
	
	/**
	 * Gets all identifiers out of the function definitions which are affected by the renaming.
	 * @param parameterName
	 * @param definition
	 * @return
	 */
	private void collectAffectedIdentifiers4FunctionDefinitions(){
		Collection<IFile> toRemove=new LinkedList<IFile>();
		for(IFile file:file2definitions.keySet()){
			FunctionDefinition definition=file2definitions.get(file);
			FunctionDeclarator declarator=astUtil4Functions.getFunctionDeclarator(definition);
			Identifier idOfParamList=astUtil4Functions.getIdentifierOfParameterWithIndex(toRenameIndex, declarator);
			if(oldName.equals(idOfParamList.getName())){	//We don't modify parameters which's name is different from the selected.
				Collection<Identifier> hits=astUtil4Variables.getAllIdentifiersWithoutOwnDeclaration(definition.getField(FunctionDefinition.BODY),idOfParamList.getName());
				Collection<Identifier> identifiers=file2affectedIdentifiers.get(file);
				if(identifiers==null){
					identifiers=new LinkedList<Identifier>();
					file2affectedIdentifiers.put(file, identifiers);
				}
				identifiers.add(idOfParamList);
				identifiers.addAll(hits);
			}else{
				toRemove.add(file);
			}
		}
		for(IFile file:toRemove){
			file2definitions.keySet().remove(file);
		}
	}
	
	/**
	 * Returns the FunctionDeclarator to which the selected identifier belongs.
	 * @return
	 */
	private FunctionDeclarator getFunctionDeclarator(){
		Identifier selecedIdentifier = getSelectedIdentifier();
		FunctionDeclarator declarator=null;
		if(astUtil4Variables.isInFunctionDeclarationParameterList(selecedIdentifier)||astUtil4Variables.isInFunctionDefinitionParameterList(selecedIdentifier)){
			declarator=astUtil.getParentForName(selecedIdentifier, FunctionDeclarator.class);
		}else{	//We have to be in the function body.
			FunctionDefinition definition=astUtil.getParentForName(selecedIdentifier, FunctionDefinition.class);
			declarator=astUtil4Functions.getFunctionDeclarator(definition);
		}
		return declarator;
		
	}

	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		FunctionDeclarator functionDeclarator=getFunctionDeclarator();
		oldName=info.getOldName();
		toRenameIndex=astUtil4Functions.getIndexOfParameterWithName(oldName, functionDeclarator);
		if(toRenameIndex==null){
			ret.addFatalError("Couldn't locate parameter in parameter list.");
		}
		Identifier functionIdentifier=astUtil4Functions.getIdentifierOfFunctionDeclaration(functionDeclarator);
		try{
			AstAnalyzerFactory factory4FunctionIdentifier=new AstAnalyzerFactory(functionIdentifier);
			FunctionSelectionIdentifier globalFunctionSelectionIdentifier=new FunctionSelectionIdentifier(functionIdentifier,factory4FunctionIdentifier);
			if(globalFunctionSelectionIdentifier.isGlobalFunction()){
				findAffectedDeclarationsAndDefinitionsIfGlobal(pm, ret,functionIdentifier);
			}
			else{
				if(globalFunctionSelectionIdentifier.isImplementationLocalFunction()){
					findAffectedDeclarationsAndDefinitionsIfLocal(ret,functionIdentifier);
				}
			}
			collectAffectedIdentifiersInFunctionDeclarations();
			collectAffectedIdentifiers4FunctionDefinitions();

		}catch(Exception e){
			ret.addFatalError(("Exception occured during refactoring initialization. See project log for more information."));
			getProjectUtil().log("Exception occured during refactoring initialization.",e);
		}
		
		return ret;
	}
	
	@Override
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm) {
		RefactoringStatus ret=new RefactoringStatus();
		NesCComponentNameCollissionDetector detector=new NesCComponentNameCollissionDetector();
		String newName=info.getNewName();
		String oldName=info.getOldName();
		try {
			//Check conflicts for function declarations.
			for(IFile file:file2declarations.keySet()){
				NesC12AST ast= getAst(file, pm);
				FunctionDeclarator declarator=file2declarations.get(file);
				Identifier toRename=astUtil4Functions.getIdentifierOfParameterWithIndex(toRenameIndex, declarator);
				Integer sameNameIndex=astUtil4Functions.getIndexOfParameterWithName(newName, declarator);
				if(sameNameIndex!=null){	//Check if there is already a parameter with the same name.
					ASTPositioning positioning=new ASTPositioning(ast);
					Identifier sameNameParameter=astUtil4Functions.getIdentifierOfParameterWithIndex(sameNameIndex, declarator);
					Region toRenameRegion= new Region(positioning.start(toRename),toRename.getName().length());
					Region sameNameRegion= new Region(positioning.start(sameNameParameter),sameNameParameter.getName().length());
					ret.addError("You intendet to rename the parameter "+oldName+" to "+newName+".", new FileStatusContext(file, toRenameRegion));
					ret.addError("There is already a parameter with this name: "+newName,  new FileStatusContext(file, sameNameRegion));
				}
			}
			//Check conflicts for function definitions.
			for(IFile file:file2definitions.keySet()){
				NesC12AST ast= getAst(file, pm);
				FunctionDefinition definition=file2definitions.get(file);
				FunctionDeclarator declarator=astUtil4Functions.getFunctionDeclarator(definition);
				Identifier toRename=astUtil4Functions.getIdentifierOfParameterWithIndex(toRenameIndex, declarator);
				Integer sameNameIndex=astUtil4Functions.getIndexOfParameterWithName(newName, declarator);
				if(sameNameIndex!=null){	//Check if there is already a parameter with the same name.
					ASTPositioning positioning=new ASTPositioning(ast);
					Identifier sameNameParameter=astUtil4Functions.getIdentifierOfParameterWithIndex(sameNameIndex, declarator);
					Region toRenameRegion= new Region(positioning.start(toRename),toRename.getName().length());
					Region sameNameRegion= new Region(positioning.start(sameNameParameter),sameNameParameter.getName().length());
					ret.addError("You intendet to rename the parameter "+oldName+" to "+newName+".", new FileStatusContext(file, toRenameRegion));
					ret.addError("There is already a parameter with this name: "+newName,  new FileStatusContext(file, sameNameRegion));
				}else{
					//Check if there is already a parameter in the function body with the same name.
					detector.handleCollisions4Scope(oldName,newName, toRename,file, ast,definition.getField(FunctionDefinition.BODY), file,ast, ret);
				}
			}
		} catch (Exception e){
			ret.addFatalError(("Exception occured during conditions checking. See project log for more information."));
			getProjectUtil().log("Exception occured during conditions checking.",e);
		}
		return ret;
		
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		
		CompositeChange ret = createNewCompositeChange();
		try {
			super.addChanges(file2affectedIdentifiers, ret, pm);
		} catch (Exception e){
			ret.add(new NullChange("Exception occured during change creation. See project log for more information."));
			getProjectUtil().log("Exception occured during change creation.",e);
		}
		return ret;
	}

	@Override
	public String getProcessorName() {
		return Refactoring.RENAME_LOCAL_VARIABLE.getEntityName();
	}

}
