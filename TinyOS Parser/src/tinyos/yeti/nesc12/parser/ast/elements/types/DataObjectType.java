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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import tinyos.yeti.nesc12.parser.ast.elements.*;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DataObjectFieldDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

/**
 * A struct or union
 * @author Benjamin Sigg
 *
 */
public class DataObjectType extends AbstractType{
	public enum Kind{ STRUCT, NX_STRUCT, UNION, NX_UNION, ATTRIBUTE };
    
    private Kind kind;
    private String name;
    
    private boolean incomplete = false;
    
    private Field[] fields;
    
    private boolean nameRangesResolved = false;
    private boolean onFieldSearch = false;
    
    private DataObjectType( Kind kind, String name ){
        this.kind = kind;
        this.name = name;
        incomplete = true;
    }
    
    private DataObjectType( Kind kind, String name, DataObjectFieldDeclarationList object ){
        this.kind = kind;
        this.name = name;
        resolveFields( object );
    }
    
    public DataObjectType( Kind kind, String name, Field[] fields ){
        this.kind = kind;
        this.name = name;
        this.fields = fields;
    }
    
    public static DataObjectType struct( Identifier name, DataObjectFieldDeclarationList object ){
        return new DataObjectType( Kind.STRUCT, name == null ? null : name.getName(), object );
    }

    public static DataObjectType struct( Identifier name ){
        return new DataObjectType( Kind.STRUCT, name == null ? null : name.getName() );
    }

    public static DataObjectType nxstruct( Identifier name, DataObjectFieldDeclarationList object ){
        return new DataObjectType( Kind.NX_STRUCT, name == null ? null : name.getName(), object );
    }

    public static DataObjectType nxstruct( Identifier name ){
        return new DataObjectType( Kind.NX_STRUCT, name == null ? null : name.getName() );
    }
    
    public static DataObjectType union( Identifier name, DataObjectFieldDeclarationList object ){
        return new DataObjectType( Kind.UNION, name == null ? null : name.getName(), object );
    }
    
    public static DataObjectType union( Identifier name ){
        return new DataObjectType( Kind.UNION, name == null ? null : name.getName() );
    }
    
    public static DataObjectType nxunion( Identifier name, DataObjectFieldDeclarationList object ){
        return new DataObjectType( Kind.NX_UNION, name == null ? null : name.getName(), object );
    }
    
    public static DataObjectType nxunion( Identifier name ){
        return new DataObjectType( Kind.NX_UNION, name == null ? null : name.getName() );
    }
    
    public static DataObjectType attribute( Identifier name, DataObjectFieldDeclarationList object ){
        return new DataObjectType( Kind.ATTRIBUTE, name == null ? null : name.getName(), object );
    }
    
    @Override
    public String id( Map<Type, String> putin, boolean typedefVisible ){
        String id = putin.get( this );
        if( id == null ){
            String idPrefix;
            if( kind == null )
                idPrefix = "dn";
            else{
                switch( kind ){
                    case ATTRIBUTE:
                        idPrefix = "da";
                        break;
                    case STRUCT:
                        idPrefix = "ds";
                        break;
                    case NX_STRUCT:
                    	idPrefix = "dxs";
                    	break;
                    case UNION:
                        idPrefix = "du";
                        break;
                    case NX_UNION:
                    	idPrefix = "dxu";
                    	break;
                    default:
                        idPrefix = null;
                }
            }
            
            putin.put( this, idPrefix );
            
            StringBuilder builder = new StringBuilder();
            builder.append( idPrefix );
            builder.append( "{" );
            if( fields != null ){
                for( Field field : fields ){
                    builder.append( "(" );
                    if( field.getType() != null )
                        builder.append( field.getType().id( putin, typedefVisible ) );
                    builder.append( ":" );
                    if( field.getName() != null )
                        builder.append( field.getName().toIdentifier() );
                    builder.append( ")" );
                }
            }
            builder.append( "}" );
            id = builder.toString();
            
            putin.put( this, id );
        }
        
        return id;
    }
    
