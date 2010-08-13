package tinyos.yeti.refactoring.rename.variable.implementation;


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
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.refactoring.Refactoring;
import tinyos.yeti.refactoring.rename.NesCComponentNameCollissionDetector;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;

public class Processor extends RenameProcessor {
	
	private RenameInfo info;

	private ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables();
	
	private IFile declaringFile;
	private Collection<Identifier> affectedIdentifiers;
	private Identifier definingIdentifier;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	@Override
	public String getProcessorName() {
		return Refactoring.RENAME_IMPLEMENTATION_LOCAL_VARIABLE.getEntityName();
	}
	
	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus ret= new RefactoringStatus();
		try {
			declaringFile=(IFile)info.getEditor().getResource();
			Identifier selectedIdentifier = getSelectedIdentifier();
			
			//Get the local declaration.
			definingIdentifier=astUtil4Variables.getImplementationLocalVariableDeclarationIdentifier(selectedIdentifier);
			
			//Get the references to the local function.
			InitDeclarator initDeclarator=astUtil4Variables.identifierToInitDeclarator(definingIdentifier);
			IASTModelPath targetPath=initDeclarator.resolveField().getPath();
			Collection<IASTModelPath> targetPaths=new LinkedList<IASTModelPath>();
			targetPaths.add(targetPath);
			affectedIdentifiers=getReferencingIdentifiersInFileForTargetPaths(declaringFile, targetPaths, pm);
			affectedIdentifiers.add(definingIdentifier);
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
			NesC12AST ast=getAst(declaringFile,pm);
			NesCExternalDefinitionList scope=getAstUtil().getModuleImplementationNodeIfInside(definingIdentifier);
			NesCComponentNameCollissionDetector detector=new NesCComponentNameCollissionDetector();
			detector.handleCollisions4Scope(info.getOldName(),info.getNewName(),definingIdentifier,declaringFile,ast, scope, declaringFile, ast, ret);
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
			map.put(declaringFile, affectedIdentifiers);
			super.addChanges(map, ret, pm);
		}
		catch (Exception e){
			ret.add(new NullChange("Exception occured during change creation. See project log for more information."));
			getProjectUtil().log("Exception occured during refactoring change creation.",e);
		}
		return ret;
	}

}
