package tinyos.yeti.refactoring;

import org.eclipse.jface.text.ITextSelection;

public interface IRefactoringAvailabilityTester{
	/**
	 * Checks if the given selection allows the refactoring to be executed.
	 * @param selection
	 * @return	true if the refactoring can be executed with the given selection, false otherwise.
	 */
	public boolean test(ITextSelection selection);
}
