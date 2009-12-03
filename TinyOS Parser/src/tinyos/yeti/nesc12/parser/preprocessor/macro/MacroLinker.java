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
package tinyos.yeti.nesc12.parser.preprocessor.macro;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.preprocessor.DirectiveLinker;
import tinyos.yeti.preprocessor.MacroCallback;
import tinyos.yeti.preprocessor.lexer.Macro;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * A {@link MacroLinker} collects macros and stores their position for
 * later linking and inclusion into the AST.
 * @author Benjamin Sigg
 */
public class MacroLinker extends DirectiveLinker<Macro> implements MacroCallback{
	public MacroLinker( Parser parser ){
		super( parser );
	}
	
	public void declared( Macro macro ){
		add( macro );
	}
	
	public void applied( Macro macro, PreprocessorElement identifier ){
		reference( macro, identifier );
	}
		
	public void undeclared( String name, Macro macro ){
		// ignore
	}
	
	@Override
	protected PreprocessorElement toElement( Macro macro ){
		return macro.getLocation();
	}
	
	@Override
	protected ModelNode toNode( Macro macro, int inputOffset, NodeStack nodes ){
		TagSet tags = new TagSet();
		tags.add( Tag.MACRO );
		
		if( nodes.size() <= 1 )
			tags.add( Tag.OUTLINE );
		
		tags.add( Tag.IDENTIFIABLE );
		
		final StandardModelNode node = new StandardModelNode( inputOffset + "_" + macro.getName(), true );
		node.setTags( tags );
		
		node.setNodeName( macro.getName() );
		
		String[] parameters = macro.getParameters();
		StringBuilder name = new StringBuilder();
		name.append( macro.getName() );
		
		if( parameters != null ){
			boolean first = true;
			name.append( "(" );
			for( String parameter : parameters ){
				if( first )
					first = false;
				else
					name.append( ", " );
				name.append( parameter );
			}
			
			switch( macro.getVarArg() ){
				case YES_UNNAMED:
					if( !first )
						name.append( ", " );
					
				case YES_NAMED:
					name.append( "..." );
			}
			
			name.append( ")" );
		}
		
		node.setLabel( name.toString() );
		return node;
	}
	
	@Override
	protected ModelConnection toConnection( Macro directive, int inputOffset, NodeStack nodes ){
		return null;
	}
}
