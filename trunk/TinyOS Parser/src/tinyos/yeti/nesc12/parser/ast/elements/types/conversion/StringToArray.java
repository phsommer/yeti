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
package tinyos.yeti.nesc12.parser.ast.elements.types.conversion;

import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.ArrayType;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.values.StringValue;

/**
 * Special conversion for things like <code>char name[] = "hello"</code>. 
 * @author Benjamin Sigg
 */
public class StringToArray implements Conversion{
	public boolean responsible( Type source, Type destination ){
		if( source.asPointerType() != null && destination.asArrayType() != null ){
			if( source.asPointerType().getRawType().equals( BaseType.U_CHAR )){
				if( destination.asArrayType().getRawType().equals( BaseType.U_CHAR )){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void check( Type source, Type destination, ConversionTable table, ConversionMap map ){
		Value value = map.getConvertedValue();
		if( !(value instanceof StringValue )){
			map.reportError( "Can't assign non-constant string to char-array", source, destination );
			return;
		}
		
		StringValue string = (StringValue)value;
		ArrayType.Size size = destination.asArrayType().getSize();
		if( size == ArrayType.Size.SPECIFIED ){
			if( string.getStringLength() > destination.asArrayType().getLength() ){
				map.reportError( "Array too small", source, destination );
			}
		}
	}
}
