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

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


import java.io.IOException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import tinyos.yeti.editors.nesc.information.html.SingleCharReader;

/**
 * Reads from a document either forwards or backwards. May be configured to
 * skip comments and strings.
 */
public class NesCCodeReader extends SingleCharReader {

    /** The EOF character */
    public static final int EOF= -1;

    private boolean fSkipComments= false;
    private boolean fSkipStrings= false;
    private boolean fForward= false;

    private IDocument fDocument;
    private int fOffset;

    private int fEnd= -1;
    private int fCachedLineNumber= -1;
    private int fCachedLineOffset= -1;


    public NesCCodeReader() {
    }

    /**
     * Returns the offset of the last read character. Should only be called after read has been called.
     */
    public int getOffset() {
        return fForward ? fOffset -1 : fOffset;
    }

    public void configureForwardReader(IDocument document, int offset, int length, boolean skipComments, boolean skipStrings) {
        fDocument= document;
        fOffset= offset;
        fSkipComments= skipComments;
        fSkipStrings= skipStrings;

        fForward= true;
        fEnd= Math.min(fDocument.getLength(), fOffset + length);		
    }

    public void configureBackwardReader(IDocument document, int offset, boolean skipComments, boolean skipStrings) throws BadLocationException {
        fDocument= document;
        fOffset= offset;
        fSkipComments= skipComments;
        fSkipStrings= skipStrings;

        fForward= false;
        fCachedLineNumber= fDocument.getLineOfOffset(fOffset);
    }

    /*
     * @see Reader#close()
     */
    @Override
    public void close() throws IOException {
        fDocument= null;
    }

    /*
     * @see SingleCharReader#read()
     */
    @Override
    public int read() throws IOException {
        try {
            return fForward ? readForwards() : readBackwards();
        } catch (BadLocationException x) {
            throw new IOException(x.getMessage());
        }
    }

    public int next() throws BadLocationException{
        return fForward ? readForwards() : readBackwards();
    }
    
    private void gotoCommentEnd() throws BadLocationException {
        while (fOffset < fEnd) {
            char current= fDocument.getChar(fOffset++);
            if (current == '*') {
                if (fOffset < fEnd && fDocument.getChar(fOffset) == '/') {
                    ++ fOffset;
                    return;
                }
            }
        }
    }

    private void gotoStringEnd(char delimiter) throws BadLocationException {
        while (fOffset < fEnd) {
            char current= fDocument.getChar(fOffset++);
            if (current == '\\') {
                // ignore escaped characters
                ++ fOffset;
            } else if (current == delimiter) {
                return;
            }
        }
    }

    private void gotoLineEnd() throws BadLocationException {
        int line= fDocument.getLineOfOffset(fOffset);
        int lines = fDocument.getNumberOfLines();
        if( line+1 == lines ){
        	fOffset = fDocument.getLength()-1;
        }
        else{
        	fOffset= fDocument.getLineOffset(line + 1);
        }
    }

    private int readForwards() throws BadLocationException {
        while (fOffset < fEnd) {
            char current= fDocument.getChar(fOffset++);

            switch (current) {
                case '/':

                    if (fSkipComments && fOffset < fEnd) {
                        char next= fDocument.getChar(fOffset);
                        if (next == '*') {
                            // a comment starts, advance to the comment end
                            ++ fOffset;
                            gotoCommentEnd();
                            continue;
                        } else if (next == '/') {
                            // '//'-comment starts, advance to the line end
                            gotoLineEnd();
                            continue;
                        }
                    }

                    return current;

                case '"':
                case '\'':

                    if (fSkipStrings) {
                        gotoStringEnd(current);
                        continue;
                    }

                    return current;
            }

            return current;
        }

        return EOF;
    }

    private void handleSingleLineComment() throws BadLocationException {
        int line= fDocument.getLineOfOffset(fOffset);
        if (line < fCachedLineNumber) {
            fCachedLineNumber= line;
            fCachedLineOffset= fDocument.getLineOffset(line);
            int offset= fOffset;
            while (fCachedLineOffset < offset) {
                char current= fDocument.getChar(offset--);
                if (current == '/' && fCachedLineOffset <= offset && fDocument.getChar(offset) == '/') {
                    fOffset= offset;
                    return;
                }
            }
        }
    }

    private void gotoCommentStart() throws BadLocationException {
        while (0 < fOffset) {
            char current= fDocument.getChar(fOffset--);
            if (current == '*' && 0 <= fOffset && fDocument.getChar(fOffset) == '/')
                return;
        }
    }

    private void gotoStringStart(char delimiter) throws BadLocationException {
        while (0 < fOffset) {
            char current= fDocument.getChar(fOffset);
            if (current == delimiter) {
                if ( !(0 <= fOffset && fDocument.getChar(fOffset -1) == '\\'))
                    return;
            }
            -- fOffset;
        }
    }

    private int readBackwards() throws BadLocationException {

        while (0 < fOffset) {
            -- fOffset;

            handleSingleLineComment();

            char current= fDocument.getChar(fOffset);
            switch (current) {
                case '/':

                    if (fSkipComments && fOffset > 1) {
                        char next= fDocument.getChar(fOffset - 1);
                        if (next == '*') {
                            // a comment ends, advance to the comment start
                            fOffset -= 2;
                            gotoCommentStart();
                            continue;
                        }
                    }

                    return current;

                case '"':
                case '\'':

                    if (fSkipStrings) {
                        -- fOffset;
                        gotoStringStart(current);
                        continue;
                    }

                    return current;
            }

            return current;
        }

        return EOF;
    }
}