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

import java.math.BigInteger;
import java.util.Arrays;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class IntegerValue extends AbstractValue implements NumberValue{
    private BigInteger value;
    private BaseType type;

    public IntegerValue( BaseType type, BigInteger value ){
        this.value = value;
        this.type = type;
    }

    public IntegerValue( BaseType type, long value ){
        this( type, BigInteger.valueOf( value ));
    }

    public void resolveNameRanges(){
        if( type != null )
            type.resolveNameRanges();
    }

    public String getBindingType() {
        return "Integer";
    }

    public String getBindingValue() {
        return toString();
    }

    public BaseType getType() {
        return type;
    }

    public BigInteger getValue() {
        return value;
    }

    public double asFloating() {
        return value.doubleValue();
    }

    public long asInteger() {
        return value.longValue();
    }

    public int intValue(){
        return value.intValue();
    }

    public boolean isFloating() {
        return false;
    }

    public boolean isInteger() {
        return true;
    }

    public int sizeOf() {
        return type.sizeOf();
    }

    public String toLabel(){
        return value.toString();
    }

    @Override
    public String toString() {
        return value.toString();
        /*
        if( value < 0 ){
            switch( type ){
                case U_CHAR:
                    return String.valueOf( 0xF & value );
                case U_SHORT:
                    return String.valueOf( 0xFF & value );
                case U_INT:
                    return String.valueOf( 0xFFFF & value );
                case U_LONG:
                    return String.valueOf( 0x7FFFFFFF & value );
                case U_LONG_LONG:
                    return String.valueOf( 0x7FFFFFFF & value );
            }
        }

        return String.valueOf( value );*/
    }

    @Override
    public int hashCode(){
        return value == null ? 0 : value.hashCode();
    }

    @Override
    public boolean equals( Object obj ){
        if( obj == this )
            return true;

        if( obj == null )
            return false;

        if( obj instanceof IntegerValue ){
            IntegerValue integer = (IntegerValue)obj;
            if( integer.value == null && value == null )
                return true;
            if( integer.value == null || value == null )
                return false;
            return value.equals( integer.value );
        }

        return false;
    }

    /**
     * Interprets the contents of <code>text</code> and returns an 
     * {@link IntegerValue} with the smallest possible type that still
     * holds the value text. Might cut the integer represented by text in
     * order to fit into the biggest type.
     * @param text some integer
     * @return the value or <code>null</code> if <code>text</code> is not valid
     */
    public static IntegerValue valueOf( String text ){
        return valueOf( text, null, null );
    }

    /**
     * Interprets the contents of <code>text</code> and returns an 
     * {@link IntegerValue} with the smallest possible type that still
     * holds the value text. Might cut the integer represented by text in
     * order to fit into the biggest type.
     * @param text some integer
     * @param stack used to report errors
     * @param location used to report the location of errors
     * @return the value or <code>null</code> if <code>text</code> is not valid
     */
    public static IntegerValue valueOf( String text, AnalyzeStack stack, ASTNode location ){
        try{
            if( text.length() == 0 )
                return null;

            // check whether type is specified
            boolean unsigned = false;
            boolean lSuffix = false;
            boolean llSuffix = false;

            boolean run = false;

            BaseType type = null;

            do{
                run = false;

                if( text.length() > 1 && ( text.endsWith( "u" ) || text.endsWith( "U" ))){
                    run = true;
                    unsigned = true;
                    text = text.substring( 0, text.length()-1 );
                }
                if( text.length() > 2 && ( text.endsWith( "ll" ) || text.endsWith( "LL" ))){
                    run = true;
                    llSuffix = true;
                    text = text.substring( 0, text.length()-2 );
                }
                if( text.length() > 1 && ( text.endsWith( "l" ) || text.endsWith( "L" ))){
                    run = true;
                    lSuffix = true;
                    text = text.substring( 0, text.length()-1 );
                }
            }while( run );

            BigInteger integer = parse( text );
            boolean decimal = isDecimal( text );

            BaseType[] candidates = standardTypes( decimal, unsigned, lSuffix, llSuffix );

            for( BaseType candiate : candidates ){
                if( inBoundaries( integer, candiate )){
                    type = candiate;
                    break;
                }
            }

            if( type == null ){
                type = candidates[ candidates.length-1 ];

                if( stack != null && stack.isReportErrors() ){
                    stack.warning( "integer constant is too large for '" + type.toLabel( null, Type.Label.SMALL ) + "' type", location );
                }

                integer = cut( integer, type );
            }

            return new IntegerValue( type, integer );
        }
        catch( NumberFormatException ex ){
            return null;
        }
    }

    public static boolean isDecimal( String text ){
        return !text.startsWith( "x" ) &&
        !text.startsWith( "X" ) &&  
        !text.startsWith( "0" );
    }

    /**
     * Parses <code>text</code> which can represent a hexadecimal, octal or
     * decimal.
     * @param text some text
     * @return the parsed text
     */
    public static BigInteger parse( String text ){
        if( text.startsWith( "x" ) || text.startsWith( "X" )){
            // hexadecimal
            text = text.substring( 1 );
            return new BigInteger( text, 16 );
        }

        if( text.startsWith( "0x" ) || text.startsWith( "0X" )){
            // hexadecimal
            text = text.substring( 2 );
            return new BigInteger( text, 16 );
        }

        if( text.length() > 1 && text.charAt( 0 ) == '0' ){
            // octal
            text = text.substring( 1 );
            return new BigInteger( text, 8 );
        }

        // decimal
        return new BigInteger( text );
    }

    /**
     * Gets the types that a value could have given several flags.
     * @param decimal whether the value is decimal
     * @param flagU whether the "u" or "U" was seen
     * @param flagL whether the "l" or "L" was seen
     * @param flagLL whether the "ll" or "LL" was seen
     * @return the types
     */
    public static BaseType[] standardTypes( boolean decimal, boolean flagU, boolean flagL, boolean flagLL ){
        if( decimal ){
            if( flagU ){
                if( flagL ){
                    return new BaseType[]{ BaseType.U_LONG, BaseType.U_LONG_LONG };
                }
                if( flagLL ){
                    return new BaseType[]{ BaseType.U_LONG_LONG };
                }
                return new BaseType[]{ BaseType.U_INT, BaseType.U_LONG, BaseType.U_LONG_LONG };
            }
            if( flagL ){
                return new BaseType[]{ BaseType.S_LONG, BaseType.S_LONG_LONG };
            }
            if( flagLL ){
                return new BaseType[]{ BaseType.S_LONG_LONG };
            }

            return new BaseType[]{
                    // not standard C
                    BaseType.S_CHAR,
                    BaseType.S_SHORT,

                    BaseType.S_INT, 
                    BaseType.S_LONG,
                    BaseType.S_LONG_LONG };
        }

        if( flagU ){
            if( flagL ){
                return new BaseType[]{ BaseType.U_LONG, BaseType.U_LONG_LONG };
            }
            if( flagLL ){
                return new BaseType[]{ BaseType.U_LONG_LONG };
            }
        }
        if( flagL ){
            return new BaseType[]{ BaseType.S_LONG, BaseType.U_LONG, BaseType.S_LONG_LONG, BaseType.U_LONG_LONG };
        }
        if( flagLL ){
            return new BaseType[]{ BaseType.S_LONG_LONG, BaseType.U_LONG_LONG };
        }

        return new BaseType[]{
                // not standard C
                BaseType.S_CHAR,
                BaseType.U_CHAR,
                BaseType.S_SHORT,
                BaseType.U_SHORT,
                
                BaseType.S_INT,
                BaseType.U_INT,
                BaseType.S_LONG,
                BaseType.U_INT,
                BaseType.S_LONG_LONG,
                BaseType.U_LONG_LONG };
    }

    /**
     * Tells whether the integer <code>integer</code> can be represented by
     * <code>type</code>.
     * @param integer some value
     * @param type some type
     * @return <code>true</code> if <code>integer</code> is a <code>type</code>
     */
    public static boolean inBoundaries( BigInteger integer, BaseType type ){
        switch( type ){
            case S_CHAR:
            case S_INT:
            case S_LONG:
            case S_LONG_LONG:
            case S_SHORT:
                return inBoundaries( integer, type.sizeOf(), false );
            case U_CHAR:
            case U_INT:
            case U_LONG:
            case U_LONG_LONG:
            case U_SHORT:
                return inBoundaries( integer, type.sizeOf(), true );
        }

        return false;
    }

    /**
     * Checks whether the value <code>integer</code> can be represented with
     * <code>sizeOf</code> bytes.
     * @param integer some value
     * @param sizeOf the number of available bytes
     * @param unsigned whether the bytes represent an unsigned or signed type 
     * @return <code>true</code> if integer is in the boundaries
     */
    public static boolean inBoundaries( BigInteger integer, int sizeOf, boolean unsigned ){
        boolean negative = integer.signum() < 0;

        if( unsigned ){
            return !negative && ((sizeOf * 8) >= integer.bitLength());
        }
        else{
            int needed = integer.bitLength() + 1;
            int available = sizeOf * 8;

            if( needed == available+1 && negative ){
                // check for special case 1000 0000 ... 0000 which has no
                // positive representation
                int lowest = integer.getLowestSetBit();
                if( lowest+1 == available )
                    needed--;
            }

            return available >= needed;
        }
    }

    public static BigInteger cut( BigInteger integer, BaseType type ){
        switch( type ){
            case U_CHAR:
            case U_INT:
            case U_LONG:
            case U_LONG_LONG:
            case U_SHORT:
                return cut( integer, type.sizeOf(), true );
            case S_CHAR:
            case S_INT:
            case S_LONG:
            case S_LONG_LONG:
            case S_SHORT:
                return cut( integer, type.sizeOf(), false );
        }

        return null;
    }

    /**
     * Creates a value that represents <code>integer</code> and fits into
     * <code>sizeOf</code> bytes.
     * @param integer some value
     * @param sizeOf the number of available bytes
     * @param unsigned whether the result must be unsigned or not
     * @return some value that passes {@link #inBoundaries(BigInteger, int, boolean)}
     */
    public static BigInteger cut( BigInteger integer, int sizeOf, boolean unsigned ){
        if( inBoundaries( integer, sizeOf, unsigned ))
            return integer;

        byte[] val = new byte[sizeOf+1];
        Arrays.fill( val, 1, val.length, (byte)-1 );

        BigInteger unsignedMax = new BigInteger( val ); 
        integer = integer.mod( unsignedMax );
        if( unsigned ){
            return integer;
        }
        else{
            if( integer.bitLength() >= sizeOf * 8 )
                return integer.subtract( unsignedMax );

            return integer;
        }
    }
}