    public boolean isIncomplete(){
        return incomplete;
    }
    
    
    /**
     * An unsized type describes a type that can't be used in structures
     * or arrays.
     * @return <code>true</code> if the size of this type is variable
     */
    public boolean isUnsized(){
        int count = getFieldCount();
        if( count == 0 )
            return false;
        
        Field field = getField( count-1 );
        if( field == null || field.getType() == null )
            return false;
        
        ArrayType type = field.getType().asArrayType();
        if( type == null )
            return false;
        
        return type.getSize() == ArrayType.Size.INCOMPLETE;
    }
    
    @Override
    protected String createId( Map<Type, String> putin, boolean typedefVisible ){
        return id( putin, typedefVisible );
    }
    
    private void resolveFields( DataObjectFieldDeclarationList object ){
        if( object == null ){
            fields = new Field[]{};
        }
        else{
            List<Field> list = object.resolveFields();
            if( list == null )
                fields = new Field[]{};
            else
                fields = list.toArray( new Field[ list.size() ] );
            
            for( int i = 0, n = fields.length; i<n; i++ ){
                if( fields[i] != null )
                    fields[i] = fields[i].asSimple();
            }
        }
    }
    
    public boolean isStruct(){
        return kind == Kind.STRUCT || kind == Kind.NX_STRUCT;
    }
    
    public boolean isUnion(){
        return kind == Kind.UNION || kind == Kind.NX_UNION;
    }
    
    public boolean isNX(){
    	return kind == Kind.NX_STRUCT || kind == Kind.NX_UNION;
    }
    
    public boolean isAttribute(){
        return kind == Kind.ATTRIBUTE;
    }
    
    public String getName() {
		return name;
	}
    
    public Kind getKind() {
		return kind;
	}
    
    public int getFieldCount(){
        if( incomplete )
            return 0;
        
        return fields.length;
    }
    
    public Field getField( int index ){
        return fields[ index ];
    }
    
    public List<Field> getAllFields(){
    	List<Field> fields = new ArrayList<Field>();
    	getAllFields( fields );
    	return fields;
    }
    
    private void getAllFields( List<Field> fields ){
    	if( !onFieldSearch ){
    		onFieldSearch = true;
    		try{
    			if( this.fields != null ){
    				for( Field field : this.fields ){
    					if( field != null ){
    						DataObjectType type = field.getType().asDataObjectType();
    						if( type != null && field.getName() == null ){
    							type.getAllFields( fields );
    						}
    						else{
    							fields.add( field );
    						}
    					}
    				}
    			}
    		}
    		finally{
    			onFieldSearch = false;
    		}
    	}
    }
    
    /**
     * Searches a field with <code>name</code>, note that this field
     * might not be part of this data object but can also be a field of a
     * sub-object.
     * @param name the name of the field
     * @return the field or <code>null</code>
     */
    public Field getField( Name name ){
    	if( onFieldSearch )
    		return null;
    	
	    try{
	    	onFieldSearch = true;
	    	
	    	if( fields == null )
	    		return null;
	    	
	        for( Field field : fields ){
	            if( name.equals( field.getName() ) )
	                return field;
	        }
	        
	        for( Field field : fields ){
	        	DataObjectType type = field.getType().asDataObjectType();
	        	if( type != null && field.getName() == null ){
	        		Field result = type.asDataObjectType().getField( name );
		        	if( result != null ){
		        		return result;
		        	}
	        	}
	        }
	        
	        return null;
	    }
	    finally{
	    	onFieldSearch = false;
	    }
    }
    
    public void resolveNameRanges(){
        if( !nameRangesResolved ){
            nameRangesResolved = true;
            if( fields != null ){
                for( Field field : fields ){
                    if( field != null ){
                        field.resolveNameRanges();
                    }
                }
            }
        }
    }
    
    public Type replace( Map<GenericType, Type> generic ) {
    	if( fields == null || fields.length == 0 )
    		return this;
    	
        Field[] fields = new Field[ this.fields.length ];
        for( int i = 0, n = fields.length; i<n; i++ ){
            if( this.fields[i] != null ){
                fields[i] = this.fields[i].replace( null, generic );
            }
        }
        
        DataObjectType result = new DataObjectType( kind, name, fields );
        return result;
    }
    
