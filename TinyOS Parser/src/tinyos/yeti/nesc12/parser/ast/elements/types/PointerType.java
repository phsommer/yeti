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

import java.util.Map;

import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;

public class PointerType extends AbstractType{
    private Type raw;
    private boolean nameResolving;
    
    public PointerType( Type raw ){
        this.raw = raw;
    }

    @Override
    protected String createId( Map<Type, String> putin, boolean typedefVisible ){
        if( raw == null )
            return "p";
        else
            return "p"+raw.id( putin, typedefVisible );
    }
    
    public Type getRawType(){
        return raw;
    }
    
    public boolean isIncomplete(){
        return false;
    }
    
    @Override
    public PointerType asPointerType(){
        return this;
    }
    
    public void resolveNameRanges(){
        if( !nameResolving ){
            try{
                nameResolving = true;
                if( raw != null )
                    raw.resolveNameRanges();
            }
            finally{
                nameResolving = false;
            }
        }
    }
    
    public Type replace( Map<GenericType, Type> generic ) {
        if( raw == null )
            return this;
        
        Type check = raw.replace( generic );
        if( check == raw )
            return this;
        
        return new PointerType( check );
    }
    
    public int getInitializerLength() {
        return 1;
    }
    
    public boolean isLeafInitializerType() {
        return true;
    }
    
    public Type getInitializerType( int index, Initializer initializer ) {
        if( index == 0 )
            return this;
        return null;
    }
    
    public Type getInitializerType( int index, Initializer initializer, TypedefType self ){
    	if( index == 0 ){
    		return self == null ? this : self;
    	}
    	return null;
    }
    
    public int sizeOf() {
        return BaseType.U_INT.sizeOf();
    }
    
    public Value cast( Value value ) {
        Type valueType = value.getType();
        
        if( valueType == null )
        	return new UnknownValue( this );
        
        if( valueType.equals( this ))
            return value;
        
        if( valueType.asBase() != null ){
            switch( valueType.asBase() ){
                case U_CHAR:
                case S_CHAR:
                case U_INT:
                case S_INT:
                case U_LONG:
                case S_LONG:
                case U_LONG_LONG:
                case S_LONG_LONG:
                case U_SHORT:
                case S_SHORT:
                    return BaseType.U_INT.cast( value );
            }
        }
        
        return new UnknownValue( this );
    }
    
    /*
    public void checkConversion( ConversionMap map, Type valueType, ASTMessageHandler handler, ASTNode location ) {
        if( valueType.asPointerType() != null ){
            map.pushPointer();
            PointerType pointer = valueType.asPointerType();
            raw.checkConversion( map, pointer.getRawType(), handler, location );
            map.popPointer();
            return;
        }
        
        if( valueType.asBase() != null ){
            switch( valueType.asBase() ){
                case U_CHAR:
                case S_CHAR:
                case U_INT:
                case S_INT:
                case U_LONG:
                case S_LONG:
                case U_LONG_LONG:
                case S_LONG_LONG:
                case U_SHORT:
                case S_SHORT:
                    handler.report( Severity.WARNING, "assigning number value to pointer without conversion", location );
                    return;
            }
        }
        
        handler.report( map.getErrorSeverity(), "assigning incompatible pointer types", location );
    }*/
    
    public Value getStaticDefaultValue() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Binding getSegmentChild( int segment, int index ){
        return raw.getSegmentChild( segment, index );
    }
    
    @Override
    public int getSegmentCount(){
        return raw.getSegmentCount();
    }
    
    @Override
    public String getSegmentName( int segment ){
        return raw.getSegmentName( segment );
    }
    
    @Override
    public int getSegmentSize( int segment ){
        return raw.getSegmentSize( segment );
    }
    
    public String toLabel( String name, Label label ){
        if( name == null )
            return raw.toLabel( "*", label );
        
        if( raw.asArrayType() != null )
            return raw.toLabel( "(*" + name + ")", label );
        
        return raw.toLabel( "*" + name, label );
    }
    
    @Override
    public String toString() {
        return toLabel( null, Label.EXTENDED );
    }
    

    @Override
    public boolean equals( Object obj ) {
        if( this == obj )
            return true;
        if( !(obj instanceof Type ))
            return false;
        return ConversionTable.instance().equals( this, (Type)obj );
    }
}
