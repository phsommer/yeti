package tinyos.yeti.refactoring;

import tinyos.yeti.editors.NesCEditor;

public class RefactoringInfo {

	private String inputPageName;
	private String inputWizardName;
	private NesCEditor editor;
	
	public RefactoringInfo(NesCEditor editor, String inputPageName, String inputWizardName) {
		this.editor = editor;
		this.inputPageName = inputPageName;
		this.inputWizardName = inputWizardName;
	}

	public void setInputPageName(String inputPageName) {
		this.inputPageName = inputPageName;
	}

	public String getInputPageName() {
		return inputPageName;
	}

	public void setInputWizardName(String inputWizardName) {
		this.inputWizardName = inputWizardName;
	}

	public String getInputWizardName() {
		return inputWizardName;
	}

	public void setEditor(NesCEditor editor) {
		this.editor = editor;
	}

	public NesCEditor getEditor() {
		return editor;
	}

}
