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
package tinyos.yeti.editors.nesc.doc;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.preferences.PreferenceConstants;


public class NesCDocAutoIdentStrategy implements IAutoEditStrategy {


    public NesCDocAutoIdentStrategy() {

    }

    /**
     * Returns the first offset greater than <code>offset</code> and smaller than
     * <code>end</code> whose character is not a space or tab character. If no such
     * offset is found, <code>end</code> is returned.
     *
     * @param document the document to search in
     * @param offset the offset at which searching start
     * @param end the offset at which searching stops
     * @return the offset in the specified range whose character is not a space or tab
     * @exception BadLocationException if position is an invalid range in the given document
     */
    protected int findEndOfWhiteSpace(IDocument document, int offset, int end) throws BadLocationException {
        while (offset < end) {
            char c= document.getChar(offset);
            if (c != ' ' && c != '\t') {
                return offset;
            }
            offset++;
        }
        return end;
    }

    /**
     * Copies the indentation of the previous line.
     * Adds a '*' at the beginning
     * @param document the document to work on
     * @param command the command to deal with
     */
    public void autoIndentAfterNewLine(IDocument document, DocumentCommand command) {
        if (command.offset == -1 || document.getLength() == 0)
            return;

        try {
            // find start of line
            int p= (command.offset == document.getLength() ? command.offset  - 1 : command.offset);
            IRegion info= document.getLineInformationOfOffset(p);
            int start= info.getOffset();

            // find white spaces
            int end= findEndOfWhiteSpace(document, start, command.offset);

            StringBuffer buf= new StringBuffer(command.text);
            if (end > start) {
                // append to input
                buf.append(document.get(start, end - start));
            }

            boolean done = false;

            if( '/' == document.getChar( end ) && '*' == document.getChar( end+1 )){
                // search for ending of the current block
                if( !hasCommentEnding( document, end+2 )){
                    buf.append( " * " );
                    command.caretOffset = command.offset + buf.length();
                    buf.append( '\n' );
                    if (end > start) {
                        buf.append(document.get(start, end - start));
                    }
                    buf.append( " */" );
                    command.shiftsCaret = false;
                    done = true;
                }
            }
            if( !done ){
                if( '*' != document.getChar( end ) ){
                    buf.append( ' ' );
                }

                buf.append( "* " );
            }

            command.text = buf.toString();

        } catch (BadLocationException excp) { 
            // stop work 
        }
    }

    private boolean hasCommentEnding( IDocument document, int offset ){
        try{
            char first = '-';
            char second = '-';

            for( int i = offset, n = document.getLength(); i<n; i++ ){
                first = second;
                second = document.getChar( i );

                if( first == '/' && second == '*' )
                    return false;

                if( first == '*' && second == '/' )
                    return true;
            }
        }
        catch( BadLocationException ex ){
            return false;
        }

        return false;
    }

    public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
        if( command.length == 0 && command.text != null && TextUtilities.endsWith(document.getLegalLineDelimiters(), command.text) != -1){
            if( TinyOSPlugin.getDefault().getPreferenceStore().getBoolean( PreferenceConstants.AUTO_STRATEGY_IDENT )){
            	autoIndentAfterNewLine(document, command);
            }
        }
    }
}
