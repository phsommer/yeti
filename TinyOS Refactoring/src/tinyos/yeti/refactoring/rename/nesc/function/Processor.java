package tinyos.yeti.refactoring.rename.nesc.function;


import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.AstAnalyzerFactory;
import tinyos.yeti.refactoring.ast.InterfaceAstAnalyzer;
import tinyos.yeti.refactoring.ast.ModuleAstAnalyzer;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.rename.RenameProcessor;
import tinyos.yeti.refactoring.selection.NescFunctionSelectionIdentifier;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class Processor extends RenameProcessor {
	
	private RenameInfo info;
	
	private AstAnalyzerFactory factory4Selection;
	private NescFunctionSelectionIdentifier selectionIdentifier;

	public Processor(RenameInfo info) {
		super(info);
		this.info = info;
	}
	
	/**
	 * Returns the name of the interface which declares the selected function.
	 * @return
	 */
	private String getInterfaceDefinitionName(){
		if(selectionIdentifier.isFunctionDeclaration()){
			InterfaceAstAnalyzer analyzer=factory4Selection.getInterfaceAnalyzer();
			return analyzer.getEntityIdentifier().getName();
		}
		else if(selectionIdentifier.isFunctionDefinition()){
			ModuleAstAnalyzer analyzer=factory4Selection.getModuleAnalyzer();
			Identifier localInterfaceName=analyzer.getAssociatedInterfaceName4FunctionIdentifier(selectionIdentifier.getSelection());
			return analyzer.getInterfaceLocalName2InterfaceGlobalName().get(localInterfaceName).getName();
		}
		return null;
	}

	@Override
	public Change createChange(IProgressMonitor pm) 
	throws CoreException,OperationCanceledException {
		CompositeChange ret = new CompositeChange("Rename Nesc Function "+ info.getOldName() + " to " + info.getNewName());
		Identifier selectedIdentifier=getSelectedIdentifier();
		factory4Selection=new AstAnalyzerFactory(selectedIdentifier);
		selectionIdentifier=new NescFunctionSelectionIdentifier(selectedIdentifier,factory4Selection);
		try {
			String definingInterfaceName=getInterfaceDefinitionName();
			ProjectUtil projectUtil=getProjectUtil();
			IDeclaration definingInterfaceDeclaration=projectUtil.getInterfaceDefinition(definingInterfaceName);
			
			//Add Change for interface definition
			IFile declaringFile=getIFile4ParseFile(definingInterfaceDeclaration.getParseFile());
			if(!projectUtil.isProjectFile(declaringFile)){
				markRefactoringAsInfeasible("Defining interface is out of project range!");
				return new NullChange();
			}
			AstAnalyzerFactory factory4definingInterface=new AstAnalyzerFactory(declaringFile, projectUtil, pm);
			if(!factory4definingInterface.hasInterfaceAnalyzerCreated()){
				markRefactoringAsInfeasible("Defining interface is out of project range!");
				return new NullChange();
			}
			InterfaceAstAnalyzer analyzer=factory4definingInterface.getInterfaceAnalyzer();
			Identifier definingIdentifier=null;
			for(Identifier id:analyzer.getNesCFunctionIdentifiers()){
				if(id.equals(selectedIdentifier)){
					definingIdentifier=id;
				}
			}
			if(definingIdentifier==null){
				markRefactoringAsInfeasible("Function Declaration not found!");
				return new NullChange();
			}
			List<Identifier> identifiers=new LinkedList<Identifier>();
			identifiers.add(definingIdentifier);
			addMultiTextEdit(identifiers, getAst(declaringFile, pm), declaringFile, createTextChangeName("nesc function", declaringFile), ret);
			
			//Add Changes for referencing elements
			Collection<IASTModelPath> paths=new LinkedList<IASTModelPath>();
			paths.add(definingInterfaceDeclaration.getPath());
			for(IFile file:getAllFiles()){
				identifiers=getReferencingIdentifiersInFileForTargetPaths(file, paths, pm);
				identifiers=throwAwayDifferentNames(identifiers, definingIdentifier.getName());
				addMultiTextEdit(identifiers, getAst(file, pm), file, createTextChangeName("nesc function", file), ret);
			}
			
			
		} catch (Exception e){
			e.printStackTrace();
		}
		return ret;
	}

}
