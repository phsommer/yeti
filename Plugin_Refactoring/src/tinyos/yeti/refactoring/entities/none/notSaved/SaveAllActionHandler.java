package tinyos.yeti.refactoring.entities.none.notSaved;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;

import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;



public class SaveAllActionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Collection<org.eclipse.ui.IEditorPart> editors = ActionHandlerUtil.getUnsavedEditors();
		for(org.eclipse.ui.IEditorPart editor:editors){
			editor.doSave(new NullProgressMonitor());
		}
		
		
		return null;
	}



}
