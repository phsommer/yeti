package tinyos.yeti.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.text.ITextSelection;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.refactoring.utilities.ActionHandlerUtil;

public class AvailabilityTester extends PropertyTester {

	private enum Properies {
		renameLocalVariable, 
		renameGlobalVariable, 
		renameImplementationLocalVariable,
		renameLocalFunction, 
		renameGlobalFunction,
		renameInterface,
		renameComponent,
		extractFunction,
		NoRefactoringAvailable
	};
	private Map<Properies, IRefactoringAvailabilityTester> testerMap = new HashMap<Properies,IRefactoringAvailabilityTester>(); 
	
	public AvailabilityTester() {
		testerMap.put(Properies.renameLocalVariable, new tinyos.yeti.refactoring.rename.local.variable.AvailabilityTester());
		testerMap.put(Properies.renameGlobalVariable, new tinyos.yeti.refactoring.rename.global.field.GlobalVariableAvailabilityTester());
		testerMap.put(Properies.renameImplementationLocalVariable, new tinyos.yeti.refactoring.rename.implementation.variable.AvailabilityTester());
		testerMap.put(Properies.renameLocalFunction, new tinyos.yeti.refactoring.rename.local.function.AvailabilityTester());
		testerMap.put(Properies.renameGlobalFunction, new tinyos.yeti.refactoring.rename.global.field.GlobalFunctionAvailabilityTester());
		testerMap.put(Properies.renameInterface, new tinyos.yeti.refactoring.rename.global.interfaces.AvailabilityTester());
		testerMap.put(Properies.renameComponent, new tinyos.yeti.refactoring.rename.component.AvailabilityTester());
		testerMap.put(Properies.extractFunction,new tinyos.yeti.refactoring.extractFunction.AvailabilityTester());
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
