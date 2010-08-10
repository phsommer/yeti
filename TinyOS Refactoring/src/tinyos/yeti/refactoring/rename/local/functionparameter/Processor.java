package tinyos.yeti.refactoring.rename.local.functionparameter;

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

import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.refactoring.Refactoring;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.rename.NesCComponentNameCollissionDetector;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class Processor extends RenameProcessor {
	
	private ASTUtil astUtil=new ASTUtil();
	private ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables(astUtil);

	private RenameInfo info;

	private IFile containingFile;
	private Identifier declaringIdentifier;
	private CompoundStatement declaringCompound;
	private Collection<Identifier> affectedIdentifiers;

	public Processor(RenameInfo info) {
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
		declaringIdentifier=astUtil4Variables.getFunctionParameterIfInside(currentlySelected.getName(),currentlySelected);
		if(declaringIdentifier==null){
			ret.addFatalError("Couldnt find the identifier which declares the selected parameter.");
			return ret;
		}
		 FunctionDefinition definition= astUtil.getParentForName(currentlySelected, FunctionDefinition.class);
		 declaringCompound=definition.getBody();
		if(declaringCompound==null){
			ret.addFatalError("Couldnt find the declaration which declares the selected variable.");
			return ret;
		}
		affectedIdentifiers=new LinkedList<Identifier>();
		affectedIdentifiers.add(declaringIdentifier);
		Collection<Identifier> identifiers =astUtil4Variables.getAllIdentifiersWithoutOwnDeclaration(declaringCompound, currentlySelected.getName());
		affectedIdentifiers.addAll(identifiers);	//We prefer to have the function parameter identifier on the top.

		return ret;
		
	}
	
	@Override
	protected RefactoringStatus checkConditionsAfterNameSetting(IProgressMonitor pm) {
		RefactoringStatus ret=new RefactoringStatus();
		try {
			NesC12AST ast= getAst(containingFile, pm);
			NesCComponentNameCollissionDetector detector=new NesCComponentNameCollissionDetector();
			//Check if there is already a parameter in the function declaration with the same name.
			Identifier sameNameParameter=astUtil4Variables.getFunctionParameterIfInside(info.getNewName(),declaringCompound);
			if(sameNameParameter!=null){
				ASTPositioning positioning=new ASTPositioning(ast);
				Region toRenameRegion= new Region(positioning.start(declaringIdentifier),declaringIdentifier.getName().length());
				Region sameNameRegion= new Region(positioning.start(sameNameParameter),sameNameParameter.getName().length());
				ret.addError("You intendet to rename the parameter "+info.getOldName()+" to "+info.getNewName()+".", new FileStatusContext(containingFile, toRenameRegion));
				ret.addError("There is already a parameter with this name: "+info.getNewName(),  new FileStatusContext(containingFile, sameNameRegion));
			}else{
				//Check if there is already a parameter in the function body with the same name.
				detector.handleCollisions4Scope(info.getOldName(),info.getNewName(), declaringIdentifier,containingFile, ast,declaringCompound, containingFile,ast, ret);
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
