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
package tinyos.yeti.nesc12.parser.ast.elements.types;

import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;

/**
 * Class that contains helpful methods to deal with types
 * @author Benjamin Sigg
 */
public final class TypeUtility{
    private TypeUtility(){
        // ignore
    }
    
    public static boolean equals( Type a, Type b ){
        return ConversionTable.instance().equals( a, b );
    }
    
    public static FunctionType function( Type type ){
        if( type == null )
            return null;
        
        return type.asFunctionType();
    }
    
    public static Type result( FunctionType type ){
        if( type == null )
            return null;
        
        return type.getResult();
    }
    
    public static PointerType pointer( Type type ){
        if( type == null )
            return null;
        
        return type.asPointerType();
    }
    
    public static ArrayType array( Type type ){
        if( type == null )
            return null;
        
        return type.asArrayType();
    }
    
    public static Type raw( PointerType type ){
        if( type == null )
            return null;
        
        return type.getRawType();
    }

    public static Type raw( ArrayType type ){
        if( type == null )
            return null;
        
        return type.getRawType();
    }
    
    public static BaseType base( Type type ){
        if( type == null )
            return null;
        
        return type.asBase();
    }
    
    public static DataObjectType object( Type type ){
        if( type == null )
            return null;
        
        return type.asDataObjectType();
    }
    
    public static String toAstNodeLabel( Type type ){
        TypedefType typedef = type.asTypedefType();
        if( typedef != null ){
            return typedef.getName() + " - " + typedef.getBase().toLabel( null, Type.Label.SMALL );
        }
        
        return type.toLabel( null, Type.Label.EXTENDED );
    }
}
