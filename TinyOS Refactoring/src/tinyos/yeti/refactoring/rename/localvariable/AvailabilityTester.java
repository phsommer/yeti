package tinyos.yeti.refactoring.rename.localvariable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.refactoring.rename.RenameInfo;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class AvailabilityTester implements tinyos.yeti.refactoring.AvailabilityTester.IRefactoringAvailabilityTester {

	@Override
	public boolean test(ITextSelection receiver) {
		NesCEditor editor = ActionHandlerUtil.getActiveEditor().getNesCEditor();
		
		RenameInfo info = new RenameInfo("",editor);
		info.setEditor(editor);
		RenameProcessor processor = new RenameLocalVariableProcessor(info);
		try {
			return processor.checkInitialConditions(null).isOK();
		} catch (OperationCanceledException e) {
			e.printStackTrace();
			return false;
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
	}

}
