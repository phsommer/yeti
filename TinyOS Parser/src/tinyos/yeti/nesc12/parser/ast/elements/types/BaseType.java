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
import tinyos.yeti.nesc12.parser.ast.elements.values.FloatingValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;

public enum BaseType implements Type{
    VOID( false, false, 0, 0 ),

    BOOL( false, true, 4, 1 ),
    U_CHAR( true, true, 1, 2 ),
    S_CHAR( true, true, 1, 2 ),
    U_SHORT( true, true, 2, 3 ),
    S_SHORT( true, true, 2, 3 ),    
    U_INT( true, true, 4, 4 ),
    S_INT( true, true, 4, 4 ),
    U_LONG( true, true, 8, 5 ),
    S_LONG( true, true, 8, 5 ),
    U_LONG_LONG( true, true, 8, 6 ), 
    S_LONG_LONG( true, true, 8, 6 ), 
    FLOAT( false, true, 4, 7 ),
    DOUBLE( false, true, 8, 8 ),
    LONG_DOUBLE( false, true, 8, 9 ),

    FLOAT_COMPLEX( false, true, 8, 10 ),
    DOUBLE_COMPLEX( false, true, 16, 11 ),
    LONG_DOUBLE_COMPLEX( false, true, 16, 12 );

    private boolean integer;
    private boolean arithmetic;
    private int size;
    private int ordinal;

    private BaseType( boolean integer, boolean arithmetic, int size, int ordinal ){
        this.integer = integer;
        this.arithmetic = arithmetic;
        this.size = size;
        this.ordinal = ordinal;
    }

    public int sizeOf() {
        return size;
    }

    public String id( boolean typedefVisible ){
        switch( this ){
            case BOOL: return "B";
            case DOUBLE: return "D";
            case DOUBLE_COMPLEX: return "DC";
            case FLOAT: return "F";
            case FLOAT_COMPLEX: return "FC";
            case LONG_DOUBLE: return "LD";
            case LONG_DOUBLE_COMPLEX: return "LDC";
            case S_CHAR: return "C";
            case S_INT: return "I";
            case S_LONG: return "L";
            case S_LONG_LONG: return "LL";
            case S_SHORT: return "S";
            case U_CHAR: return "UC";
            case U_INT: return "UI";
            case U_LONG: return "UL";
            case U_LONG_LONG: return "ULL";
            case U_SHORT: return "US";
            case VOID: return "V";
            default: return null;
        }
    }
    public String id( Map<Type, String> putin, boolean typedefVisible ){
        return id( typedefVisible );
    }

    public int typeOrdinal(){
        return ordinal;
    }

    public Value cast( Value value ) {
        // TODO what is with pointers? Are the conversions correct?
        if( this.equals( value.getType() ))
            return value;

        if( this == VOID )
            return value;

        if( value instanceof PointerType )
        	return new UnknownValue( this );
        
        if( value instanceof UnknownValue ){
        	if( value.getType() == this )
        		return value;
        	
        	return new UnknownValue( this );
        }
        
        if( value instanceof IntegerValue ){
            IntegerValue i = (IntegerValue)value;

            switch( this ){
                case U_CHAR:
                case U_SHORT:
                case U_INT:
                case U_LONG:
                case U_LONG_LONG:
                    return new IntegerValue( this, IntegerValue.cut( i.getValue(), sizeOf(), true ) );
                case S_CHAR:
                case S_SHORT:
                case S_INT:
                case S_LONG:
                case S_LONG_LONG:
                    return new IntegerValue( this, IntegerValue.cut( i.getValue(), sizeOf(), false ) );

                case FLOAT:
                    float t = (float)i.asFloating();
                    return new FloatingValue( this, t );
                case DOUBLE:
                case LONG_DOUBLE:
                    return new FloatingValue( this, i.asFloating() );
                default:
                    return null;
            }
        }
        if( value instanceof FloatingValue ){
            FloatingValue f = (FloatingValue)value;

            switch( this ){
                case U_CHAR:
                case S_CHAR:
                    return new IntegerValue( this, 0xF & (long)f.getValue() );
                case U_SHORT:
                case S_SHORT:
                    return new IntegerValue( this, 0xFF & (long)f.getValue() );
                case U_INT:
                case S_INT:
                    return new IntegerValue( this, 0xFFFF & (long)f.getValue() );
                case U_LONG:
                case U_LONG_LONG:
                    return new IntegerValue( this, (long)f.getValue() );
                case FLOAT:
                    return new FloatingValue( this, (float)f.getValue() );
                case DOUBLE:
                case LONG_DOUBLE:
                    return new FloatingValue( this, f.getValue() );
                default:
                    return null;
            }
        }

        return null;
    }

