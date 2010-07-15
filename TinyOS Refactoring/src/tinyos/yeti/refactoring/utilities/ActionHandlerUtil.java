package tinyos.yeti.refactoring.utilities;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.refactoring.RefactoringPlugin;

public class ActionHandlerUtil {
	public static NesCEditor getNesCEditor(ExecutionEvent event) throws ExecutionException{
		return ActionHandlerUtil.getMultiPageNesCEditor(event).getNesCEditor();
	}
	
	/**
	 * Can return you the MutiPageNesCEditor which was active by the time the action was called.
	 * @param event
	 * @return
	 * @throws ExecutionException Happens if the User was not working in a MultiPageNesCEditor at the time the Action was called.
	 */
	public static MultiPageNesCEditor getMultiPageNesCEditor(ExecutionEvent event) throws ExecutionException{
		MultiPageNesCEditor editor = null;
		try{
			editor = (MultiPageNesCEditor)HandlerUtil.getActiveEditor(event);
		} catch(NullPointerException e) {
			throw new ExecutionException("It was asumed that you work in an Editor but you didn't.");
		} catch(ClassCastException castError){
			throw new ExecutionException("It was asumed that you work in a NesCEditor but you didn't.");
		}
		return editor;
	}
	
	/**
	 * Retruns the File the Editor is editing
	 */
	public static IFile getInputFile(NesCEditor editor) {
		IEditorInput editorInput = editor.getEditorInput();
		if (!(editorInput instanceof IFileEditorInput)) {
			throw new IllegalStateException("The Editor Input must be a File");
		}
		IFile inputFile = ((IFileEditorInput) editorInput).getFile();
		return inputFile;
	}
	
	public static ITextSelection getSelection(NesCEditor editor){
		ISelection selectionTmp = editor.getSelectionProvider().getSelection();

		if (selectionTmp.isEmpty() || !(selectionTmp instanceof ITextSelection)) {
			throw new RuntimeException("----- Was not a ITextSelection");
		}

		return (ITextSelection) selectionTmp;
	}

	public static MultiPageNesCEditor getActiveEditor(){
		IEditorPart editor_object = RefactoringPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if(editor_object instanceof MultiPageNesCEditor){
			return ((MultiPageNesCEditor) editor_object);
		} else {
			System.err.println("Was looking for a MultiPageNesCEditor but found: "+editor_object.getClass().getCanonicalName());
			return null;
		}
	}
}