    @Override
    public DataObjectType asDataObjectType(){
        return this;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode( fields );
        result = prime * result + ( ( kind == null ) ? 0 : kind.hashCode() );
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
    
    public Value cast( Value value ) {
        if( this.equals( value.getType() ) )
            return value;
        
        return new UnknownValue( this );
    }
    /*
    public void checkConversion( ConversionMap map, Type valueType, ASTMessageHandler handler, ASTNode location ) {
        DataObjectType object = valueType.asDataObjectType();
        if( object == null ){
            handler.report( Severity.ERROR, "Incompatible types in assignment", location );
            return;
        }
        
        if( !equals( valueType )){
            handler.report( Severity.ERROR, "Incompatible types in assignment", location );
        }
    }
    */
    
    public int getInitializerLength() {
        int sum = 0;
        for( int i = 0, n = getFieldCount(); i<n; i++ ){
            Type type = getField( i ).getType();
            if( type != null )
                sum += type.getInitializerLength();
            else
                sum += 1;
        }
        return sum;
    }
    
    public boolean isLeafInitializerType() {
        return false;
    }
    
    public Type getInitializerType( int index, Initializer initializer ) {
    	return getInitializerType( index, initializer, null );
    }
    
    public Type getInitializerType( int index, Initializer initializer, TypedefType self ){
        if( index < 0 )
            return null;
        
        for( int i = 0, n = getFieldCount(); i<n; i++ ){
            Type type = getField( i ).getType();
            if( type != null ){
                int length = type.getInitializerLength();
                if( index < length ){
                    switch( initializer ){
                        case CHILD:
                            return type;
                        case PARENT:
                            if( type.isLeafInitializerType() )
                                return self == null ? this : self;
                        case LEAF:
                            return type.getInitializerType( index, initializer );
                    }
                }
                else{
                    if( i+1 < n ){
                        index -= length;
                    }
                }
            }
            else{
                index--;
            }
        }
        
        // last type might be an array..
        if( getFieldCount() > 0 ){
            Type last = getField( getFieldCount()-1 ).getType();
            if( last != null && last.asArrayType() != null ){
                ArrayType array = last.asArrayType();
                if( array.getSize() == ArrayType.Size.INCOMPLETE || array.getSize() == ArrayType.Size.VARIABLE ){
                    if( initializer == Initializer.CHILD )
                        return array;
                    
                    return array.getInitializerType( index, initializer );
                }
            }
        }
        
        return null;
    }
    
    public Value getStaticDefaultValue() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public int sizeOf() {
        // simple, but as long as it is consistent...
        if( kind == Kind.UNION ){
            int max = 0;
            for( int i = 0, n = getFieldCount(); i<n; i++ ){
                Type type = getField( i ).getType();
                if( type != null )
                    max = Math.max( max, type.sizeOf() );
                else
                    max = Math.max( max, 4 );
            }
            return max;
        }
        else{
            int sum = 0;
            for( int i = 0, n = getFieldCount(); i<n; i++ ){
                Type type = getField( i ).getType();
                if( type != null )
                    sum += type.sizeOf();
                else
                    sum += 4;
            }
            return sum;
        }
    }
    
    @Override
    public int getSegmentCount(){
        return 1;
    }
    
    @Override
    public String getSegmentName( int segment ){
        return "fields";
    }
    
    @Override
    public int getSegmentSize( int segment ){
        return getFieldCount();
    }
    
    @Override
    public Binding getSegmentChild( int segment, int index ){
        return getField( index );
    }
    
    public String toLabel( String name, Label label ){
        StringBuilder builder = new StringBuilder();
        if( isStruct() || isAttribute() )
            builder.append( "struct" );
        else
            builder.append( "union" );
        
        if( this.name != null ){
            builder.append( " " );
            if( isAttribute() )
                builder.append( "@" );
            builder.append( this.name );
        }
        if( this.name == null || label == Label.EXTENDED ){
            builder.append( "{" );
            boolean first = true;
            if( fields != null ){
                for( Field field : fields ){
                    if( first )
                        first = false;
                    else
                        builder.append( ", " );

                    if( field.getType() == null ){
                        builder.append( "?" );
                    }
                    else{
                        builder.append( field.getType().toLabel( Name.toIdentifier( field.getName() ), Label.SMALL ) );
                    }
                }
            }
            builder.append( "}" );
        }
        
        if( name != null ){
            builder.append( " " );
            builder.append( name );
        }
        
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return toLabel( null, Label.SMALL );
    }
}
