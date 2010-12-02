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

/**
 * Helper class detecting single lines that need to be indented.
 * @author Benjamin Sigg
 */
public class SingleLineIndentDetector{
	private static enum ConditionalRead{
		/** default state */
		NOTHING,
		/** keyword has been read */
		READ_KEYWORD, 
		
		/** currently reading condition */
		READING_CONDITION, 
		
		/** condition has been read */
		READ_CONDITION;
	}

	private char[][] conditionals = { "if".toCharArray(), "while".toCharArray(), "for".toCharArray(), "else".toCharArray() };
	
	private ConditionalRead conditions = ConditionalRead.NOTHING;
	private int bracketCount = 0;
	
	private ConditionalKeywordDetector detector;
	
	private boolean thisLineIndent;
	private boolean thisLineIndentConfirmed;
	private boolean nextLineIndent = false;
	
	public void startLine( int length ){
		detector = new ConditionalKeywordDetector( length );
		
		thisLineIndent = nextLineIndent;
		thisLineIndentConfirmed = false;
		nextLineIndent = false;
	}
	
	public boolean thisLineHasIndent(){
		return thisLineIndent;
	}
	
	public void pushSource( char c ){
		switch( conditions ){
			case NOTHING:
				if( detector.push( c )){
					conditions = ConditionalRead.READ_KEYWORD;
					nextLineIndent = true;
				}
				break;
			case READ_KEYWORD:
				detector.push( c );
				if( detector.equalsLastKeyword( "else" )){
					conditions = ConditionalRead.READ_CONDITION;
				}
				else{
					if( c == '(' ){
						conditions = ConditionalRead.READING_CONDITION;
						bracketCount = 1;
					}
					else if( !Character.isWhitespace( c )){
						conditions = ConditionalRead.NOTHING;
					}
				}
				break;
			case READING_CONDITION:
				detector.push( c );
				if( c == '(' )
					bracketCount++;
				else if( c == ')' ){
					bracketCount--;
					if( bracketCount == 0 ){
						conditions  = ConditionalRead.READ_CONDITION;
						nextLineIndent = true;
					}
				}
				break;
			case READ_CONDITION:
				detector.push( c );
				if( !Character.isWhitespace( c ) ){
					nextLineIndent = false;
					conditions = ConditionalRead.NOTHING;
				}
				break;
		}
		
		if( thisLineIndent && !thisLineIndentConfirmed ){
			if( c == '{' ){
				thisLineIndent = false;
				thisLineIndentConfirmed = true;
			}
			else if( !Character.isWhitespace( c )){
				thisLineIndentConfirmed = true;
			}
		}
	}
	
	public void pushText( char c ){
		detector.push( c );
	}
	
	private class ConditionalKeywordDetector{
		private char[] buffer;
		private int offset = 0;
		private char[] lastKeyword;
		
		public ConditionalKeywordDetector( int length ){
			buffer = new char[ length ];
		}
		
		public boolean equalsLastKeyword( String keyword ){
			return keyword.contentEquals( new String( lastKeyword ) );
		}
		
		public boolean push( char c ){
			buffer[ offset++ ] = c;
			testing: for( char[] test : conditionals ){
				for( int i = test.length-1, j = offset-1; i >= 0 && j >= 0; i--, j-- ){
					if( test[i] != buffer[j] ){
						continue testing;
					}
				}
				int index = offset-test.length-1;
				if( index < 0 ){
					lastKeyword = test;
					return true;
				}
				
				if( Character.isWhitespace( buffer[index] )){
					lastKeyword = test;
					return true;
				}
			}
			return false;
		}
	}
}
