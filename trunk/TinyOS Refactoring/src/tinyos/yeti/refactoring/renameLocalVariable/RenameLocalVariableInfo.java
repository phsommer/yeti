package tinyos.yeti.refactoring.renameLocalVariable;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCEditor;

public class RenameLocalVariableInfo {
	private String oldName;
	private String newName = null;
	private String inputPageName = "Getting the new Variable name.";
	private NesCEditor editor;

	public void setEditor(NesCEditor editor) {
		this.editor = editor;
	}
	
	public NesCEditor getEditor() {
		return editor;
	}

	public RenameLocalVariableInfo(String oldName) {
		this.oldName = oldName;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public String getOldName() {
		return oldName;
	}

	public void setInputPageName(String inputPageName) {
		this.inputPageName = inputPageName;
	}

	public String getInputPageName() {
		return inputPageName;
	}

	/**
	 * Retruns the File the Editor is editing
	 */
	public IFile getInputFile() {
		IEditorInput editorInput = this.getEditor().getEditorInput();
		if (!(editorInput instanceof IFileEditorInput)) {
			throw new IllegalStateException("The Editor Input must be a File");
		}
		IFile inputFile = ((IFileEditorInput) editorInput).getFile();
		return inputFile;
	}

}
