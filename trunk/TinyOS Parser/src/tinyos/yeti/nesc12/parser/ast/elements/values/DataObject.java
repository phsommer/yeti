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
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

/**
 * An actual data object, has always a type and provides values for the fields
 * of its type.
 * @author Benjamin Sigg
 *
 */
public class DataObject extends AbstractValue{
    private DataObjectType type;
    private Value[] values;
    
    public DataObject( DataObjectType type ){
        this.type = type;
        values = new Value[ type.getFieldCount() ];
    }
    
    public DataObject( DataObjectType type, Value[] fields ){
        this.type = type;
        this.values = fields;
        
        if( fields.length != type.getFieldCount() )
            throw new IllegalArgumentException( "The size of 'fields' does not match the size that was expected when asking 'type'" );
    }

    public String getBindingType() {
        return "DataObject";
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
            case 0: return "Fields";
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
            case 0: return getFieldCount();
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
    
    public int getFieldCount(){
        return values.length;
    }
    
    public Value getValue( Identifier name ){
        for( int i = 0, n = type.getFieldCount(); i<n; i++ ){
            if( name.getName().equals( Name.toIdentifier( type.getField( i ).getName() ) ) )
                return getValue( i );
        }
        
        return null;
    }
    
    public Value getValue( String identifier ){
    	for( int i = 0, n = type.getFieldCount(); i<n; i++ ){
            if( identifier.equals( Name.toIdentifier( type.getField( i ).getName() ) ) )
                return getValue( i );
        }
        
        return null;
    }
    
    public Value getValue( int field ){
        return values[field];
    }
    
    public void setValue( int field, Value value ){
        values[field] = value;
    }
    
    public int sizeOf() {
        return type.sizeOf();
    }
    
    public void resolveNameRanges(){
        if( type != null )
            type.resolveNameRanges();
        
        if( values != null ){
            for( Value value : values ){
                if( value != null ){
                    value.resolveNameRanges();
                }
            }
        }
    }
    
    @Override
    public String toString(){
        return toLabel();
    }
    
    public String toLabel() {
        if( type == null )
            return "invalid data object";
        
        StringBuilder builder = new StringBuilder();
        builder.append( "{" );
        if( type.isUnion() ){
            if( values.length > 0 ){
                builder.append( type.getField( 0 ).getName() );
                builder.append( "=" );
                builder.append( values[0] );
            }
        }
        
        for( int i = 0, n = type.getFieldCount(); i<n; i++ ){
            builder.append( type.getField( i ).getName() );
            builder.append( "=" );
            builder.append( values[i] );
            builder.append( ";" );
            if( i+1 < n )
                builder.append( " " );
        }
        builder.append( "}" );
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
        
        if( obj instanceof DataObject ){
            DataObject object = (DataObject)obj;
            
            if( !ConversionTable.instance().equals( type, object.type ))
                return false;
            
            if( object.values == null && values == null )
                return true;
            
            if( object.values == null || values == null )
                return false;
            
            return Arrays.equals( object.values, values );
        }
        
        return false;
    }
}
