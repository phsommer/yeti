package tinyos.yeti.refactoring;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.text.ITextSelection;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class AvailabilityTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args,Object expectedValue) {
		// We don't want the Refactoring Menu to come and go, so the "No Refactoring Available" will always stay
		boolean isNotAvailableEntry = property.equals(Refactoring.NO_REFACTORING_AVAILABLE.getPropertyName());
		
		if(!isPluginReady()){
			return isNotAvailableEntry;
		}
		
		if(!(receiver instanceof ITextSelection)){
			System.err.println("Wrong receiver Typ");
			return isNotAvailableEntry;
		}
		ITextSelection selection = (ITextSelection) receiver;
		
		// The Update does'nt work when no selection is done.
		// Thats way we force the User to do a real selection
		if(selection.getLength() == 0){
			return isNotAvailableEntry;
		}
		
		//If there is no refactoring available show the dummy refactoring in the menu.
		if(isNotAvailableEntry){
			return !isRefactoringAvailable(selection);
		}
		
		Refactoring refactoring=Refactoring.getRefactoring4Property(property);
		if(refactoring==null){
			System.err.println("No Refactoring defined for property: "+property);
			return false;
		}
		IRefactoringAvailabilityTester tester = refactoring.getTester();
		try{
			return tester.test(selection);
		} catch (NullPointerException e) {
			// Happens when the System is not jet initialized, but the Property is already checked.
			return false;
		}
	}
	
	/**
	 * Because the AST may not already be initialized, not doing this check could lead to nullpointer exceptions.
	 * This call makes sure that the IDE is fully loaded before the refactoring.
	 * @return 
	 */
	private boolean isPluginReady() {
		NesCEditor editor = ActionHandlerUtil.getActiveEditor().getNesCEditor();
		NesC12AST ast = (NesC12AST) editor.getAST();
		return ast!=null;
	}

	/**
	 * Tests if there is any Refactoring available
	 * @param selection
	 * @return
	 */
	private boolean isRefactoringAvailable(ITextSelection selection) {
		for(Refactoring refactoring:Refactoring.values()){
		IRefactoringAvailabilityTester tester=refactoring.getTester();
			if(tester.test(selection)){
				return true;
			}
		}
		return false;
	}

}
