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
package tinyos.yeti.nesc12.ep.attributes;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.values.DataObject;
import tinyos.yeti.nesc12.parser.ast.elements.values.StringValue;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Attribute;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.IAttributeResolve;

public class Combine implements IAttributeResolve{
	public void resolve( final Attribute attribute, final AnalyzeStack stack ){
		if( stack.isCreateReferences() && stack.isCreateModel() ){
			Value value = attribute.resolveValue();
			if( value == null )
				return;
			if( !(value instanceof DataObject) )
				return;
			
			DataObject obj = (DataObject)value;
			Value methodName = obj.getValue( "name" );
			if( methodName == null )
				return;
			if( !(methodName instanceof StringValue ))
				return;
			
			final String name = ((StringValue)methodName).getString();
			
			if( name != null ){
				stack.getNodeStack().executeOnPop( new Runnable(){
					public void run(){
						Field field = stack.getField( new SimpleName( null, name ) );
						stack.reference( attribute.getArguments(), field.getPath() );		
					}
				}, 1 );
			}
		}
	}
}
