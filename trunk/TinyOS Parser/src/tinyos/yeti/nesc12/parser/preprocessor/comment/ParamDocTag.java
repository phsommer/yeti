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
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.preprocessor.RangeDescription;

public class ParamDocTag extends DefaultDocTag{
	public static final IDocTagFactory FACTORY = new IDocTagFactory(){
		public IDocTag create( String name, String description, int nameOffset, int[] descriptionChars, int[] descriptionOffsets ){
			return new ParamDocTag( name, description, nameOffset, descriptionChars, descriptionOffsets );
		}
	};

	public ParamDocTag( String name, String description, int nameOffset, int[] descriptionChars, int[] descriptionOffsets ){
		super( name, description, nameOffset, descriptionChars, descriptionOffsets );
	}

	public String getParameterName(){
		String sentence = getFirstQuotedSentence();
		if( sentence == null )
			return getFirstDescriptionWord();
		
		int length = sentence.length();
		while( length > 0 && Character.isWhitespace( sentence.charAt( length-1 )))
			length--;
		
		int offset = length-1;
		while( offset >= 0 && !Character.isWhitespace( sentence.charAt( offset ) ))
			offset--;
		
		if( offset <= 0 )
			return sentence;
		
		return sentence.substring( offset+1, length );
	}
	
	public String getParameterDescription(){
		int end = getFirstQuotedSentenceEnd();
		if( end == -1 )
			end = getFirstDescriptionWordEnd();
		
		if( end == -1 )
			return getDescription();
		
		end++;
		String description = getDescription();
		int length = description.length();
		while( end < length && Character.isWhitespace( description.charAt( end ) ))
			end++;
		
		if( end >= length )
			return "";
		
		return description.substring( end );
	}
	
	public RangeDescription getRangeOfParameterName( AnalyzeStack stack ){
		String sentence = getFirstQuotedSentence();
		if( sentence == null )
			return getRangeOfFirstDescriptionWord( stack );
		
		int length = sentence.length();
		while( length > 0 && Character.isWhitespace( sentence.charAt( length-1 )))
			length--;
		
		int offset = length-1;
		while( offset >= 0 && !Character.isWhitespace( sentence.charAt( offset ) ))
			offset--;
		
		if( offset <= 0 )
			return null;
		
		// find the offset of the description in the document
		int descriptionOffset = 0;
		int nearest = -1;
		
		int[] descriptionChars = getDescriptionChars();
		int[] descriptionOffsets = getDescriptionOffsets();
		
		for( int i = 0; i < descriptionChars.length; i++ ){
			if( descriptionChars[i] <= offset && nearest < descriptionChars[i]){
				nearest = descriptionChars[i];
				descriptionOffset = descriptionOffsets[i];
			}
		}
		
		int sentenceOffset = getFirstQuotedSentenceOffset();
		descriptionOffset += sentenceOffset;
		
		return stack.getParser().resolveInputLocation( offset+descriptionOffset+1, length+descriptionOffset );
	}
	
	@Override
	protected void doResolve( ASTNode owner, AnalyzeStack stack ){
		String tagName = getParameterName();
		if( tagName == null ){
			stack.warning( "Missing name of parameter", getRange( stack ) );
		}
		else{
			FunctionDeclarator function = getFunction( owner );
			if( function != null ){
				if( !knownParameter( function, tagName, stack )){
					stack.warning( "Unknown parameter: '" + tagName + "'", getRangeOfParameterName( stack ));
				}
			}
		}
	}
	
	private FunctionDeclarator getFunction( ASTNode owner ){
		if( owner instanceof InitDeclarator ){
			Declarator declarator = ((InitDeclarator)owner).getDeclarator();
			if( declarator != null ){
				FunctionDeclarator function = declarator.getFunction();
				return function;
			}
		}
		return null;
	}
	
	private boolean knownParameter( FunctionDeclarator function, String tagName, AnalyzeStack stack ){
		Name[] names = function.resolveArgumentNames( stack );
		if( names != null ){
			for( Name name : names ){
				if( name != null ){
					if( tagName.equals( name.toIdentifier() )){
						return true;
					}
				}
			}
		}	
		return false;
	}
}
