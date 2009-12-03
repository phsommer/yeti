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
package tinyos.yeti.nesc12.parser.preprocessor.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.utility.IntList;

/**
 * A {@link DocAnalysis} analyzes documentation-comment and tells where 
 * which tags with what arguments are shown.
 * @author Benjamin Sigg
 */
public class DocAnalysis{
	private static Map<String, IDocTagFactory> factories;
	private static IDocTagFactory defaultFactory = DefaultDocTag.FACTORY;
	
	static{
		factories = new HashMap<String, IDocTagFactory>();
		
		factories.put( "return", ReturnDocTag.FACTORY );
		factories.put( "param", ParamDocTag.FACTORY );
	}
	
	private List<IDocTag> tags = new ArrayList<IDocTag>();
	
	/**
	 * Creates a new analysis.
	 * @param comment the comment to analyze, not <code>null</code>
	 * @param offset the offset of this comment, i.e. the position of the
	 * first character of <code>comment</code> in the document
	 */
	public DocAnalysis( String comment, int offset ){
		build( comment, offset );
	}
	
	public IDocTag[] getTags(){
		return tags.toArray( new IDocTag[ tags.size() ] );
	}
	
	public void resolve( ASTNode owner, AnalyzeStack stack ){
		for( IDocTag tag : tags ){
			tag.resolve( owner, stack );
		}
	}
	
	public String getParameterDescription( String paramName ){
		for( IDocTag tag : tags ){
			if( tag instanceof ParamDocTag ){
				ParamDocTag param = (ParamDocTag)tag;
				if( param.getParameterName().equals( paramName )){
					return param.getParameterDescription();
				}
			}
		}
		return null;
	}
	
	private void build( String comment, int offset ){
		Scanner scanner = new Scanner( comment );
		
		boolean firstLine = true;
		boolean firstTagLine = true;
		
		String tagName = null;
		int tagNameOffset = 0;
		StringBuilder description = new StringBuilder();
		
		String line;
		
		IntList characterOffsets = new IntList();
		IntList lineOffsets = new IntList();
		
		while( (line = scanner.nextLine()) != null ){
			int offsetOfText = offsetOfText( line, firstLine );
			firstLine = false;
			String nextTagName = getTagName( line, offsetOfText );
			if( nextTagName != null ){
				if( tagName != null ){
					define( tagName, tagNameOffset, description.toString(), characterOffsets.toArray(), lineOffsets.toArray() );
					characterOffsets.clear();
					lineOffsets.clear();
				}
				description.setLength( 0 );
				tagName = nextTagName;
				tagNameOffset = offsetOfText+1+offset+scanner.currentOffset();
				offsetOfText += tagName.length() + 2;
				firstTagLine = true;
			}
			
			if( tagName != null ){
				if( offsetOfText < line.length() ){
					String newText = null;
					int end = getCommentEnd( line );
					if( end == -1 ){
						newText = line.substring( offsetOfText );
					}
					else if( end > offsetOfText ){
						newText = line.substring( offsetOfText, end );
					}
					
					if( newText != null ){
						if( firstTagLine )
							firstTagLine = false;
						else
							description.append( "\n" );
						
						characterOffsets.add( scanner.currentOffset() + offsetOfText + offset );
						lineOffsets.add( description.length() );
						description.append( newText );
					}
				}
			}
		}
		
		if( tagName != null ){
			define( tagName, tagNameOffset, description.toString(), characterOffsets.toArray(), lineOffsets.toArray() );
		}
	}
	
	private IDocTagFactory getFactory( String name ){
		IDocTagFactory factory = factories.get( name );
		if( factory == null )
			return defaultFactory;
		return factory;
	}
	
	private void define( String tagName, int tagNameOffset, String description, int[] characterOffsets, int[] lineOffsets ){
		IDocTag tag = getFactory( tagName ).create( tagName, description, tagNameOffset, lineOffsets, characterOffsets );
		tags.add( tag );
	}
	
	private int offsetOfText( String line, boolean firstLine ){
		int index = 0;
		int length = line.length();
		
		while( index < length && Character.isWhitespace( line.charAt( index )))
			index++;
		
		if( firstLine ){
			if( !line.startsWith( "/**", index ))
				return index;
			index += 3;
		}
		else{
			if( !line.startsWith( "*", index ))
				return index;
			index += 1;
		}
		
		while( index < length && Character.isWhitespace( line.charAt( index ) ))
			index++;
		
		return index;
	}
	
	private String getTagName( String line, int offset ){
		if( offset < line.length() ){
			if( line.charAt( offset ) == '@' ){
				int end = offset+1;
				while( end < line.length() && !Character.isWhitespace( line.charAt( end ) )){
					end++;
				}
				if( end > offset+1 ){
					return line.substring( offset+1, end );
				}
			}
		}
		return null;
	}
 
	private int getCommentEnd( String line ){
		return line.indexOf( "*/" );
	}
	
	private static class Scanner{
		private String input;
		private int offset = 0;
		private int end = 0;
		
		public Scanner( String input ){
			this.input = input;
		}
		
		public String nextLine(){
			offset = end;
			int inputLength = input.length();
			while( offset < inputLength && isNewline( input.charAt( offset ))){
				offset++;
			}
			
			end = offset;
			while( end < inputLength && !isNewline( input.charAt( end ) )){
				end++;
			}
			
			if( end == offset ){
				return null;
			}
			
			return input.substring( offset, end );
		}
		
		private boolean isNewline( char c ){
			return c == '\n' || c == '\r' || c == '\u2028' || c == '\u2029' || c == '\u0085';
		}
		
		public int currentOffset(){
			return offset;
		}
	}
}
