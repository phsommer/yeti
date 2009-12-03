/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos.yeti.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * Double click strategy aware of Java identifier syntax rules.
 */
public class NesCDoubleClickSelector implements ITextDoubleClickStrategy {
	
	
	protected static final char[] BRACKETS= {'{', '}', '(', ')', '[', ']' };
	
	protected NesCPairMatcher fPairMatcher= new NesCPairMatcher(BRACKETS);
		
	
	public NesCDoubleClickSelector() {
		super();
	}
	
	/**
	 * @see ITextDoubleClickStrategy#doubleClicked
	 */
	public void doubleClicked(ITextViewer textViewer) {
		
		int offset= textViewer.getSelectedRange().x;
		
		if (offset < 0)
			return;
			
		IDocument document= textViewer.getDocument();
		
		IRegion region= fPairMatcher.match(document, offset);
		if (region != null && region.getLength() >= 2)
			textViewer.setSelectedRange(region.getOffset() + 1, region.getLength() - 2);
		else
			selectWord(textViewer, document, offset);
	}

	protected void selectWord(ITextViewer textViewer, IDocument document, int anchor) {

		try {

			int offset= anchor;
			char c;

			while (offset >= 0) {
				c= document.getChar(offset);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--offset;
			}

			int start= offset;

			offset= anchor;
			int length= document.getLength();

			while (offset < length) {
				c= document.getChar(offset);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++offset;
			}
			
			int end= offset;
			
			if (start == end)
				textViewer.setSelectedRange(start, 0);
			else
				textViewer.setSelectedRange(start + 1, end - start - 1);
			
		} catch (BadLocationException x) {
		}
	}
}