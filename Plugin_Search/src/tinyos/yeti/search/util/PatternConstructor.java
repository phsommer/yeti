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
package tinyos.yeti.search.util;

import java.util.regex.Pattern;

/**
 * Creates {@link Pattern}s.
 * @author Benjamin Sigg
 */
public class PatternConstructor {
	public static Pattern convert( String input, boolean regex, boolean caseSensitive, boolean wholeWord ){
		if( !regex ){
			input = toRegex( input );
		}

		if( wholeWord ){
			input = toWholeWord( input );
		}

		int options = Pattern.MULTILINE;
		if( !caseSensitive ){
			options |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		}

		return Pattern.compile( input, options );
	}

	private static String toWholeWord( String input ){
		StringBuilder builder = new StringBuilder( input.length()+10 );
		builder.append( "\\b(?:" );
		builder.append( input );
		builder.append( ")\\b" );
		return builder.toString();
	}

	private static String toRegex( String input ){
		boolean escape = false;
		StringBuilder builder = new StringBuilder();
		
		for( int i = 0, n = input.length(); i<n; i++ ){
			char c= input.charAt( i );

			switch(c) {
				case '\\':
					if( escape ){
						builder.append( "\\\\" );
						escape = false;
					}
					else{
						escape = true;
					}
					break;
					
				case '?':
					if( escape ){
						builder.append( "\\?" );
						escape = false;
					}
					else{
						builder.append( "." );
					}
					break;
					
				case '*':
					if( escape ){
						builder.append( "\\*" );
						escape = false;
					}
					else{
						builder.append( ".*" );
					}
					break;
					
	            case '(':
	            case ')':
	            case '{':
	            case '}':
	            case '.':
	            case '[':
	            case ']':
	            case '$':
	            case '^':
	            case '+':
	            case '|':
	            	if( escape ){
	            		builder.append( "\\\\" );
	            		escape = false;
	            	}
	            	builder.append( "\\" );
	            	builder.append( c );
					break;
				default:
					if( escape ){
						builder.append( "\\\\" );
						escape = false;
					}
					builder.append( c );
					
				break;
			}
		}
		if( escape ){
			builder.append( "\\\\" );  //$NON-NLS-1$
		}
		return builder.toString();
	}
}
