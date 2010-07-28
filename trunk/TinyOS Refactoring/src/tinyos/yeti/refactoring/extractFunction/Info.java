package tinyos.yeti.refactoring.extractFunction;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.refactoring.RefactoringInfo;

public class Info extends RefactoringInfo{

	private String functionName;
	
	
	public Info(NesCEditor editor) {
		super(editor, "Extract Infos", "Extract Function Wizzard");
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public String getFunctionName() {
		return functionName;
	}
	
	public int getSelectionBegin(){
		return getSelection().getOffset();
	}
	
	public int getSelectionEnd(){
		return getSelectionBegin()+getSelection().getLength();
	}

}
