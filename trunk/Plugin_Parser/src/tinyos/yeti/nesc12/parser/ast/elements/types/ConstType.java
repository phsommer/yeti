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

/**
 * A type encapsuling another type and adding the const modifier.
 * @author Benjamin Sigg
 *
 */
public class ConstType extends AbstractType{
    private Type type;
    
    public ConstType( Type type ){
        this.type = type;
    }
    
    @Override
    protected String createId( Map<Type, String> putin, boolean typedefVisible ){
        if( type == null )
            return "c";
        else
            return "c"+type.id( putin, typedefVisible );
    }
    
    @Override
    public ArrayType asArrayType(){
        return type.asArrayType();
    }
    @Override
    public BaseType asBase(){
        return type.asBase();
    }
    @Override
    public DataObjectType asDataObjectType(){
        return type.asDataObjectType();
    }
    @Override
    public FunctionType asFunctionType(){
        return type.asFunctionType();
    }
    @Override
    public PointerType asPointerType(){
        return type.asPointerType();
    }
    
    @Override
    public EnumType asEnumType(){
        return type.asEnumType();
    }
    
    @Override
    public ConstType asConstType(){
        return this;
    }
    
    public Value cast( Value value ) {
        return type.cast( value );
    }
    
    public int getInitializerLength() {
        return type.getInitializerLength();
    }
    
    public boolean isLeafInitializerType() {
        return type.isLeafInitializerType();
    }
    
    public Type getInitializerType( int index, Initializer initializer ) {
        return type.getInitializerType( index, initializer );
    }
    
    public Type getInitializerType( int index, Initializer initializer, TypedefType self ){
    	return type.getInitializerType( index, initializer, self );
    }
    
    public boolean isIncomplete(){
        return type.isIncomplete();
    }
    
    public Type getRawType() {
        return type;
    }
    
    public Value getStaticDefaultValue() {
        return type.getStaticDefaultValue();
    }
    
    public void resolveNameRanges(){
        if( type != null )
            type.resolveNameRanges();
    }
    
    public Type replace( Map<GenericType, Type> generic ) {
        Type raw = type.replace( generic );
        if( raw == type )
            return this;
        
        if( raw == null )
            return null;
        
        return new ConstType( raw );
    }
    public int sizeOf() {
        return type.sizeOf();
    }
    
    @Override
    public Binding getSegmentChild( int segment, int index ){
        return type;
    }
    
    @Override
    public int getSegmentCount(){
        return 1;
    }
    
    @Override
    public String getSegmentName( int segment ){
        return "raw";
    }
    
    @Override
    public int getSegmentSize( int segment ){
        return 1;
    }
    
    public String toLabel( String name, Label label ){
        return "const " + type.toLabel( name, label );
    }
    
    @Override
    public String toString() {
        return toLabel( null, Label.EXTENDED );
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
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
