package tinyos.yeti.refactoring.rename.local.function;


import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.refactoring.Refactoring;
import tinyos.yeti.refactoring.rename.NesCComponentNameCollissionDetector;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;

public class Processor extends RenameProcessor {
	
	private ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions();
	
	private RenameInfo info;
	
	private IFile definingFile;
	private Collection<Identifier> affectedIdentifiers;
	private Identifier definingIdentifier;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}

	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus ret= new RefactoringStatus();
		try {
			definingFile=(IFile)info.getEditor().getResource();
			Identifier selectedIdentifier = getSelectedIdentifier();
			
			//Get the local Definition and if there is one the declaration.
			affectedIdentifiers=new LinkedList<Identifier>();
			definingIdentifier=astUtil4Functions.getLocalFunctionDefinitionIdentifier(selectedIdentifier);
			affectedIdentifiers.add(definingIdentifier);
			Identifier localDeclarationId=astUtil4Functions.getLocalFunctionDeclarationIdentifier(selectedIdentifier);
			if(localDeclarationId!=null){	//If there is a declartion of the function we add its identifier to be changed.
				affectedIdentifiers.add(localDeclarationId);
			}
			
			//Get the references to the local function.
			FunctionDefinition definition=astUtil4Functions.identifierToFunctionDefinition(definingIdentifier);
			IASTModelPath targetPath=definition.resolveNode().getPath();
			Collection<IASTModelPath> targetPaths=new LinkedList<IASTModelPath>();
			targetPaths.add(targetPath);
			Collection<Identifier> referenceSources=getReferencingIdentifiersInFileForTargetPaths(definingFile, targetPaths, pm);
			affectedIdentifiers.addAll(referenceSources);
		} catch (Exception e){
			ret.addFatalError(("Exception occured during refactoring initialization. See project log for more information."));
			getProjectUtil().log("Exception occured during refactoring initialization.",e);
		}
		return ret;
	}
	
	@Override
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm) {
		RefactoringStatus ret=new RefactoringStatus();
		try {
			NesC12AST ast=getAst(definingFile,pm);
			NesCExternalDefinitionList scope=getAstUtil().getModuleImplementationNodeIfInside(definingIdentifier);
			NesCComponentNameCollissionDetector detector=new NesCComponentNameCollissionDetector();
			detector.handleCollisions4Scope(info.getNewName(),definingIdentifier, scope, definingFile, ast, ret);
		} catch (Exception e){
			ret.addFatalError(("Exception occured during conditions checking. See project log for more information."));
			getProjectUtil().log("Exception occured during conditions checking.",e);
		}
		return ret;
	}
	
	
	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		CompositeChange ret =createNewCompositeChange();
		try {
			Map<IFile,Collection<Identifier>> map=new HashMap<IFile, Collection<Identifier>>(); 
			map.put(definingFile, affectedIdentifiers);
			super.addChanges(map, ret, pm);
			return ret;
			
		}
		catch (Exception e){
			ret.add(new NullChange(("Exception occured during change creation. See project log for more information.")));
			getProjectUtil().log("Exception occured during refactoring initialization.",e);
		}
		return ret;
	}

	@Override
	public String getProcessorName() {
		return Refactoring.RENAME_LOCAL_FUNCTION.getEntityName();
	}

}
