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
package tinyos.yeti.editors.format;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import tinyos.yeti.TinyOSPlugin;

/**
 * Scans a nesc document and tells for each line how much indent it should have.
 * @author Benjamin Sigg
 *
 */
public class IndentScanner{
	private IDocument source;
	
	private boolean previousLineInComment = false;
	
	private int[] indents;
	private int[] spaces;
	
	private int advanced = -1;
	
	private SingleLineIndentDetector singleLineIndent = new SingleLineIndentDetector();
	
	public IndentScanner( IDocument source ){
		this.source = source;
		
		int count = getLineCount();
		indents = new int[ count ];
		spaces = new int[ count ];
	}
	
	public int getLineCount(){
		return source.getNumberOfLines();
	}
	
	public int getIndent( int line ){
		advance( line );
		return Math.max( 0, indents[ line ] );
	}

	public int getSpace( int line ){
		advance( line );
		return Math.max( 0, spaces[ line ] );
	}
	
	private void advance( int line ){
		try{
			while( advanced < line ){
				advanced++;
				int lineOffset = source.getLineOffset( advanced );
				int lineLength = source.getLineLength( advanced );
				
				Delta delta = lineDelta( lineOffset, lineLength, line );
				
				if( advanced > 0 ){
					spaces[ advanced ] += spaces[ advanced-1 ];
					indents[ advanced ] += indents[ advanced-1 ];
				}
				
				spaces[ advanced ] += delta.thisSpace;
				indents[ advanced ] += delta.thisIndent;
				
				if( advanced+1 < spaces.length ){
					spaces[ advanced+1 ] = delta.nextSpace;
					indents[ advanced+1 ] = delta.nextIndent;
				}
			}
		}
		catch( BadLocationException ex ){
			TinyOSPlugin.log( ex );
		}
	}
	
	private Delta lineDelta( int offset, int length, int line ) throws BadLocationException{
		char stringStart = 0;
		boolean inString = false;
		boolean escape = false;
		boolean commentStart = false;
		boolean commentEnd = false;
		boolean inComment = previousLineInComment;
		
		boolean seenNonWhitespace = false;
		
		singleLineIndent.startLine( length );
		Delta delta = new Delta();
		
		for( int i = 0; i<length; i++ ){
			char c = source.getChar( offset + i );
			
			if( inString ){
				singleLineIndent.pushText( c );
				if( escape ){
					escape = false;
				}
				else{
					if( c == '\\' ){
						escape = true;
					}
					else if( c == stringStart ){
						inString = false;
					}
				}
			}
			else if( inComment ){
				singleLineIndent.pushText( c );
				if( commentEnd ){
					if( c == '/' ){
						inComment = false;
					}
					if( c != '*'){
						commentEnd = false;
					}
				}
				else if( c == '*' ){
					commentEnd = true;
					if( !seenNonWhitespace ){
						delta.thisSpace += 1;
						delta.nextSpace -= 1;
					}
				}
			}
			else{
				singleLineIndent.pushSource( c );
				if( c == '\'' || c == '"' ){
					inString = true;
					stringStart = c;
				}
				else if( c == '/' ){
					if( commentStart ){
						previousLineInComment = false;
						break;
					}
					commentStart = true;
				}
				else if( c == '*' ){
					if( commentStart ){
						commentStart = false;
						inComment = true;
					}
				}
				else if( c == '{' ){
					delta.nextIndent += 1;
				}
				else if( c == '}' ){
					if( seenNonWhitespace )
						delta.nextIndent -= 1;
					else
						delta.thisIndent -= 1;
				}
				else if( c == '(' ){
					delta.nextIndent += 2;
				}
				else if( c == ')' ){
					if( seenNonWhitespace )
						delta.nextIndent -= 2;
					else
						delta.thisIndent -= 2;
				}
			}
			
			if( !seenNonWhitespace ){
				seenNonWhitespace = !Character.isWhitespace( c );
			}
		}
		
		if( singleLineIndent.thisLineHasIndent() ){
			delta.thisIndent++;
			delta.nextIndent--;
		}
		
		previousLineInComment = inComment;
		return delta;
	}
	
	private class Delta{
		public int thisIndent;
		public int nextIndent;
		
		public int thisSpace;
		public int nextSpace;
	}
	

}
