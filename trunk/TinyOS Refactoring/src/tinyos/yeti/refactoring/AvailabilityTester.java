package tinyos.yeti.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.text.ITextSelection;

public class AvailabilityTester extends PropertyTester {

	private enum Properies {renameLocalVariable, renameGlobalVariable,renameFunction,NoRefactoringAvailable};
	private Map<Properies, IRefactoringAvailabilityTester> testerMap = new HashMap<Properies,IRefactoringAvailabilityTester>(); 
	
	public AvailabilityTester() {
		testerMap.put(Properies.renameLocalVariable, new tinyos.yeti.refactoring.renameLocalVariable.AvailabilityTester());
		testerMap.put(Properies.renameGlobalVariable, new tinyos.yeti.refactoring.renameGlobalVariable.AvailabilityTester());
		testerMap.put(Properies.renameFunction, new tinyos.yeti.refactoring.renameFunction.AvailabilityTester());
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,Object expectedValue) {
		if(!(receiver instanceof ITextSelection)){
			System.err.println("Falscher receiver Typ");
			return false;
		}
		ITextSelection selection = (ITextSelection) receiver;
		//If there is no refactoring available show the dummy refactoring in the menu.
		if(property.equals(Properies.NoRefactoringAvailable.toString())){
			return !isRefactoringAvailable(selection);
		}
		
		IRefactoringAvailabilityTester tester = testerMap.get(Properies.valueOf(property));
		if(tester == null){
			System.err.println("No Tester Available");
			return false;
		}
		
		try{
			return tester.test(selection);
		} catch (NullPointerException e) {
			// Happens when the System is not jet initialized, but the Property is already checked.
			return false;
		}
	}
	
	/**
	 * Tests if there is any Refactoring available
	 * @param selection
	 * @return
	 */
	private boolean isRefactoringAvailable(ITextSelection selection) {
		for(IRefactoringAvailabilityTester tester:testerMap.values()){
			if(tester.test(selection)){
				return true;
			}
		}
		return false;
	}

	public interface IRefactoringAvailabilityTester{
		public boolean test(ITextSelection receiver);
	}

}
