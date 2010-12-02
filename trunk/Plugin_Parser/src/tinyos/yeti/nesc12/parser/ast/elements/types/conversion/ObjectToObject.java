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

import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;

public class ObjectToObject implements Conversion{
    public boolean responsible( Type source, Type destination ){
        return source.asDataObjectType() != null && destination.asDataObjectType() != null;
    }
    
    public void check( Type source, Type destination, ConversionTable table, ConversionMap map ){
        DataObjectType sourceObject = source.asDataObjectType();
        DataObjectType destinationObject = destination.asDataObjectType();
        
        if( sourceObject == destinationObject )
            return;
            
        if( sourceObject.isStruct() != destinationObject.isStruct() ){
            map.reportError( "incompatible types, struct and non-struct", source, destination );
        }
        else if( sourceObject.isUnion() != destinationObject.isUnion() ){
            map.reportError( "incompatible types, union and non-union", source, destination );
        }
        
        if( sourceObject.getFieldCount() != destinationObject.getFieldCount() ){
            map.reportError( "incompatible object types, different number of fields", source, destination );
            return;
        }
        
        for( int i = 0, n = Math.min( sourceObject.getFieldCount(), destinationObject.getFieldCount() ); i<n; i++ ){
            Field sourceField = sourceObject.getField( i );
            Field destinationField = destinationObject.getField( i );
            // TODO also check bit with of fields ( int x : 2 )
            
            Name sourceName = sourceField.getName();
            Name destinationName = destinationField.getName();
            
            if(( sourceName == null && destinationName != null ) || (sourceName != null && !sourceName.equals( destinationName ))){
                map.reportError( "incompatible object types, different fields", source, destination );
                return;
            }
            
            if( sourceField.getType() != null && destinationField.getType() != null ){
                if( !table.equals( sourceField.getType(), destinationField.getType() )){
                    map.reportError( "incompatible object types, different fields", source, destination );
                    return;
                }
            }
        }
    }
}
