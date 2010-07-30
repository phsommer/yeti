package tinyos.yeti.refactoring.rename.global.field;


import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.rename.global.FieldInfo;
import tinyos.yeti.refactoring.rename.global.FieldInfoSet;
import tinyos.yeti.refactoring.rename.global.FieldKind;
import tinyos.yeti.refactoring.rename.global.GlobalFieldFinder;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;

public class GlobalFieldRenameProcessor extends RenameProcessor {

	private RenameInfo info;

	public GlobalFieldRenameProcessor(RenameInfo info) {
		super(info);
		this.info = info;
	}

	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		CompositeChange ret = new CompositeChange("Rename Field "+ info.getOldName() + " to " + info.getNewName());
		try {
			
			//Get the User selection
			Identifier selectedIdentifier = getSelectedIdentifier();
			//Gather general information about the field.
			GlobalFieldFinder finder=new GlobalFieldFinder(info.getEditor(), pm);
			FieldInfoSet fieldInfoSet=finder.getFieldInfoSetAbout(selectedIdentifier.getName());
			Map<IFile, Collection<FieldInfo> > files2FieldInfos=fieldInfoSet.getFiles2FieldInfos();
			
			//Gather all identifiers of the field on a per file base.
			for(IFile file:files2FieldInfos.keySet()){
				NesC12AST ast=getAst(file, pm);
				ASTPositioning astPositioning=new ASTPositioning(ast);
				Collection<Identifier> identifiers=new LinkedList<Identifier>();
				Collection<FieldInfo> fieldInfos=files2FieldInfos.get(file);
				
				//Get the Identifiers of declarations and, if this field is a function, the identifiers of definitions in the file. 
				for(FieldInfo fieldInfo:fieldInfos){
					if(fieldInfo.getKind()!=FieldKind.INCLUDED_DECLARATION){	//Included Declarations don't have to be changed.
						RangeDescription description=fieldInfo.getField().getRange();
						Identifier id=(Identifier)astPositioning.getASTLeafAtAstPos(description.getLeft());
						identifiers.add(id);
					}
				}
				
				//Get the Identifiers of References in the file. 
				Collection<IASTModelPath> paths=fieldInfoSet.getKnownPathsForFile(file);
				Collection<Identifier> referencesOfFile=getReferencingIdentifiersInFileForTargetPaths(file,paths,pm);
				identifiers.addAll(referencesOfFile);
				
				//add the changes for the found identifiers.
				String textChangeName = createTextChangeName("global field", file);
				addMultiTextEdit(identifiers,ast,file,textChangeName,ret);
			}
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return ret;
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
		ASTUtil4Functions astUtil4Functions=new ASTUtil4Functions();
		ASTUtil4Variables astUtil4Variables=new ASTUtil4Variables();
		if (!(astUtil4Functions.isGlobalFunction(selectedIdentifier)||astUtil4Variables.isGlobalVariable(selectedIdentifier))) {
			ret.addFatalError("No Global Field selected.");
		}
		return ret;
	}

}
