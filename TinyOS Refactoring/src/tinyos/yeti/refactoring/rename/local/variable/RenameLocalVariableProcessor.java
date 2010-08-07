package tinyos.yeti.refactoring.rename.local.variable;

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

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class RenameLocalVariableProcessor extends RenameProcessor {
	
	private ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables();

	private RenameInfo info;

	private IFile containingFile;
	private CompoundStatement declaringCompound;
	private Collection<Identifier> affectedIdentifiers;

	public RenameLocalVariableProcessor(RenameInfo info) {
		super(info);
		this.info = info;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		
		CompositeChange ret = new CompositeChange("Rename Local Variable "+ info.getOldName() + " to " + info.getNewName());
		Map<IFile,Collection<Identifier>> map=new HashMap<IFile, Collection<Identifier>>();
		map.put(containingFile, affectedIdentifiers);
		try {
			super.addChanges("local variable", map, ret, pm);
		} catch (Exception e){
			ret.add(new NullChange("Exception occured during change creation. See project log for more information."));
			getProjectUtil().log("Exception occured during change creation.",e);
		}
		return ret;
	}

	@Override
	protected RefactoringStatus initializeRefactoring(IProgressMonitor pm){
		RefactoringStatus ret=new RefactoringStatus();
		// Find currently selected Element
		Identifier currentlySelected = getSelectedIdentifier();

		// Find the CompoundStatement which declares the identifier
		declaringCompound = astUtil4Variables.findDeclaringCompoundStatement(currentlySelected);
		affectedIdentifiers =astUtil4Variables.getAllIdentifiersWithoutOwnDeclaration(declaringCompound, currentlySelected.getName());
		containingFile=ActionHandlerUtil.getInputFile(info.getEditor());
		if(containingFile==null){
			ret.addFatalError("Couldnt find the file which contains the selection.");
		}

		return ret;
		
	}

}
