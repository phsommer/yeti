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

import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;

/**
 * A type which will be replaced by another type.
 * @author Benjamin Sigg
 */
public class GenericType extends AbstractType{
    private String name;
    
    public GenericType( String name ){
        this.name = name;
    }
    
    @Override
    protected String createId( Map<Type, String> putin, boolean typedefVisible ){
        if( name == null )
            return "g";
        else
            return "g("+name+")";
    }
    
    public Value cast( Value value ) {
        if( this.equals( value.getType() ))
            return value;
        
        return new UnknownValue( this );
    }

    public boolean isIncomplete(){
        return false;
    }
    
    @Override
    public GenericType asGenericType(){
        return this;
    }
    
    /*
    public void checkConversion( ConversionMap map, Type valueType, ASTMessageHandler handler, ASTNode location ) {
        if( !valueType.equals( this )){
            handler.report( Severity.WARNING, "no information about assignment of generic types", location );
        }
    }
     */
    
    public int getInitializerLength() {
        return 1;
    }
    
    public boolean isLeafInitializerType() {
        return true;
    }

    public Type getInitializerType( int index, Initializer initializer ) {
        return this;
    }
    
    public Type getInitializerType( int index, Initializer initializer, TypedefType self ){
    	return self == null ? this : self;
    }

    public void resolveNameRanges(){
        // ignore
    }
    
    public Type replace( Map<GenericType, Type> generic ) {
        Type check = generic.get( this );
        if( check == null )
            return this;
        return check;
    }

    public Value getStaticDefaultValue() {
        return null;
    }

    public int sizeOf() {
        return -1;
    }
    
    public String getName() {
        return name;
    }
    
    public String toLabel( String name, Label label ){
        if( label == Label.DECLARATION ){
            if( name == null )
                return this.name;
            else
                return this.name + " " + name;
        }
        else{
            if( name == null )
                return toString();
            else
                return toString() + " " + name;
        }
    }
    
    @Override
    public String toString() {
        return (name == null ? "null" : name) + " (generic)";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        return result;
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
