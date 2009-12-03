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
package tinyos.yeti.nesc12.ep.rules;

import org.eclipse.jface.text.BadLocationException;

import tinyos.yeti.editors.IDocumentMap;

/**
 * A reader that goes backwards through a document and searches words in the
 * document. This reader uses {@link RuleUtility#reverseWord(int, IDocumentMap)},
 * and cannot jump over special signs like a point.
 * @author Benjamin Sigg
 */
public class ReverseWordReader {
    private IDocumentMap document;
    private int offset;

    /**
     * Creates a new reader
     * @param document the document to read
     * @param offset the index of the first character that should be checked,
     * might be -1
     */
    public ReverseWordReader( IDocumentMap document, int offset ){
        this.document = document;
        this.offset = offset;
    }

    /**
     * Reads the previous word of the current location of this reader.
     * @return the previous word or <code>null</code>
     * @throws BadLocationException if the location was too high
     */
    public String previous() throws BadLocationException{
        if( offset < 0 )
            return null;
        
        String word = RuleUtility.reverseWord( offset, document );
        offset = RuleUtility.reverseWordBegin( offset, document );
        
        offset--;
        
        return word;
    }
}
