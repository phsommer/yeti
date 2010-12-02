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
package tinyos.yeti.editors.nesc;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.rules.IToken;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.INesCPartitions;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.nesc.doc.NesCDocAutoIdentStrategy;
import tinyos.yeti.preferences.PreferenceConstants;
import tinyos.yeti.utility.GenericTemplate;

public class NesCAutoIdentStrategy implements IAutoEditStrategy {
	private char[][] bracketTriggers = new char[][]{
			{ '[', ']' },
			{ '(', ')' },
			{ '"', '"' },
			{ '\'', '\'' }
	};

	private NesCEditor editor;

	/**
	 * Creates a new indent line auto edit strategy which can be installed on
	 * text viewers.
	 * @param editor the editor for which this strategy will work
	 */
	public NesCAutoIdentStrategy( NesCEditor editor ) {
		this.editor = editor;
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
	 * Copies the indentation of the previous line if the current
	 * line endswith a { an additional tabwith is inserted
	 *
	 * @param document the document to work on
	 * @param command the command to deal with
	 */
	private void autoIndentAfterNewLine(IDocument document, DocumentCommand command) {
		if( command.offset == -1 || document.getLength() == 0 )
			return;

		try {
			// find start of line
			int offset = (command.offset == document.getLength() ? command.offset  - 1 : command.offset);
			IRegion info= document.getLineInformationOfOffset( offset );
			offset = command.offset;
			int start = info.getOffset();

			// find white spaces
			int end = findEndOfWhiteSpace( document, start, command.offset );

			if( startingCommentBlock( document, end, offset )){
				NesCDocAutoIdentStrategy delegate = new NesCDocAutoIdentStrategy();
				delegate.autoIndentAfterNewLine( document, command );
				return;
			}

			StringBuffer buf= new StringBuffer(command.text);
			String insert;
			if( end > start )
				insert = document.get( start, end - start );
			else
				insert = "";

			if( startingBlock( document, offset ) ){
				int before = countBlockLevel( 0, offset, document );
				int after = countBlockLevel( offset, document.getLength()-offset, document );
				if( before + after > 0 ){
					buf.append( insert );
					buf.append( getTab() );
					command.shiftsCaret = false;
					command.caretOffset = offset + buf.length();

					// here would be a good place to check whether the } is really needed
					buf.append( '\n' );
					buf.append( insert );
					buf.append( '}' );
					command.text = buf.toString();
					return;
				}
				else{
					buf.append( insert );
					buf.append( getTab() );
					command.text = buf.toString();
					return;
				}
			}

			buf.append( insert );
			command.text = buf.toString();
		}
		catch( BadLocationException excp ){
			// stop work
		}
	}

	public static String getTab(){
		if( TinyOSPlugin.getDefault().getPreferenceStore().getBoolean( PreferenceConstants.USE_TABS )){ 
			return "\t";
		}
		else {
			return PreferenceConstants.spacesPerTab();
		}
	}

	private boolean startingCommentBlock( IDocument document, int firstNonWhitespace, int offset ){
		try{
			if( firstNonWhitespace+1 < offset ){
				return document.getChar( firstNonWhitespace ) == '/' && document.getChar( firstNonWhitespace + 1 ) == '*';
			}
			return false;
		}
		catch( BadLocationException ex ){
			return false;
		}
	}

	private boolean startingBlock( IDocument document, int offset ){
		try{
			return document.getChar( offset-1 ) == '{';
		}
		catch ( BadLocationException e ){
			return false;
		}
	}

	/**
	 * Goes through the document and searches for { and }, counts { as 1, and
	 * } as -1 and returns the sum of all the parenthesis
	 * @param offset the begin of the search
	 * @param length the number of sign to search
	 * @param document the document to search in
	 * @return the level, may be below 0
	 */
	private int countBlockLevel( int offset, int length, IDocument document ){
		if( length <= 0 )
			return 0;

		int level = 0;
		try{
			NesCPartitionScanner2 scanner = new NesCPartitionScanner2();
			scanner.setRange( document, offset, length );

			IToken token;
			while( (token = scanner.nextToken()) != null ){
				if( token.isEOF() )
					break;

				if( token.getData() == null || INesCPartitions.DEFAULT.equals( token.getData() )){
					offset = scanner.getTokenOffset();
					length = scanner.getTokenLength();

					for( int i = 0; i < length; i++ ){
						char c = document.getChar( i+offset );
						if( c == '{' )
							level++;
						else if( c == '}' )
							level--;
					}
				}
			}
			
			scanner.disconnect();
		}
		catch ( BadLocationException e ){
			e.printStackTrace();
		}
		return level;
	}

	private void autoIndentBracketCheck( IDocument document, DocumentCommand command ){
		if( command.text != null && command.text.length() == 1 ){
			char c = command.text.charAt( 0 );
			for( char[] bracket : bracketTriggers ){
				if( bracket[0] == c ){
					GenericTemplate template = new GenericTemplate( "$" + bracket[0] + "${}" + "$" + bracket[1] );
					template.apply( editor, command );
					
					return;
				}
			}
		}
	}

	public void customizeDocumentCommand( IDocument d, DocumentCommand c ){
		if( c.text != null && c.length == 0 ){
			if (TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1) {
				if( TinyOSPlugin.getDefault().getPreferenceStore().getBoolean( PreferenceConstants.AUTO_STRATEGY_IDENT )){
					autoIndentAfterNewLine(d, c);
				}
			}
			else if ( "\t".equals( c.text ) ) {
				if (!TinyOSPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.USE_TABS)){ 
					c.text = PreferenceConstants.spacesPerTab();
				}
			}
			else{
				if( TinyOSPlugin.getDefault().getPreferenceStore().getBoolean( PreferenceConstants.AUTO_BRACKETS )){
					autoIndentBracketCheck( d, c );
				}
			}
		}
	}
}
