package tinyos.yeti.refactoring.renameFunction;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ASTUtil;
import tinyos.yeti.refactoring.ActionHandlerUtil;
import tinyos.yeti.refactoring.DefaultRefactoringWizard;

public class ActionHandler extends AbstractHandler implements IHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		NesCEditor editor = ActionHandlerUtil.getNesCEditor(event);
		ITextSelection selection = ActionHandlerUtil.getSelection(editor);
		ASTUtil util = new ASTUtil((NesC12AST) editor.getAST());
		Identifier id=util.getASTLeafAtPos(selection.getOffset(), Identifier.class);
		if(id==null){
			return null;
		}
		System.err.println("Found Identifier!");
		return null;
//		String oldName =id.getName();
//
//		
//		Info info = new Info(oldName,editor);
//		Processor processor = new Processor(info);
//		RenameRefactoring refactoring = new RenameRefactoring(processor);
//		DefaultRefactoringWizard wizard = new DefaultRefactoringWizard(
//				refactoring, 
//				new InputPage(info), 
//				info);
//		RefactoringWizardOpenOperation wizardStarter = new RefactoringWizardOpenOperation(wizard);
//
//		try {
//			wizardStarter.run(editor.getSite().getShell(),
//					info.getInputWizardName());
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		/* Return is reserved for Future use. Must be null. Really!! */
//		return null;
	}

}
