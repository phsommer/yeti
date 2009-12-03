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

import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;

public class FloatingValue extends AbstractValue implements NumberValue{
    private double value;
    private BaseType type;
    
    public FloatingValue( BaseType type, double value ){
        this.type = type;
        this.value = value;
    }
    
    public void resolveNameRanges(){
        if( type != null )
            type.resolveNameRanges();
    }
    
    public String getBindingType() {
        return "Floating";
    }
    
    public String getBindingValue() {
        return toString();
    }
    
    public BaseType getType() {
        return type;
    }
    
    public double getValue() {
        return value;
    }
    
    public double asFloating() {
        return value;
    }
    
    public long asInteger() {
        return (long)value;
    }
    
    public boolean isFloating() {
        return true;
    }
    
    public boolean isInteger() {
        return false;
    }
    
    public int sizeOf() {
        return type.sizeOf();
    }
    
    public String toLabel(){
        return String.valueOf( value );
    }
    
    @Override
    public String toString() {
        return toLabel();
    }
    
    @Override
    public int hashCode(){
        long bits = Double.doubleToLongBits( value );
        return (int)(bits ^ (bits >>> 32));
    }
    
    @Override
    public boolean equals( Object obj ){
        if( obj == this )
            return true;
        if( obj == null )
            return false;
        if( obj instanceof FloatingValue ){
            FloatingValue floating = (FloatingValue)obj;
            return floating.value == value;
        }
        return false;
    }
    
    public static FloatingValue valueOf( String text ){
        /*
         * That is what the lexer sees as floating constant:
         * 
         * nondigit            =   [a-zA-Z_]
         * digit               =   [0-9]
         * digit_sequence      =   {digit}+
         * 
         * nonzero_digit       =   [1-9]
         * 
         * hexadecimal_digit   =   [0-9a-fA-F]
         * hexadecimal_digit_sequence      =   {hexadecimal_digit}+
         * hexadecimal_prefix  =   "0" ("x" | "X")
         * 
         * sign                =   "+" | "-"
         * floating_suffix     =   "f" | "F" | "l" | "L"
         * exponent_part       =   ("e" | "E") {sign}? {digit_sequence}
         * binary_exponent_part            =   ("p" | "P") {sign}? {digit_sequence}
         * fractional_constant =   ({digit_sequence} "." {digit_sequence}?) | ("." {digit_sequence})
         * hexadecimal_fractional_constant =   ({hexadecimal_digit_sequence} "." {hexadecimal_digit_sequence}?) | ("." {hexadecimal_digit_sequence})
         * hexadecimal_floating_constant   =   {hexadecimal_prefix} ({hexadecimal_fractional_constant}|{hexadecimal_digit_sequence}) {binary_exponent_part} {floating_suffix}?
         * decimal_floating_constant       =   ({fractional_constant} {exponent_part}?) | ({digit_sequence} {exponent_part}) {floating_suffix}?
         * floating_constant   =   {decimal_floating_constant} | {hexadecimal_floating_constant}
         */
        
        if( text.length() == 0 )
            return null;
        
        BaseType type = typeOf( text );
        switch( type ){
            case FLOAT:
            case LONG_DOUBLE:
                text = text.substring( 0, text.length()-1 );
                break;
        }
        
        try{
            boolean hex = text.startsWith( "0x" );
            if( hex ){
                text = text.substring( 2 );
                
                int point = text.indexOf( '.' );
                int power = text.indexOf( 'p' );
                if( power < 0 )
                    power = text.indexOf( 'P' );
                
                if( point < 0 ){
                    long begin = Long.parseLong( text.substring( 0, power ), 16 );
                    text = begin + text.substring( power );
                    return new FloatingValue( type, Double.parseDouble( text ) );
                }
                else{
                    String beginText = text.substring( 0, point );
                    String endText = text.substring( point+1, power );
                    
                    long begin = beginText.length() == 0 ? 0 : Long.parseLong( beginText, 16 );
                    long end = endText.length() == 0 ? 0 : Long.parseLong( endText, 16 );
                    
                    return new FloatingValue( type, Double.parseDouble( begin + "." + end + "e" + text.substring( power+1 )));
                }
            }
            else{
                return new FloatingValue( type, Double.parseDouble( text ));
            }
        }
        catch( NumberFormatException ex ){
            return null;
        }
    }
    
    public static BaseType typeOf( String text ){
        if( text.endsWith( "f" ) || text.endsWith( "F" ))
            return BaseType.FLOAT;
        if( text.endsWith( "l" ) || text.endsWith( "L" ))
            return BaseType.LONG_DOUBLE;
        
        return BaseType.DOUBLE;
    }
}
