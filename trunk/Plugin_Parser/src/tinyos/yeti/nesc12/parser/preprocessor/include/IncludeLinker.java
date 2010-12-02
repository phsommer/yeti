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
package tinyos.yeti.nesc12.parser.preprocessor.include;

import java.util.HashMap;
import java.util.Map;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.StandardModelConnection;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.preprocessor.DirectiveLinker;
import tinyos.yeti.preprocessor.IncludeCallback;
import tinyos.yeti.preprocessor.IncludeFile;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.elements.Include;

public class IncludeLinker extends DirectiveLinker<Include> implements IncludeCallback{
	private Map<Include, IncludeFile> files = new HashMap<Include, IncludeFile>();
	
	public IncludeLinker( Parser parser ){
		super( parser );
	}
	
	public void included( Include include, IncludeFile file ){
		add( include );
		files.put( include, file );
		reference( include, include );
	}
	
	@Override
	public void transmitNodes( NodeStack stack, Range range ){
		// prevent from hapening
	}
	
	@Override
	public void transmitNodes( NodeStack stack ){
		if( hasNodesToTransmit() ){
			StandardModelNode node = new StandardModelNode( "include declarations", false, Tag.OUTLINE, NesC12ASTModel.INCLUDES, Tag.NO_BASE_EXPANSION );
			node.setLabel( "include declarations" );
			node.setParseFile( getParser().getParseFile() );
			
			stack.pushNode( node );
			stack.addLocation( getParser().resolveInputLocation( -1, -1 ) );
		
			super.transmitNodes( stack );
			stack.popNode( null );
		}
		else{
			super.transmitNodes( stack );
		}
	}
	
	@Override
	protected ModelNode toNode( Include directive, int inputOffset, NodeStack nodes ){
		return null;
	}
	
	@Override
	protected ModelConnection toConnection( Include directive, int inputOffset, NodeStack nodes ){
		IncludeFile file = files.get( directive );
		if( file == null )
			return null;
			
		StandardModelConnection connection = new StandardModelConnection( file.getFile().getPath(), null );
		connection.setTags( TagSet.get( NesC12ASTModel.UNIT ) );
		connection.setReference( true );
		connection.setLabel( file.getFile().getName() );
		connection.setReferencedPath( new ASTModelPath( ((NesC12FileInfo)file.getFile()).getParseFile(), file.getFile().getPath() ) );
		
		return connection;
	}
		
	@Override
	protected PreprocessorElement toElement( Include directive ){
		return directive;
	}
	

}
