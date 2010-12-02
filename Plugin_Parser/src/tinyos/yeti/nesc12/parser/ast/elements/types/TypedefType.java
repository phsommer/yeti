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

import java.util.HashMap;
import java.util.Map;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.inspection.INesCTypedef;
import tinyos.yeti.ep.parser.inspection.InspectionKey;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;

public class TypedefType implements Type, INesCTypedef{
    private String name;
    private Type base;
    
    public TypedefType( String name, Type base ){
        this.name = name;
        this.base = base;
    }

    public String id( boolean typedefVisible ){
        return id( new HashMap<Type, String>(), typedefVisible );
    }
    
    public String id( Map<Type, String> putin, boolean typedefVisible ){
        String id = putin.get( this );
        if( id == null ){
            if( base == null )
                id = "t";
            else if( typedefVisible )
                id = name;
            else
                id = base.id( putin, typedefVisible );
            
            putin.put( this, id );
        }
        return id;
    }
    
    public IASTModelNodeConnection asConnection(){
    	return null;
    }
    
    public IASTModelNode asNode(){
    	return null;
    }
    
    public int getReferenceKindCount(){
    	return 0;
    }
    
    public InspectionKey<?> getReferenceKind( int index ){
    	return null;
    }
    
    public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector ){
    	return null;
    }
    
    public String getTypedefName(){
    	return getName();
    }
    
    public String getTypedefType(){
    	if( base == null )
    		return null;
    	return base.toLabel( null, Label.DECLARATION );
    }

    public String getNesCDescription(){
    	StringBuilder builder = new StringBuilder();
    	if( name != null ){
    		builder.append( name );
    		builder.append( ": " );
    	}
    	if( base != null ){
    		builder.append( base.toLabel( null, Label.DECLARATION ) );
    	}
    	return builder.toString();
    }
    
    public Type getBase(){
        return base;
    }
    
    public Value cast( Value value ){
        return base.cast( value );
    }

    public int getInitializerLength(){
        return base.getInitializerLength();
    }

    public boolean isLeafInitializerType() {
        return base.isLeafInitializerType();
    }
    
    public Type getInitializerType( int index, Initializer initializer ) {
    	return base.getInitializerType( index, initializer, this );
    }
    
    public Type getInitializerType( int index, Initializer initializer, TypedefType self ){
    	return base.getInitializerType( index, initializer, self == null ? this : self );
    }
    
    public Value getStaticDefaultValue(){
        return base.getStaticDefaultValue();
    }
    
    public ArrayType asArrayType(){
        return base.asArrayType();
    }
    
    public BaseType asBase(){
        return base.asBase();
    }
    
    public DataObjectType asDataObjectType(){
        return base.asDataObjectType();
    }
    
    public FunctionType asFunctionType(){
        return base.asFunctionType();
    }
    
    public PointerType asPointerType(){
        return base.asPointerType();
    }
    
    public EnumType asEnumType(){
        return base.asEnumType();
    }
    
    public ConstType asConstType(){
        return base.asConstType();
    }
    
    public GenericType asGenericType(){
        return base.asGenericType();
    }
    
    public TypedefType asTypedefType() {
    	return this;
    }
    
    public Type replace( Map<GenericType, Type> generic ){
        Type newBase = base.replace( generic );
        if( newBase == base )
            return this;
        if( newBase == null )
            return null;
        
        if( base.asGenericType() != null && base.asTypedefType() == null )
        	return newBase;
        
        return new TypedefType( name, newBase );
    }
    
    public boolean isIncomplete(){
        return base.isIncomplete();
    }

    public int sizeOf(){
        return base.sizeOf();
    }

    public Type asType(){
        return this;
    }

    public Value asValue(){
        return null;
    }

    public void resolveNameRanges(){
        base.resolveNameRanges();
    }

    public String getBindingType(){
        return "Type";
    }

    public String getBindingValue(){
        return toString();
    }

    public Binding getSegmentChild( int segment, int index ){
        return base;
    }

    public int getSegmentCount(){
        return 1;
    }

    public String getSegmentName( int segment ){
        return "raw";
    }

    public int getSegmentSize( int segment ){
        return 1;
    }
    
    public String getName(){
        return name;
    }
    
    public String toLabel( String name, Label label ){
        if( label == Label.SMALL || label == Label.DECLARATION ){
            if( name == null ){
                return this.name;
            }
            else{
                return this.name + " " + name;
            }
        }
        else{
            return base.toLabel( name, label );
        }
    }
    
    @Override
    public String toString(){
        return name;
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
