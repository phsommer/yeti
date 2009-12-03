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
package tinyos.yeti.utility;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class DocumentUtility {
	public static String lastWord(IDocument doc, int offset) {
        try {
           for (int n = offset-1; n >= 0; n--) {
             char c = doc.getChar(n);
             if (!Character.isJavaIdentifierPart(c))
               return doc.get(n + 1, offset-n-1);
           }
           
           // since we run to n=0, the whole document before offset is only a word...
           return doc.get( 0, offset );
        } catch (BadLocationException e) {
           // ... log the exception ...
        }
        return "";
     }
	
	
}
