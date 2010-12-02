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

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.preprocessor.RangeDescription;

public class DefaultDocTag implements IDocTag{
	public static final IDocTagFactory FACTORY = new IDocTagFactory(){
		public IDocTag create( String name, String description, int nameOffset, int[] descriptionChars, int[] descriptionOffsets ){
			return new DefaultDocTag( name, description, nameOffset, descriptionChars, descriptionOffsets );
		}
	};
	
	private String name;
	private String description;
	private int nameOffset;
	private int[] descriptionChars;
	private int[] descriptionOffsets;
	private boolean resolved = false;
	
	public DefaultDocTag( String name, String description, int nameOffset, int[] descriptionChars, int[] descriptionOffsets ){
		this.name = name;
		this.description = description;
		this.nameOffset = nameOffset;
		this.descriptionChars = descriptionChars;
		this.descriptionOffsets = descriptionOffsets;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public int getNameOffset(){
		return nameOffset;
	}
	
	public int[] getDescriptionChars(){
		return descriptionChars;
	}
	
	public int[] getDescriptionOffsets(){
		return descriptionOffsets;
	}
	
	public final void resolve( ASTNode owner, AnalyzeStack stack ){
		if( !resolved ){
			resolved = true;
			doResolve( owner, stack );
		}
	}
	
	protected void doResolve( ASTNode owner, AnalyzeStack stack ){
		// nothing
	}
	
	/**
	 * Gets the first word of the description, using whitespaces as 
	 * separator characters.
	 * @return the first word or <code>null</code>
	 */
	public String getFirstDescriptionWord(){
		if( description == null )
			return null;
		
		int length = description.length();
		int offset = 0;
		int end = 0;
		
		while( offset < length && Character.isWhitespace( description.charAt( offset ) ))
			offset++;
		
		end = offset;
		while( end < length && !Character.isWhitespace( description.charAt( end ) ))
			end++;
		
		if( offset == end )
			return null;
		
		return description.substring( offset, end );
	}
	
	public int getFirstDescriptionWordOffset(){
		if( description == null )
			return -1;
		
		int length = description.length();
		int offset = 0;
		
		while( offset < length && Character.isWhitespace( description.charAt( offset ) ))
			offset++;
		
		return offset;
	}
	
	public int getFirstDescriptionWordEnd(){
		int offset = getFirstDescriptionWordOffset();
		if( offset == -1 )
			return -1;
		
		int length = description.length();
		int end = offset;
		while( end < length && !Character.isWhitespace( description.charAt( end ) ))
			end++;
		
		return end-1;
	}
	
	/**
	 * If the description starts with a quoted sentence, then this
	 * sentence (without the quotes) is returned.
	 * @return the quoted sentence or <code>null</code> if not existing
	 */
	public String getFirstQuotedSentence(){
		if( description == null )
			return null;
	
		int offset = getFirstQuotedSentenceOffset();
		if( offset == -1 )
			return null;
		
		int length = description.length();
		int end = 0;
		
		end = offset;
		while( end < length && description.charAt( end ) != '\'' )
			end++;
		
		if( end >= length )
			return null;
		
		return description.substring( offset, end );
	}
	
	public int getFirstQuotedSentenceOffset(){
		if( description == null )
			return -1;
		
		int length = description.length();
		int offset = 0;
		
		while( offset < length && Character.isWhitespace( description.charAt( offset ) ))
			offset++;
		
		
		if( description.charAt( offset ) != '\'' )
			return -1;
		
		return offset+1;
	}
	
	public int getFirstQuotedSentenceEnd(){
		int offset = getFirstQuotedSentenceOffset();
		if( offset == -1 )
			return -1;
		
		int length = description.length();
		int end = offset;
		while( end < length && description.charAt( end ) != '\'' )
			end++;
		
		return end-1;
	}
	
	protected RangeDescription getRange( AnalyzeStack stack ){
		return stack.getParser().resolveInputLocation( nameOffset, nameOffset+name.length() );
	}
	
	protected RangeDescription getRangeOfFirstDescriptionWord( AnalyzeStack stack ){
		if( description == null )
			return null;
		
		// find the offset of the word in the description
		int length = description.length();
		int offset = 0;
		int end = 0;
		
		while( offset < length && Character.isWhitespace( description.charAt( offset ) ))
			offset++;
		
		end = offset;
		while( end < length && !Character.isWhitespace( description.charAt( end ) ))
			end++;
		
		if( offset == end )
			return null;
		
		// find the offset of the description in the document
		int descriptionOffset = 0;
		int nearest = -1;
		
		for( int i = 0; i < descriptionChars.length; i++ ){
			if( descriptionChars[i] <= offset && nearest < descriptionChars[i]){
				nearest = descriptionChars[i];
				descriptionOffset = descriptionOffsets[i];
			}
		}
		
		return stack.getParser().resolveInputLocation( offset+descriptionOffset, end+descriptionOffset );
	}
}
