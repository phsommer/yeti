package tinyos.yeti.refactoring.rename.globalvariable;

import org.eclipse.jface.text.ITextSelection;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.utilities.ASTUtil;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class AvailabilityTester implements tinyos.yeti.refactoring.AvailabilityTester.IRefactoringAvailabilityTester {

	@Override
	public boolean test(ITextSelection receiver) {
		NesCEditor editor = ActionHandlerUtil.getActiveEditor().getNesCEditor();
		NesC12AST ast = (NesC12AST) editor.getAST();
		ASTUtil util = new ASTUtil(ast);
		int addition=receiver.getLength()/2;
		int pos=receiver.getOffset()+addition;
		Identifier id = util.getASTLeafAtPos(pos, Identifier.class);
		if(id==null){
			return false;
		}
		return ASTUtil4Variables.isGlobalVariable(id);
	}

}