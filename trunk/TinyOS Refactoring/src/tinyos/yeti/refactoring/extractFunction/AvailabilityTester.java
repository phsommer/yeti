package tinyos.yeti.refactoring.extractFunction;

import org.eclipse.jface.text.ITextSelection;

import tinyos.yeti.refactoring.AvailabilityTester.IRefactoringAvailabilityTester;

public class AvailabilityTester implements IRefactoringAvailabilityTester{

	@Override
	public boolean test(ITextSelection receiver) {
		// TODO
		return true;
	}

}
