package tinyos.yeti.refactoring.rename;

import org.eclipse.jface.text.ITextSelection;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ast.ASTPositioning;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public abstract class RenameAvailabilityTester implements tinyos.yeti.refactoring.IRefactoringAvailabilityTester {

	@Override
	public boolean test(ITextSelection receiver) {
		NesCEditor editor = ActionHandlerUtil.getActiveEditor().getNesCEditor();
		NesC12AST ast = (NesC12AST) editor.getAST();
		ASTPositioning util = new ASTPositioning(ast);
		Identifier selectedIdentifier = util.getASTLeafAtPos(receiver.getOffset(),receiver.getLength(), Identifier.class);
		if(selectedIdentifier==null){
			return false;
		}
		return isSelectionAppropriate(selectedIdentifier);
	}
	
	/**
	 * This function has to check, if the selected Identifier allows the Refactoring to proceed.
	 * @param selectedIdentifier
	 * @return
	 */
	abstract protected boolean isSelectionAppropriate(Identifier selectedIdentifier);
}