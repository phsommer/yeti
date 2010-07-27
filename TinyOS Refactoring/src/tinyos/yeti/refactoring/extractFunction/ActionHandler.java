package tinyos.yeti.refactoring.extractFunction;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.refactoring.DefaultRefactoringWizard;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class ActionHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		NesCEditor editor = ActionHandlerUtil.getNesCEditor(event);
		
		Info info = new Info(editor);
		Processor processor = new Processor(info);
		ProcessorBasedRefactoring refactoring = new ProcessorBasedRefactoring(processor);
		DefaultRefactoringWizard wizard = new DefaultRefactoringWizard(
				refactoring, 
				new InputPage(info), 
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

}