    public String getBindingType() {
        return "Type";
    }

    public String getBindingValue() {
        return toString();
    }

    public Binding getSegmentChild( int segment, int index ) {
        return null;
    }

    public int getSegmentCount() {
        return 0;
    }

    public String getSegmentName( int segment ) {
        return null;
    }

    public int getSegmentSize( int segment ) {
        return 0;
    }

    public Type asType() {
        return this;
    }

    public Value asValue() {
        return null;
    }

    public void resolveNameRanges(){
        // ignore
    }

    public Type replace( Map<GenericType, Type> generic ) {
        return this;
    }

    public ArrayType asArrayType(){
        return null;
    }

    public BaseType asBase(){
        return this;
    }

    public DataObjectType asDataObjectType(){
        return null;
    }

    public FunctionType asFunctionType(){
        return null;
    }

    public PointerType asPointerType(){
        return null;
    }

    public ConstType asConstType(){
        return null;
    }

    public GenericType asGenericType(){
        return null;
    }

    public EnumType asEnumType(){
        return null;
    }

    public TypedefType asTypedefType() {
        return null;
    }

    public boolean isIncomplete(){
        return false;
    }

    public boolean isIntegerType(){
        return integer;
    }

    public boolean isArithmeticType() {
        return arithmetic;
    }

    /**
     * Tries to find out to which type and expression that contains
     * <code>a</code> and <code>b</code> evaluates.
     * @param a the first type
     * @param b the second type
     * @return the combination or <code>null</code>
     */
    public static BaseType arithmetic( BaseType a, BaseType b ){
        return a.ordinal() > b.ordinal() ? a : b;
    }

    public int getInitializerLength() {
        if( this == VOID )
            return 0;

        // TODO what is with complex types?
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
    	if( index == 0 )
    		return self == null ? this : self;
    	return null;
    }

    public Value getStaticDefaultValue() {
        if( this == VOID )
            return null;

        switch( this ){
            case FLOAT:
            case DOUBLE:
            case LONG_DOUBLE:
                return new FloatingValue( this, 0 );
            case BOOL:
            case S_CHAR:
            case U_CHAR:
            case S_INT:
            case U_INT:
            case S_LONG:
            case U_LONG:
            case S_LONG_LONG:
            case U_LONG_LONG:
            case S_SHORT:
            case U_SHORT:
                return new IntegerValue( this, 0 );
        }

        // TODO what is with complex types?
        return null;
    }
    
    public String toLabel( String name, Label label ){
        boolean small = label == Label.SMALL || label == Label.DECLARATION;
        
        if( name == null )
            return label( small );

        return label( small ) + " " + name;
    }

    @Override
    public String toString() {
        return label( false );
    }

    private String label( boolean small ){
        if( small ){
            switch( this ){
                case BOOL: return "_BOOL";
                case DOUBLE: return "double";
                case DOUBLE_COMPLEX: return "double _Complex";
                case FLOAT: return "float";
                case FLOAT_COMPLEX: return "float _Complex";
                case LONG_DOUBLE: return "long double";
                case LONG_DOUBLE_COMPLEX: return "long double _Complex";
                case S_CHAR: return "char";
                case S_INT: return "int";
                case S_LONG: return "long";
                case S_LONG_LONG: return "long long";
                case S_SHORT: return "short";
                case U_CHAR: return "unsigned char";
                case U_INT: return "unsigned int";
                case U_LONG: return "unsigned long";
                case U_LONG_LONG: return "unsigned long long";
                case U_SHORT: return "unsigned short";
                case VOID: return "void";
            }

            return null;
        }
        else{
            switch( this ){
                case BOOL: return "_BOOL";
                case DOUBLE: return "double";
                case DOUBLE_COMPLEX: return "double _Complex";
                case FLOAT: return "float";
                case FLOAT_COMPLEX: return "float _Complex";
                case LONG_DOUBLE: return "long double";
                case LONG_DOUBLE_COMPLEX: return "long double _Complex";
                case S_CHAR: return "signed char";
                case S_INT: return "signed int";
                case S_LONG: return "signed long int";
                case S_LONG_LONG: return "signed long long int";
                case S_SHORT: return "signed short int";
                case U_CHAR: return "unsinged char";
                case U_INT: return "unsigned int";
                case U_LONG: return "unsigned long int";
                case U_LONG_LONG: return "unsigned long long int";
                case U_SHORT: return "unsigned short int";
                case VOID: return "void";
            }
        }
        return null;
    }
}
