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
package tinyos.yeti.nesc12.parser.ast.elements.values;

import java.util.Arrays;

import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.ArrayType;

public class ArrayValue extends AbstractValue{
    private ArrayType type;
    private Value[] values;
    
    public ArrayValue( ArrayType type, Value[] values ){
        this.type = type;
        this.values = values;
    }

    public String getBindingType() {
        return "Array";
    }
    
    public String getBindingValue() {
        return null;
    }
    

    @Override
    public int getSegmentCount() {
        return super.getSegmentCount() + 1;
    }
    
    @Override
    public String getSegmentName( int segment ) {
        int count = super.getSegmentCount();
        if( segment < count )
            return super.getSegmentName( segment );
        else
            segment -= count;
        
        switch( segment ){
            case 0: return "Values";
            default: return null;
        }
    }
    
    @Override
    public int getSegmentSize( int segment ) {
        int count = super.getSegmentCount();
        if( segment < count )
            return super.getSegmentSize( segment );
        else
            segment -= count;
        
        switch( segment ){
            case 0: return getLength();
            default: return 0;
        }
    }
    
    @Override
    public Binding getSegmentChild( int segment, int index ) {
        int count = super.getSegmentCount();
        if( segment < count )
            return super.getSegmentChild( segment, index );
        else
            segment -= count;
        
        switch( segment ){
            case 0: return getValue( index );
            default: return null;
        }
    }
    
    public Type getType() {
        return type;
    }
    
    public int getLength(){
        return values.length;
    }
    
    public Value getValue( int index ){
        return values[index];
    }
    
    public int sizeOf() {
        int length = type.getRawType().sizeOf();
        if( length == -1 )
            return -1;
        
        return values.length * length;
    }
    
    public void resolveNameRanges(){
        if( type != null )
            type.resolveNameRanges();
        
        if( values != null ){
            for( Value value : values ){
                if( value != null )
                    value.resolveNameRanges();
            }
        }
    }
    
    @Override
    public String toString(){
        return toLabel();
    }
    
    public String toLabel() {
        StringBuilder builder = new StringBuilder();
        builder.append( "[" );
        for( int i = 0, n = Math.min( 10, values.length ); i<n; i++ ){
            if( i != 0 )
                builder.append( ", " );
            builder.append( values[i] );
        }
        if( values.length > 10 )
            builder.append( ", ..." );
        builder.append( "]" );
        return builder.toString();
    }
    
    @Override
    public int hashCode(){
        return values == null ? null : Arrays.hashCode( values );
    }
    
    @Override
    public boolean equals( Object obj ){
        if( obj == this )
            return true;
        
        if( obj == null )
            return false;
        
        if( obj instanceof ArrayValue ){
            ArrayValue array = (ArrayValue)obj;
            if( array.values == null && values == null )
                return true;
            
            if( array.values == null || values == null )
                return false;
            
            return Arrays.equals( array.values, values );
        }
        
        return false;
    }
}
