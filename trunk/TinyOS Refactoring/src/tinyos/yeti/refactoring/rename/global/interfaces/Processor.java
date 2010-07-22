package tinyos.yeti.refactoring.rename.global.interfaces;


import java.io.IOException;
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
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.rename.global.FieldInfo;
import tinyos.yeti.refactoring.rename.global.FieldInfoSet;
import tinyos.yeti.refactoring.rename.global.FieldKind;
import tinyos.yeti.refactoring.rename.global.GlobalFieldFinder;
import tinyos.yeti.refactoring.utilities.ASTUTil4Interfaces;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ASTUtil4Functions;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.DebugUtil;

public class Processor extends RenameProcessor {

	private RenameInfo info;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}

	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		DebugUtil.clearOutput();
		CompositeChange ret = new CompositeChange("Rename Interface "+ info.getOldName() + " to " + info.getNewName());
		
		
		try {
			DebugUtil.addOutput("Change Interface called!");
			GlobalFieldFinder finder=new GlobalFieldFinder(info.getEditor(), pm);
			finder.printFieldInformation(super.getSelectedIdentifier().getName());
			
			
			
			
		} catch (Exception e){
			e.printStackTrace();
		}
		DebugUtil.printOutput();
		return ret;
		
		
		
		
		
		
//		try {
//			
//			//Get the User selection
//			Identifier selectedIdentifier = getSelectedIdentifier();
//			//Gather general information about the field.
//			GlobalFieldFinder finder=new GlobalFieldFinder(info.getEditor(), pm);
//			FieldInfoSet fieldInfoSet=finder.getFieldInfoSetAbout(selectedIdentifier.getName());
//			Map<IFile, Collection<FieldInfo> > files2FieldInfos=fieldInfoSet.getFiles2FieldInfos();
//			
//			//Gather all identifiers of the field on a per file base.
//			for(IFile file:files2FieldInfos.keySet()){
//				NesC12AST ast=getAst(file, pm);
//				ASTUtil astUtil=new ASTUtil(ast);
//				Collection<Identifier> identifiers=new LinkedList<Identifier>();
//				Collection<FieldInfo> fieldInfos=files2FieldInfos.get(file);
//				
//				//Get the Identifiers of declarations and, if this field is a function, the identifiers of definitions in the file. 
//				for(FieldInfo fieldInfo:fieldInfos){
//					if(fieldInfo.getKind()!=FieldKind.INCLUDED_DECLARATION){	//Included Declarations don't have to be changed.
//						RangeDescription description=fieldInfo.getField().getRange();
//						Identifier id=(Identifier)astUtil.getASTLeafAtAstPos(description.getLeft());
//						identifiers.add(id);
//					}
//				}
//				
//				//Get the Identifiers of References in the file. 
//				Collection<IASTModelPath> paths=fieldInfoSet.getKnownPathsForFile(file);
//				Collection<Identifier> referencesOfFile=getReferencingIdentifiersInFileForTargetPaths(file,paths,pm);
//				identifiers.addAll(referencesOfFile);
//				
//				//add the changes for the found identifiers.
//				String textChangeName = "Replacing global field name " + info.getOldName()+ " with " + info.getNewName() + " in Document " + file;
//				addMultiTextEdit(identifiers,ast,file,textChangeName,ret);
//			}
//			
//		}
//		catch (Exception e){
//			e.printStackTrace();
//		}
//		return ret;
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
		if (!(ASTUTil4Interfaces.isInterface(selectedIdentifier)||ASTUtil4Variables.isGlobalVariable(selectedIdentifier))) {
			ret.addFatalError("No Global Field selected.");
		}
		return ret;
	}

	@Override
	public Object[] getElements() {
		// TODO Auto-generated method stub
		return new Object[] {};
	}

	@Override
	public String getIdentifier() {
		return "tinyos.yeti.refactoring.renameFunction.Processor";
	}

	@Override
	public String getProcessorName() {
		return info.getInputPageName();
	}

	@Override
	public boolean isApplicable() 
	throws CoreException {
		return super.isApplicable();
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,SharableParticipants sharedParticipants) 
	throws CoreException {
		// TODO Auto-generated method stub
		return new RefactoringParticipant[] {};
	}

}
