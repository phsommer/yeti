package tinyos.yeti.refactoring;

import org.eclipse.jface.text.ITextSelection;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

/**
 * Carries around all Information needed to execute a Refactoring
 * This Class is meant to be extended for each Refactoring
 */
public class RefactoringInfo {

	private String inputPageName;
	private String inputWizardName;
	private NesCEditor editor;
	private ITextSelection selection;
	private ASTUtil utility;
	
	public RefactoringInfo(NesCEditor editor, String inputPageName, String inputWizardName) {
		this.editor = editor;
		this.inputPageName = inputPageName;
		this.inputWizardName = inputWizardName;
		setSelection(ActionHandlerUtil.getSelection(editor));
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

	private void setSelection(ITextSelection selection) {
		this.selection = selection;
	}

	public ITextSelection getSelection() {
		return selection;
	}
	
	/**
	 * Returns the Ast for the currently selected Editor.
	 * @return
	 */
	protected NesC12AST getAst(){
		return (NesC12AST)getEditor().getAST();
	}
	
	/**
	 * Often the first initialization of the Class is before the AST is ready.
	 * This getter makes sure the AST is used, as soon as it is available.
	 * 
	 * @return
	 */
	public ASTUtil getAstUtil() {
		if (utility == null) {
			NesC12AST ast = getAst();
			if (ast != null) {
				utility = new ASTUtil(ast);
			}
		}
		return utility;
	}
	
	public ProjectUtil getProjectUtil(){
		return new ProjectUtil(getEditor());
	}

}
