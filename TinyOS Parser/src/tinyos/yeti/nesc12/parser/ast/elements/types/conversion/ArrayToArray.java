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
import tinyos.yeti.nesc12.parser.ast.elements.types.ArrayType;
import tinyos.yeti.nesc12.parser.ast.elements.types.ArrayType.Size;

public class ArrayToArray implements Conversion{
    public boolean responsible( Type source, Type destination ){
        return source.asArrayType() != null && destination.asArrayType() != null;
    }
    
    public void check( Type source, Type destination, ConversionTable table, ConversionMap map ){
        ArrayType sourceArray = source.asArrayType();
        ArrayType destinationArray = destination.asArrayType();
        
        boolean sSpecified = sourceArray.getSize() == Size.SPECIFIED;
        boolean sUnSpecified = sourceArray.getSize() == Size.SPECIFIED_UNKNOWN;
        
        boolean dSpecified = destinationArray.getSize() == Size.SPECIFIED;
        boolean dUnSpecified = destinationArray.getSize() == Size.SPECIFIED_UNKNOWN;
        
        if( sSpecified || sUnSpecified ){
            if( dSpecified || dUnSpecified ){
            	if( sUnSpecified || dUnSpecified ){
            		map.reportWarning( "arrays might be of different size", source, destination );
            	}
            	else if( destinationArray.getLength() != sourceArray.getLength() ){
                    map.reportError( "incompatible size of arrays", source, destination );
                }
            }
            else{
                map.reportError( "incompatible size of arrays", source, destination );
            }
        }
        
        table.check( sourceArray.getRawType(), destinationArray.getRawType(), map );
    }
}
