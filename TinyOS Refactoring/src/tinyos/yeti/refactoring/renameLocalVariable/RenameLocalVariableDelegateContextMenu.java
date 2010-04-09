package tinyos.yeti.refactoring.renameLocalVariable;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;

public class RenameLocalVariableDelegateContextMenu implements IEditorActionDelegate{

	private NesCEditor editor; 
	
	
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if(targetEditor==null){
			System.err.println("IS NULL!");
		}
		if(targetEditor instanceof NesCEditor){
			this.editor=(NesCEditor)targetEditor;
		}else{
			System.err.println(targetEditor.getClass().toString());
			throw new IllegalArgumentException("Was not a MultiPageNesCEditor.");
		}
		
	}

	@Override
	public void run(IAction action) {
		RenameLocalVariableDelegateCommonWork commonWork=new RenameLocalVariableDelegateCommonWork();
		commonWork.doWork(editor);
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

}
