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

/**
 * A document map tells for each character of some document to what kind of 
 * partition it belongs.
 * @author Benjamin Sigg
 *
 */
public interface IDocumentMap{
    public static final int TYPE_SINGLE_LINE_COMMENT = 1;
    public static final int TYPE_MULTI_LINE_COMMENT = 2;
    public static final int TYPE_NESC_DOC = 4;
    public static final int TYPE_STRING = 8;
    public static final int TYPE_DEFAULT = 16;
    public static final int TYPE_SOURCE = TYPE_STRING | TYPE_DEFAULT;
    
    public IDocument getDocument();
    
    public char getChar( int offset ) throws BadLocationException;
    
    public String get( int offset, int length ) throws BadLocationException;
    
    /**
     * Checks whether the type at <code>offset</code> is one of the types
     * described by <code>flag</code>
     * @param offset the location in the document
     * @param flag some combination of flags
     * @return <code>true</code> if at least one of the flags is met
     * @throws BadLocationException if <code>offset</code> is not legal
     */
    public boolean isType( int offset, int flag ) throws BadLocationException;
    
    public boolean isSingleLineComment( int offset ) throws BadLocationException;
    
    public boolean isMultiLineComment( int offset ) throws BadLocationException;
    
    public boolean isNesCDoc( int offset ) throws BadLocationException;
    
    public boolean isString( int offset ) throws BadLocationException;
    
    public boolean isDefault( int offset ) throws BadLocationException;
    
    public boolean isSource( int offset ) throws BadLocationException;
}
