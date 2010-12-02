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

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class ArrayType extends AbstractType {
    
    public static enum Size{
        /** like int x[6]; */
        SPECIFIED, 
        
        /** like int[*] x */
        INCOMPLETE,
        
        /** like int[] x */
        VARIABLE,
        
        /** like int[x] where x is a constant */
        SPECIFIED_UNKNOWN;
    }
    
    /**
     * Checks whether the type <code>raw</code> is a valid type for an array 
     * @param raw the raw type of the array, in "int[]", the raw type would be "int"
     * @param stack the stack used to create error and warnings if
     * {@link AnalyzeStack#isReportErrors()} returns <code>true</code>, may be <code>null</code>
     * @param location the location of the type
     */
    public static boolean checkValidType( Type raw, AnalyzeStack stack, ASTNode location ){
        if( raw == null )
            return false;
        
        boolean report = stack != null && stack.isReportErrors();
        
        if( raw.asDataObjectType() != null ){
            if( raw.asDataObjectType().isUnsized() ){
                if( report ){
                    stack.error( "array type has element type with unspecified size", location );
                }
                return false;
            }
        }
        
        if( raw.isIncomplete() ){
            if( report ){
                stack.error( "array type has incomplete element type", location );
            }
            return false;
        }
        
        if( raw.asArrayType() != null && raw.asArrayType().getSize() == Size.VARIABLE ){
            if( report ){
                stack.error( "array type has variable sized element type", location );
            }
            return false;
        }
        
        return true;
    }
    
    public static ArrayType incomplete( Type raw, AnalyzeStack stack, ASTNode location ){
        if( checkValidType( raw, stack, location ))
            return new ArrayType( raw, Size.INCOMPLETE, 0 );
        else
            return null;
    }
    
    public static ArrayType incomplete( Type raw ){
        return new ArrayType( raw, Size.INCOMPLETE, 0 );
    }
    
    public static ArrayType specified( Type raw, int length, AnalyzeStack stack, ASTNode location ){
        if( checkValidType( raw, stack, location ))
            return new ArrayType( raw, Size.SPECIFIED, length );
        else
            return null;
    }
    
    public static ArrayType specified( Type raw, int length ){
        return new ArrayType( raw, Size.SPECIFIED, length );
    }
    
    public static ArrayType specifiedUnknown( Type raw ){
    	return new ArrayType( raw, Size.SPECIFIED_UNKNOWN, 0 );
    }
    
    public static ArrayType variable( Type raw, AnalyzeStack stack, ASTNode location ){
        if( checkValidType( raw, stack, location ))
            return new ArrayType( raw, Size.VARIABLE, 0 );
        else
            return null;
    }
    
    public static ArrayType variable( Type raw ){
        return new ArrayType( raw, Size.VARIABLE, 0 );
    }
    
    private Type raw;
    private boolean nameResolving;
    
    private Size size;
    private int length;
    
    /**
     * Creates a new array. Note: clients should use {@link #incomplete(Type)},
     * {@link #specified(Type, int)} or {@link #variable(Type)} to create
     * new arrays.
     * @param raw the type from which this array is built
     * @param size how the size is defined
     * @param length the length, only meaningful if the size is {@link Size#SPECIFIED}
     */
    public ArrayType( Type raw, Size size, int length ){
        this.raw = raw;
        this.size = size;
        this.length = length;
    }
    
    @Override
    protected String createId( Map<Type, String> putin, boolean typedefVisible ){
        StringBuilder builder = new StringBuilder();
        builder.append( "a" );
        if( size == null )
            builder.append( "n" );
        else{
            switch( size ){
                case INCOMPLETE:
                    builder.append( "i" );
                    break;
                case VARIABLE:
                    builder.append( "v" );
                    break;
                case SPECIFIED:
                    builder.append( length );
                    break;
            }
        }
        if( raw != null )
            builder.append( raw.id( putin, typedefVisible ) );
        
        return builder.toString();
    }
    
    @Override
    public ArrayType asArrayType(){
        return this;
    }
    
    public boolean isIncomplete(){
        return size == Size.INCOMPLETE;
    }
    
    public Value cast( Value value ) {
        if( this.equals( value.getType() ))
            return value;
        else
            return null;
    }
    /*
    public void checkConversion( ConversionMap map, Type valueType, ASTMessageHandler handler, ASTNode location ) {
        PointerType pointer = valueType.asPointerType();
        
        if( pointer != null ){
            if( getRawType() != null && pointer.getRawType() != null ){
                if( getRawType().equals( pointer.getRawType() ) ){
                    handler.report( Severity.WARNING, "cast of pointer to array", location );
                }
                else{
                    handler.report( map.getErrorSeverity(), "incompatible types in assignment", location );
                }
            }
        }
        else if( !( valueType.equals( this ))){
            handler.report( map.getErrorSeverity(), "incompatible types in assignment", location );
        }
    }
    */
    public int getInitializerLength() {
        if( size == Size.SPECIFIED ){
            int length = raw == null ? 1 : raw.getInitializerLength();
            return length * this.length;
        }
        else{
            return 0;
        }
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
        
        int rawLength = raw.getInitializerLength();
        int rawIndex = index / rawLength;
        if( size == Size.SPECIFIED && rawIndex >= length )
            return null;
        
        if( initializer == Initializer.CHILD )
            return raw;
        
        if( initializer == Initializer.PARENT && raw.isLeafInitializerType() )
            return self == null ? this : null;
        
        index -= rawIndex * rawLength;
        return raw.getInitializerType( index, initializer );
    }
    
    public Value getStaticDefaultValue() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public int sizeOf() {
        if( size == Size.SPECIFIED ){
            int length = raw == null ? 1 : raw.sizeOf();
            if( length == -1 )
                return -1;
            
            return length * this.length;
        }
        else
            return -1;
    }
    
    public Type getRawType(){
        return raw;
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
        
        return new ArrayType( check, size, length );
    }
    
    public Size getSize() {
        return size;
    }
    
    public int getLength() {
        return length;
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
        if( name == null ){
            if( size == null ){
                name = "[]";
            }
            else{
                switch( size ){
                    case INCOMPLETE:
                        name = "[*]";
                        break;
                    case VARIABLE:
                        name = "[]";
                        break;
                    case SPECIFIED:
                        name = "[" + length + "]";
                        break;
                    case SPECIFIED_UNKNOWN:
                    	name = "[?]";
                    	break;
                }
            }
        }
        else{
            if( size == null ){
                name = name + "[]";
            }
            else{
                switch( size ){
                    case INCOMPLETE:
                        name = name + "[*]";
                        break;
                    case VARIABLE:
                        name = name + "[]";
                        break;
                    case SPECIFIED:
                        name = name + "[" + length + "]";
                        break;
                    case SPECIFIED_UNKNOWN:
                    	name = name + "[?]";
                    	break;
                }
            }
        }
        
        return raw.toLabel( name, label );
    }
    
    @Override
    public String toString() {
        return toLabel( null, Label.EXTENDED );
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + length;
        result = prime * result + ( ( raw == null ) ? 0 : raw.hashCode() );
        result = prime * result + ( ( size == null ) ? 0 : size.hashCode() );
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
