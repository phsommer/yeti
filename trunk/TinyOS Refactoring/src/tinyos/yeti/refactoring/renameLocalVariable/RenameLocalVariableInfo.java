package tinyos.yeti.refactoring.renameLocalVariable;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.refactoring.rename.RenameInfo;

public class RenameLocalVariableInfo extends RenameInfo{
	private NesCEditor editor;

	public void setEditor(NesCEditor editor) {
		this.editor = editor;
	}
	
	public NesCEditor getEditor() {
		return editor;
	}

	public RenameLocalVariableInfo(String oldName) {
		super(oldName);
		this.setInputWizardName("Rename local Varible");
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
