package tinyos.yeti.refactoring.rename;

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
import tinyos.yeti.refactoring.DefaultRefactoringWizard;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public abstract class RenameActionHandler extends AbstractHandler implements IHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		NesCEditor editor = ActionHandlerUtil.getNesCEditor(event);
		ITextSelection selection = ActionHandlerUtil.getSelection(editor);
		ASTUtil util = new ASTUtil((NesC12AST) editor.getAST());
		int pos=selection.getOffset();
		pos+=selection.getLength()/2;
		Identifier id=util.getASTLeafAtPos(pos, Identifier.class);
		if(id==null){
			return null;
		}
		String oldName =id.getName();
		RenameInfo info = new RenameInfo(oldName,editor);
		RenameProcessor processor = createProcessor(info);
		RenameRefactoring refactoring = new RenameRefactoring(processor);
		DefaultRefactoringWizard wizard = new DefaultRefactoringWizard(
				refactoring, 
				new RenameInputPage(info), 
				info);
		RefactoringWizardOpenOperation wizardStarter = new RefactoringWizardOpenOperation(wizard);

		try {
			wizardStarter.run(editor.getSite().getShell(),info.getInputWizardName());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* Return is reserved for Future use. Must be null. Really!! */
		return null;
	}

	/**
	 * Must return the Processor which should be used for this action.
	 * @param info
	 * @return
	 */
	protected abstract RenameProcessor createProcessor(RenameInfo info);
	
	


}
