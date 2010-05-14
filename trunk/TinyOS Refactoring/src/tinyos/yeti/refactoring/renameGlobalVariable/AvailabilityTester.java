package tinyos.yeti.refactoring.renameGlobalVariable;

import org.eclipse.jface.text.ITextSelection;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.ASTUtil;
import tinyos.yeti.refactoring.ActionHandlerUtil;

public class AvailabilityTester implements tinyos.yeti.refactoring.AvailabilityTester.IRefactoringAvailabilityTester {

	@Override
	public boolean test(ITextSelection receiver) {
		NesCEditor editor = ActionHandlerUtil.getActiveEditor().getNesCEditor();
		NesC12AST ast = (NesC12AST) editor.getAST();
		ASTUtil util = new ASTUtil(ast);
		int pos = ActionHandlerUtil.getSelection(editor).getOffset();
		try{
			Identifier id = util.getASTLeafAtPos(pos, Identifier.class);
			id.getASTNodeName();
			// TODO: Check if it is an Global Variable
			/*ProjectModel projectModel = editor.getProjectTOS().getModel();
			DeclarationFilter filter;
			IASTReference x = projectModel.getReferences(null, null)[0];*/
			return true;
		} catch (ClassCastException e){
			return false;
		}
	}

}
