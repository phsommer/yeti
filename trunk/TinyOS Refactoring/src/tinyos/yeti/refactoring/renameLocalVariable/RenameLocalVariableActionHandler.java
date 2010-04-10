package tinyos.yeti.refactoring.renameLocalVariable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;

import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;

public class RenameLocalVariableActionHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//ISelection selection = HandlerUtil.getCurrentSelection(event);
		RenameLocalVariableDelegateCommonWork run = new RenameLocalVariableDelegateCommonWork();
		
		NesCEditor editor = getEditor(event);
		run.doWork(editor);
		/* Return is reserved for Future use. Must be null. Really!! */
		return null;
	}
	
	
	private NesCEditor getEditor(ExecutionEvent event) throws ExecutionException{
		NesCEditor editor = null;
		try{
			editor = ((MultiPageNesCEditor)HandlerUtil.getActiveEditor(event)).getNesCEditor();
		} catch(NullPointerException e) {
			throw new ExecutionException("You need to work in the Editor to use the Rename Local Variable Refactoring.");
		} catch(ClassCastException castError){
			throw new ExecutionException("You need to work in a NesCEditor to use the Rename Local Variable Refactoring.");
		}
		return editor;
	}

	

}
