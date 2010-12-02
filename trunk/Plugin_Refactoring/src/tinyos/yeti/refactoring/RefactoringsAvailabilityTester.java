package tinyos.yeti.refactoring;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.text.ITextSelection;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;
import tinyos.yeti.refactoring.utilities.ProjectUtil;

public class RefactoringsAvailabilityTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args,Object expectedValue) {
		
		// We don't want the Refactoring Menu to come and go, so the "No Refactoring Available" will always stay
		boolean isIsPluginReadyEntry = property.equals(Refactoring.PLUGIN_NOT_READY.getPropertyName());
		
		if(!isPluginReady()){
			return isIsPluginReadyEntry;
		} else if(isIsPluginReadyEntry) {
			return false;
		}
		
		boolean isNotSavedEntry =property.equals(Refactoring.NOT_SAVED.getPropertyName());
		if(!isSaved()){
			// Only the "Not Saved" Error Entry gets displayed
			return isNotSavedEntry;
		} else if(isNotSavedEntry ){
			// The Not Saved Entry has to be handled even if it is saved
			return false;
		}
		
		if(!(receiver instanceof ITextSelection)){
			ProjectUtil util=new ProjectUtil( ActionHandlerUtil.getActiveEditor().getNesCEditor());
			util.log("Wrong receiver Typ");
			return isIsPluginReadyEntry;
		}
		ITextSelection selection = (ITextSelection) receiver;
		
		boolean isNoRefactoringAvailableEntry = property.equals(Refactoring.NO_REFACTORING_AVAILABLE.getPropertyName());
		// The Update does'nt work when no selection is done.
		// Thats way we force the User to do a real selection
		if(selection.getLength() == 0){
			return isNoRefactoringAvailableEntry;
		}
		
		//If there is no refactoring available show the dummy refactoring in the menu.
		if(isNoRefactoringAvailableEntry){
			return !isRefactoringAvailable(selection);
		}

		Refactoring refactoring=Refactoring.getRefactoring4Property(property);
		if(refactoring==null){
			ProjectUtil util=new ProjectUtil( ActionHandlerUtil.getActiveEditor().getNesCEditor());
			util.log("No Refactoring defined for property: "+property);
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
	 * Tests if all editors are saved. Is required to reach a deterministic behaviour.
	 * @return
	 */
	private boolean isSaved() {
		return ActionHandlerUtil.getUnsavedEditors().isEmpty();
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
			if(tester!=null){
				if(tester.test(selection)){
					return true;
				}
			}
		}
		return false;
	}

}
