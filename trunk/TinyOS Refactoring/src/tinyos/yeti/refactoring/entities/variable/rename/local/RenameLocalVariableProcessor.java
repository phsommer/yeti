package tinyos.yeti.refactoring.entities.variable.rename.local;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.refactoring.Refactoring;
import tinyos.yeti.refactoring.rename.NesCComponentNameCollissionDetector;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class RenameLocalVariableProcessor extends RenameProcessor {
	
	private ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables();

	private RenameInfo info;

	private IFile containingFile;
	private CompoundStatement declaringCompound;
	private Identifier declaringIdentifier;
	private Collection<Identifier> affectedIdentifiers;

	public RenameLocalVariableProcessor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		Identifier currentlySelected = getSelectedIdentifier();
		containingFile=ActionHandlerUtil.getInputFile(info.getEditor());
		if(containingFile==null){
			ret.addFatalError("Couldnt find the file which contains the selection.");
			return ret;
		}
		declaringCompound = astUtil4Variables.findDeclaringCompoundStatement(currentlySelected);
		if(declaringCompound==null){
			ret.addFatalError("Couldnt find the declaration which declares the selected variable.");
			return ret;
		}
		declaringIdentifier=astUtil4Variables.getLocalVariableDeclarationIfInside(info.getOldName(), declaringCompound);
		if(declaringIdentifier==null){
			ret.addFatalError("Couldnt find the identifier of the declaration which declares the selected variable.");
			return ret;
		}
		affectedIdentifiers =astUtil4Variables.getAllIdentifiersWithoutOwnDeclaration(declaringCompound, currentlySelected.getName());

		return ret;
		
	}
	
	@Override
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm) {
		RefactoringStatus ret=new RefactoringStatus();
		try {
			NesC12AST ast= getAst(containingFile, pm);
			NesCComponentNameCollissionDetector detector=new NesCComponentNameCollissionDetector();
			detector.handleCollisions4Scope(info.getOldName(),info.getNewName(), declaringIdentifier,containingFile, ast,declaringCompound, containingFile,ast, ret);
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
		Map<IFile,Collection<Identifier>> map=new HashMap<IFile, Collection<Identifier>>();
		map.put(containingFile, affectedIdentifiers);
		try {
			super.addChanges(map, ret, pm);
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
