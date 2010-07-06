package tinyos.yeti.refactoring.renameFunction;

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
			Identifier identifier = util.getASTLeafAtPos(pos, Identifier.class);
			return 	ASTUtil4Functions.isFunctionCall(identifier)
					||ASTUtil4Functions.isFunctionDefinition(identifier);
		} catch (ClassCastException e){
			return false;
		}
	}
	


}
