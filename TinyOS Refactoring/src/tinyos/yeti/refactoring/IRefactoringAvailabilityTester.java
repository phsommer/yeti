package tinyos.yeti.refactoring;

import org.eclipse.jface.text.ITextSelection;

public interface IRefactoringAvailabilityTester{
	public boolean test(ITextSelection receiver);
}
