package tinyos.yeti.refactoring.renameLocalVariable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;

import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.refactoring.RefactoringPlugin;

public class AvailabilityTester implements tinyos.yeti.refactoring.AvailabilityTester.IRefactoringAvailabilityTester {

	@Override
	public boolean test(ITextSelection receiver) {
		IEditorPart editor_object = RefactoringPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		NesCEditor editor;
		if(editor_object instanceof MultiPageNesCEditor){
			editor = ((MultiPageNesCEditor) editor_object).getNesCEditor();
		} else {
			System.err.println("editor ist keine MultiPageNesCEditor, sondern: "+editor_object.getClass().getCanonicalName());
			return false;
		}
		
		RenameLocalVariableInfo info = new RenameLocalVariableInfo("");
		info.setEditor(editor);
		RenameLocalVariableProcessor processor = new RenameLocalVariableProcessor(info);
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
