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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.preferences.PreferenceConstants;

/**
 * Standard formatting strategy for nesc.
 * @author Benjamin Sigg
 */
public class NesCFormattingStrategy2 extends ContextBasedFormattingStrategy{
	private LinkedList<IFormattingContext> formattingContexts = new LinkedList<IFormattingContext>();
	private IFormattingContext currentContext;
	
	@Override
	public void formatterStarts( IFormattingContext context ){
		super.formatterStarts( context );
		formattingContexts.add( context );
	}
	
	@Override
	public void formatterStops(){
		super.formatterStops();
		formattingContexts.removeLast();
	}
	
	@Override
	public void format(){
		super.format();
		currentContext = formattingContexts.getLast();
		
		IDocument document = (IDocument)currentContext.getProperty( FormattingContextProperties.CONTEXT_MEDIUM );
		boolean wholeDocument = Boolean.TRUE.equals( currentContext.getProperty( FormattingContextProperties.CONTEXT_DOCUMENT ) );
		IRegion region = null;
		if( wholeDocument )
			region = new Region( 0, document.getLength() );
		else
			region = (IRegion)currentContext.getProperty( FormattingContextProperties.CONTEXT_REGION );
		
		format( document, region );
	}
	
	private String getTab(){
		IPreferenceStore store = TinyOSPlugin.getDefault().getPreferenceStore();
		if( store.getBoolean( PreferenceConstants.USE_TABS ) ){
			return "\t";
		}
		else{
			return PreferenceConstants.spacesPerTab();
		}
	}
	
	private String getIndent( String tab, int indent, int space, List<String> indents ){
		String replace = getIndent( tab, indent, indents );
		for( int i = 0; i < space; i++ ){
			replace += " ";
		}
		return replace;
	}
	
	private String getIndent( String tab, int indent, List<String> indents ){
		int size = indents.size();
		if( size == 0 ){
			indents.add( "" );
			size++;
		}
		
		while( size <= indent ){
			indents.add( indents.get( size-1 ) + tab );
			size++;
		}
		
		return indents.get( Math.max( indent, 0 ) );
	}
	
	private class Delta{
		public int indent;
		public int space;
	}
	
	private Delta indentDelta( String found, String tab, int expectedIndent, int expectedSpace ){
		int indents = 0;
		int spaces = 0;
		
		while( found.length() > 0 ){
			if( found.startsWith( tab ) ){
				found = found.substring( tab.length() );
				indents++;
			}
			else if( found.startsWith( " " )){
				found = found.substring( 1 );
				spaces++;
			}
			else{
				break;
			}
		}
		
		Delta delta = new Delta();
		delta.indent = indents - expectedIndent;
		delta.space = spaces - expectedSpace;
		return delta;
	}
	
	private void format( IDocument document, IRegion region ){
		IndentScanner scanner = new IndentScanner( document );
		String tab = getTab();
		List<String> indents = new ArrayList<String>();
		
		try{
			int begin = document.getLineOfOffset( region.getOffset() );
			int end = document.getLineOfOffset( region.getOffset() + region.getLength() );
			
			String previous = getPreviousWhitespacePrefix( document, begin );
			
			int indentDelta = 0;
			int spaceDelta = 0;
			
			if( previous != null ){
				int indent = scanner.getIndent( begin-1 );
				int space = scanner.getSpace( begin-1 );
				Delta delta = indentDelta( previous, tab, indent, space );
				indentDelta = delta.indent;
				spaceDelta = delta.space;
			}
			
			for( int line = begin; line <= end; line++ ){
				int lineOffset = document.getLineOffset( line );
				int lineLength = document.getLineLength( line );
				
				int whitespace = 0;
				while( whitespace < lineLength && Character.isWhitespace( document.getChar( whitespace + lineOffset ) ))
					whitespace++;
				
				if( whitespace == lineLength ){
					char c;
					whitespace--;
					while( whitespace >= 0 ){
						c = document.getChar( whitespace + lineOffset );
						if( c == '\r' || c == '\n' ){
							whitespace--;
						}
						else{
							break;
						}
					}
					
					if( whitespace > 0 ){
						document.replace( lineOffset, whitespace, "" );
					}
				}
				else{
					int indent = scanner.getIndent( line ) + indentDelta;
					int space = scanner.getSpace( line ) + spaceDelta;
					String replace = getIndent( tab, indent, space, indents );
					document.replace( lineOffset, whitespace, replace );
				}
			}
		}
		catch( BadLocationException e ){
			TinyOSPlugin.log( e );
		}
	}
	
	private String getPreviousWhitespacePrefix( IDocument document, int line ) throws BadLocationException{
		for( int i = line-1; i >= 0; i-- ){
			int offset = document.getLineOffset( i );
			int length = document.getLineLength( i );
			
			int count = 0;
			while( count < length && Character.isWhitespace( document.getChar( offset+count ) ))
				count++;
			
			if( count < length ){
				return document.get( offset, count );
			}
		}
		return null;
	}
}
