package tinyos.yeti.refactoring.rename.globalvariable;

import java.io.IOException;
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
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.text.edits.MultiTextEdit;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;

public class Processor extends RenameProcessor {

	private RenameInfo info;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	/**
	 * Returns all sources which reference the given path mapped by the file in which they rest.
	 * For the sources the contained Identifier is returned.
	 * @param baseDeclaration Must equal the target of the references which we are looking for, to get a match. This means that you should consider passing a logical path here.
	 * @param monitor
	 * @return
	 * @throws CoreException
	 * @throws MissingNatureException
	 * @throws IOException
	 */
	private Map<IFile, Collection<Identifier>> getReferences(IASTModelPath baseDeclaration,IProgressMonitor monitor) 
	throws CoreException, MissingNatureException, IOException {
		Map<IFile, Collection<Identifier>> result=new HashMap<IFile,Collection<Identifier>>();
		Collection<IFile> files=getAllFiles();
		for(IFile file:files){
			Collection<Identifier> identifiers=new LinkedList<Identifier>();
			result.put(file, identifiers);
			NesC12AST ast=getAst(file,monitor);
			ASTUtil util=new ASTUtil(ast);
			IASTReference[] references=getReferences(file, monitor);
			for(IASTReference ref:references){
				if(baseDeclaration.equals(getLogicalPath(ref.getTarget(),monitor))){
					IFileRegion region=ref.getSource();
					int pos=region.getOffset();
					pos+=region.getLength()/2;
					Identifier id=(Identifier)util.getASTLeafAtPos(pos);
					identifiers.add(id);
				}
			}
		}
		return result;
	}

	@Override
	public Change createChange(IProgressMonitor monitor) 
	throws CoreException,OperationCanceledException {

		CompositeChange ret;
		try {
			addOutput("start ");
			
			//Find the basedeclaration which we can use to compare with the target of references.
			Identifier selectedVariable = getSelectedIdentifier();
			IASTModelPath baseDeclaration;
			if(ASTUtil4Variables.isVariableDeclaration(selectedVariable)){
				InitDeclarator initDeclaration=(InitDeclarator)selectedVariable.getParent().getParent();
				baseDeclaration=initDeclaration.resolveField().getPath();
			}else{
				IdentifierExpression identifierExpr=(IdentifierExpression)selectedVariable.getParent();
				baseDeclaration=identifierExpr.resolveField().getPath();
			}
			baseDeclaration=super.eagerResolveLogicalPath(baseDeclaration, monitor);
			addOutput("Base Declaration: "+baseDeclaration);
			
			//Get all Identifiers of sources which reference the baseDeclaration
			Map<IFile,Collection<Identifier>> result=getReferences(baseDeclaration,monitor);
			
			//Get the Identifier for the baseDeclaration and add it to the results
			Identifier baseIdentifier=getIdentifierForPath(baseDeclaration, monitor);
			IFile baseFile=getIFile4ParseFile(baseDeclaration.getParseFile());
			result.get(baseFile).add(baseIdentifier);
		
			//Generate the changes
			ret = new CompositeChange("Rename Global Variable "+ info.getOldName() + " to " + info.getNewName());
			for(IFile file:result.keySet()){
				NesC12AST ast=getAst(file, monitor);
				Collection<Identifier> identifiers=result.get(file);
				if(identifiers.size()>0){
					MultiTextEdit multiTextEdit=new MultiTextEdit();
					addChanges4Identifiers(identifiers, info.getNewName(), multiTextEdit, ast);
					String changeName = "Replacing Variable " + info.getOldName()+ " with " + info.getNewName() + " in Document " + file;
					TextChange textChange = new TextFileChange(changeName,file);
					textChange.setEdit(multiTextEdit);
					ret.add(textChange);
				}

			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(super.endOutput);
			return new NullChange();
		}
		
		System.err.println(super.endOutput);
		return ret;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws CoreException,
			OperationCanceledException {
		RefactoringStatus ret = new RefactoringStatus();
		if (!isApplicable()) {
			ret.addFatalError("The Refactoring is no Accessable");
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
		if (!ASTUtil4Variables.isGlobalVariable(getSelectedIdentifier())) {
			ret.addFatalError("No global variable selected.");
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
		return "tinyos.yeti.refactoring.renameGlobalVariable.Processor";
	}

	@Override
	public String getProcessorName() {
		return info.getInputPageName();
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return super.isApplicable();
	}

	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,
			SharableParticipants sharedParticipants) throws CoreException {
		// TODO Auto-generated method stub
		return new RefactoringParticipant[] {};
	}

}
