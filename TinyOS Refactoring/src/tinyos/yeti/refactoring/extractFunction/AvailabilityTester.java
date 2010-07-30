package tinyos.yeti.refactoring.extractFunction;



import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.refactoring.AvailabilityTester.IRefactoringAvailabilityTester;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class AvailabilityTester implements IRefactoringAvailabilityTester{

	@Override
	public boolean test(ITextSelection receiver) {
		NesCEditor editor = ActionHandlerUtil.getActiveEditor().getNesCEditor();
		Info info = new Info(editor);
		Processor processor = new Processor(info);

		try {
			return processor.checkInitialConditions(null).isOK();
		} catch (OperationCanceledException e) {
			return false;
		} catch (CoreException e) {
			return false;
		}
	}

}
