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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

/**
 * Helper class to process nesc-doc.
 * @author Benjamin Sigg
 */
public class NesCDocPresenter {
	/**
	 * Transforms a nesc-doc comment into a HTML string.
	 * @param comment the comment
	 * @return the comment as html document
	 */
	public String toHTML( String comment ){
		TextPresentation presentation = new TextPresentation();
		comment = updatePresentation( comment, presentation );
		return toHTML( comment, presentation );
	}
	
	@SuppressWarnings( "unchecked" )
	public String toHTML( String comment, TextPresentation presentation ){
		List<MarkupTag> tags = new ArrayList<MarkupTag>();

		// handle tags in comment
		insertBreaks( comment, tags );
		
		// handle style
		Iterator<StyleRange> iterator = presentation.getAllStyleRangeIterator();
		while( iterator.hasNext() ){
			StyleRange next = iterator.next();
			if( (next.fontStyle & SWT.BOLD) != 0 ){
				tags.add( new MarkupTag( "<b>", next.start ) );
				tags.add( new MarkupTag( "</b>", next.start + next.length ) );
			}
		}
		
		Collections.sort( tags );

		StringBuilder html = new StringBuilder();
		int offset = 0;
		
		for( MarkupTag tag : tags ){
			int start = tag.offset;
			if( offset < start )
				html.append( comment.substring( offset, start ) );
			html.append( tag.tag );
			offset = start;
		}
		
		if( offset < comment.length() )
			html.append( comment.substring( offset ) );
		
		return html.toString();
	}
	
	private void insertBreaks( String comment, Collection<MarkupTag> tags ){
		int offset = 0;
		int length = comment.length();
		
		while( offset < length ){
			if( comment.charAt( offset ) == '@' ){
				int start = offset;
				offset++;
				int end = offset;
				while( end < length && Character.isLetter( comment.charAt( end ) ))
					end++;
				
				String sub = comment.substring( offset, end );
				offset = end;
				
				if( "author".equals( sub ) || "param".equals( sub ) || "return".equals( sub ) || "date".equals( sub ) || "see".equals( sub ) ){
					tags.add( new MarkupTag( "<br>", start ) );
				}
			}
			offset++;
		}
	}
	
	private final class MarkupTag implements Comparable<MarkupTag>{
		public final int offset;
		public final String tag;
		
		public MarkupTag( String tag, int offset ){
			this.tag = tag;
			this.offset = offset;
		}
		
		public int compareTo( MarkupTag o ){
			if( offset < o.offset )
				return -1;
			if( offset > o.offset )
				return 1;
			return 0;
		}
	}
	
	/**
	 * Transforms a comment as it was read from the file into a string without
	 * unnecessary line breaks and * signs. Fills <code>presentation</code>
	 * with the important regions to mark.
	 * @param comment some comment
	 * @param presentation how to present the result
	 * @return the cleaned up comment
	 */
	public String updatePresentation( String comment, TextPresentation presentation ){
		String info = cleanUp( comment );
		int length = info.length();

		int index = 0;

		// search for @xyz and mark
		int begin = -1;
		while( index < length ){
			if( info.charAt( index ) == '@' && (index == 0 || Character.isWhitespace( info.charAt( index-1 ) ))){
				begin = index;
			}
			else if( begin != -1 ){
				if( !Character.isLetter( info.charAt( index ) )){
					presentation.addStyleRange( new StyleRange( begin, index-begin, null, null, SWT.BOLD ) );
					begin = -1;
				}
			}
			index++;
		}

		return info;
	}
	
	private String cleanUp( String hoverInfo ){
		StringBuilder builder = new StringBuilder();
		Scanner scanner = new Scanner( hoverInfo );
		
		boolean first = true;
		
		while( scanner.hasNextLine() ){
			String line = scanner.nextLine();
			line = cut( line );
			if( line != null ){
				if( first )
					first = false;
				else
					builder.append( "\n" );
				builder.append( line );
			}
		}
		return builder.toString();
	}
	
	private String cut( String line ){
		int begin = beginAt( line );
		int end = endAt( line );
		
		if( begin >= end )
			return null;
		
		if( begin == 0 && end == line.length() ){
			return line;
		}
		
		return line.substring( begin, end );
	}
	
	private int beginAt( String line ){
		int result = 0;
		
		for( int i = 0, n = line.length(); i<n; i++ ){
			char c = line.charAt( i );
			if( c == '*' ){
				result = i+1;
				break;
			}
			
			if( c == '/' && i+2 < line.length() ){
				if( line.charAt( i+1 ) == '*' && line.charAt( i+2 ) == '*' ){
					result = i+3;
					break;
				}
			}
			
			if( !Character.isWhitespace( c )){
				result = i;
			}
		}
		
		if( result < line.length() && Character.isWhitespace( line.charAt( result ) ))
			result++;
		
		return result;
	}
	
	private int endAt( String line ){
		for( int i = line.length()-1; i >= 1; i-- ){
			char c1 = line.charAt( i-1 );
			char c2 = line.charAt( i );
			
			if( c1 == '*' && c2 == '/' )
				return i-1;
			
			if( !Character.isWhitespace( c2 ))
				return line.length();
		}
		return line.length();
	}
}
