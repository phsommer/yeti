package tinyos.yeti.refactoring.rename.local.variable;

import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.refactoring.rename.RenameAvailabilityTester;
import tinyos.yeti.refactoring.utilities.ASTUtil4Variables;

public class AvailabilityTester extends RenameAvailabilityTester{

	@Override
	protected boolean isSelectionAppropriate(Identifier selectedIdentifier) {
		return ASTUtil4Variables.isLocalVariable(selectedIdentifier);
	}
	
}


//implements tinyos.yeti.refactoring.AvailabilityTester.IRefactoringAvailabilityTester {

//	@Override
//	public boolean test(ITextSelection receiver) {
//		NesCEditor editor = ActionHandlerUtil.getActiveEditor().getNesCEditor();
//		
//		RenameInfo info = new RenameInfo("",editor);
//		info.setEditor(editor);
//		RenameProcessor processor = new RenameLocalVariableProcessor(info);
//		try {
//			return processor.checkInitialConditions(null).isOK();
//		} catch (OperationCanceledException e) {
//			e.printStackTrace();
//			return false;
//		} catch (CoreException e) {
//			e.printStackTrace();
//			return false;
//		}
//	}

//}
