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
import tinyos.yeti.nesc12.parser.ast.elements.types.PointerType;

public class ArrayToPointer implements Conversion{
    public boolean responsible( Type source, Type destination ){
        return source.asArrayType() != null && destination.asPointerType() != null;
    }
    
    public void check( Type source, Type destination, ConversionTable table, ConversionMap map ){
        ArrayType sourceArray = source.asArrayType();
        PointerType destinationPointer = destination.asPointerType();
        
        map.pushPointer();
        table.check( sourceArray.getRawType(), destinationPointer.getRawType(), map );
        map.popPointer();
    }
}
