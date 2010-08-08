package tinyos.yeti.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.text.ITextSelection;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class AvailabilityTester extends PropertyTester {
	
	private Map<Refactoring, IRefactoringAvailabilityTester> testerMap = new HashMap<Refactoring,IRefactoringAvailabilityTester>(); 
	
	public AvailabilityTester() {
		testerMap.put(Refactoring.RENAME_LOCAL_VARIABLE, new tinyos.yeti.refactoring.rename.local.variable.AvailabilityTester());
		testerMap.put(Refactoring.RENAME_GLOBAL_VARIABLE, new tinyos.yeti.refactoring.rename.global.field.GlobalVariableAvailabilityTester());
		testerMap.put(Refactoring.RENAME_IMPLEMENTATION_LOCAL_VARIABLE, new tinyos.yeti.refactoring.rename.implementation.variable.AvailabilityTester());
		testerMap.put(Refactoring.RENAME_LOCAL_FUNCTION, new tinyos.yeti.refactoring.rename.local.function.AvailabilityTester());
		testerMap.put(Refactoring.RENAME_GLOBAL_FUNCTION, new tinyos.yeti.refactoring.rename.global.field.GlobalFunctionAvailabilityTester());
		testerMap.put(Refactoring.RENAME_INTERFACE, new tinyos.yeti.refactoring.rename.global.interfaces.AvailabilityTester());
		testerMap.put(Refactoring.RENAME_COMPONENT, new tinyos.yeti.refactoring.rename.component.AvailabilityTester());
		testerMap.put(Refactoring.RENAME_COMPONENT_ALIAS, new tinyos.yeti.refactoring.rename.alias.component.AvailabilityTester());
		testerMap.put(Refactoring.RENAME_INTERFACE_ALIAS, new tinyos.yeti.refactoring.rename.alias.interfaces.AvailabilityTester());
		testerMap.put(Refactoring.RENAME_NESC_FUNCTION, new tinyos.yeti.refactoring.rename.nesc.function.AvailabilityTester());
		testerMap.put(Refactoring.EXTRACT_FUNCTION,new tinyos.yeti.refactoring.extractFunction.AvailabilityTester());
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,Object expectedValue) {
		if(pluginNotReady()){
			return false;
		}
		if(!(receiver instanceof ITextSelection)){
			System.err.println("Falscher receiver Typ");
			return false;
		}
		ITextSelection selection = (ITextSelection) receiver;
		
		//If there is no refactoring available show the dummy refactoring in the menu.
		if(property.equals(Refactoring.NO_REFACTORING_AVAILABLE.toString())){
			return !isRefactoringAvailable(selection);
		}
		
		Refactoring refactoring=Refactoring.getRefactoring4Property(property);
		IRefactoringAvailabilityTester tester = testerMap.get(refactoring);
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
	 * Because the AST may not already be initialized, not doing this check could lead to nullpointer exceptions.
	 * This call makes sure that the IDE is fully loaded before the refactoring.
	 * @return 
	 */
	private boolean pluginNotReady() {
		NesCEditor editor = ActionHandlerUtil.getActiveEditor().getNesCEditor();
		NesC12AST ast = (NesC12AST) editor.getAST();
		return ast==null;
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
