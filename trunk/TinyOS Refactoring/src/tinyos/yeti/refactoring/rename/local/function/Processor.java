package tinyos.yeti.refactoring.rename.local.function;


import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;

public class Processor extends RenameProcessor {

	private ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions();
	
	private RenameInfo info;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}

	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		CompositeChange ret = new CompositeChange("Rename local function "+ info.getOldName() + " to " + info.getNewName());
		try {
			
			//Get the User selection
			Identifier selectedIdentifier = getSelectedIdentifier();
			
			//Get the local Definition and if there is one the declaration.
			Collection<Identifier> identifiers=new LinkedList<Identifier>();
			Identifier localDefinitionId=astUtil4Functions.getLocalFunctionDefinitionIdentifier(selectedIdentifier);
			if(localDefinitionId==null){	//If this happens this means, that the selected identifier is not part of a local function. This case should never happen because of earlier checks.
				return new NullChange();
			}
			identifiers.add(localDefinitionId);
			Identifier localDeclarationId=astUtil4Functions.getLocalFunctionDeclarationIdentifier(selectedIdentifier);
			if(localDeclarationId!=null){	//If there is a declartion of the function we add its identifier to be changed.
				identifiers.add(localDeclarationId);
			}
			
			//Get the references to the local function.
			FunctionDefinition definition=astUtil4Functions.identifierToFunctionDefinition(localDefinitionId);
			IASTModelPath targetPath=definition.resolveNode().getPath();
			Collection<IASTModelPath> targetPaths=new LinkedList<IASTModelPath>();
			targetPaths.add(targetPath);
			IFile file=(IFile)info.getEditor().getResource();
			Collection<Identifier> referenceSources=getReferencingIdentifiersInFileForTargetPaths(file, targetPaths, pm);
			identifiers.addAll(referenceSources);
			
			//Create the changes for the identifiers
			String textChangeName = "Replacing local function name " + info.getOldName()+ " with " + info.getNewName() + " in Document " + file;
			addMultiTextEdit(identifiers, super.getAst(file,pm), file, textChangeName,ret);
			
			return ret;
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
	throws CoreException, OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if (!isApplicable()) {
			ret.addFatalError("The Refactoring is no Accessable");
		}
		Identifier selectedIdentifier=getSelectedIdentifier();
		if (!(astUtil4Functions.isLocalFunction(selectedIdentifier))) {
			ret.addFatalError("No Local Function selected.");
		}
		return ret;
	}

}
