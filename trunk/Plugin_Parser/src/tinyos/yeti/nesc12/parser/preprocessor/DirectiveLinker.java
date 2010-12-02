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
package tinyos.yeti.nesc12.parser.preprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.preprocessor.FileInfo;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * This class collects some directives of the preprocessor and provides
 * facilities to create {@link IASTModelNode}s and {@link IASTReference}s.
 * @author Benjamin Sigg
 *
 * @param <D> the kind of element collected in this linker
 */
public abstract class DirectiveLinker<D>{
	private Parser parser;
	
	private List<D> directives = new ArrayList<D>();
	private List<D> unprocessed = new ArrayList<D>();
	
	private Map<PreprocessorElement, D> references;
	private Map<D, IASTModelPath> directivePaths;
	
	public DirectiveLinker( Parser parser ){
		this.parser = parser;
		if( parser.isCreateReferences() ){
			references = new HashMap<PreprocessorElement, D>();
			directivePaths = new HashMap<D, IASTModelPath>();
		}
	}
	
	public Parser getParser(){
		return parser;
	}
	
	public void add( D directive ){
		directives.add( directive );
	}
	
	public void reference( D directive, PreprocessorElement referencing ){
		if( references != null ){
			references.put( referencing, directive );
		}
	}
	
	/**
	 * To be called after the preprocessor has finished working.
	 */
	public void preprocessorDone(){
		unprocessed = new ArrayList<D>( directives );
	}
	
	public void transmitReferences( AnalyzeStack stack ){
		for( Map.Entry<PreprocessorElement, D> reference : references.entrySet() ){
			IASTModelPath path = directivePaths.get( reference.getValue() );
			if( path != null ){
				RangeDescription description = parser.resolveLocation( false, reference.getKey() );
				stack.reference( description, path );
			}
		}
	}
	
	/**
	 * Transmits all macros defined in <code>range</code>.
	 * @param stack the destination
	 * @param range the range in which the macros are defined
	 */
	public void transmitNodes( NodeStack stack, tinyos.yeti.nesc12.parser.ast.Range range ){
		ListIterator<D> iterator = unprocessed.listIterator();
		int left = range.getLeft();
		int right = range.getRight();
		
		left = parser.getPreprocessorReader().inputLocation( left, true );
		right = parser.getPreprocessorReader().inputLocation( right, false );
		
		while( iterator.hasNext() ){
			D directive = iterator.next();
			
			int offset = getOffset( directive );
			if( offset == -1 ){
				iterator.remove();
			}
			else if( left <= offset && right >= offset ){
				iterator.remove();
				transmitNodes( stack, directive, offset );
			}
		}
	}
	
	public void transmitNodes( NodeStack stack ){
		for( D directive : unprocessed ){
			transmitNodes( stack, directive, getOffset( directive ) );
		}
		unprocessed.clear();
	}
	
	public boolean hasNodesToTransmit(){
		return !unprocessed.isEmpty();
	}
	
	/**
	 * Gets the offset of <code>directive</code> in the input text.
	 * @param directive some directive
	 * @return the offset or -1
	 */
	protected int getOffset( D directive ){
		PreprocessorElement element = toElement( directive );
		if( element == null )
			return -1;
		
		PreprocessorToken token = element.getToken();
		if( token == null )
			return -1;
			
		return token.getBeginLocation();
	}
	
	protected abstract PreprocessorElement toElement( D directive );
	
	protected abstract ModelNode toNode( D directive, int inputOffset, NodeStack nodes );
	
	protected abstract ModelConnection toConnection( D directive, int inputOffset, NodeStack nodes );
	
	private void transmitNodes( NodeStack stack, final D directive, int inputOffset ){
		IParseFile foundFile = getFile( directive );
		if( foundFile == null )
			return;
		
		if( !foundFile.equals( stack.getAnalyzeStack().getParseFile() )){
			return;
		}
		
		RangeDescription range = parser.resolveLocation( true, toElement( directive ) );
		
		final ModelNode node = toNode( directive, inputOffset, stack );
		if( node != null ){
			if( directivePaths != null ){
				stack.executeOnPop( new Runnable(){
					public void run(){
						directivePaths.put( directive, node.getPath() );	
					}
				}, 0 );
			}
			
			stack.addChild( node, null );
			stack.pushNode( node );
			stack.addLocation( range );
			stack.popNode( null );
		}
		
		final ModelConnection connection = toConnection( directive, inputOffset, stack );
		if( connection != null ){
			if( directivePaths != null && connection.isReference() ){
				stack.executeOnPop( new Runnable(){
					public void run(){
						directivePaths.put( directive, connection.getReferencedPath() );
					}
				}, 0 );
			}
			
			stack.addConnection( connection );
			stack.pushNode( null );
			stack.setConnection( connection );
			stack.addLocation( range );
			stack.popNode( null );
		}
	}
	
	private IParseFile getFile( D directive ){
		PreprocessorElement location = toElement( directive );
		if( location == null )
			return null;
		
		PreprocessorToken token = location.getToken();
		if( token == null )
			return null;
		
		FileInfo file = token.getFile();
		if( file instanceof NesC12FileInfo ){
			return ((NesC12FileInfo)file).getParseFile();
		}
		return null;
	}
}
