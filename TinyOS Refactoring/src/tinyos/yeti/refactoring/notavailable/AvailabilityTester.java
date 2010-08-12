package tinyos.yeti.refactoring.notavailable;

import org.eclipse.jface.text.ITextSelection;

import tinyos.yeti.refactoring.IRefactoringAvailabilityTester;

/**
 * This is just a dummy implementation and is not intended to be ever called, since the NoAvailable refactoring is a specially treated case.
 * If the test method still needs to be called, it is definined to return false.
 * @author Max Urech
 *
 */
public class AvailabilityTester implements IRefactoringAvailabilityTester{

	
	@Override
	public boolean test(ITextSelection receiver) {
		return false;
	}
}
