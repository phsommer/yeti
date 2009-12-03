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

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.types.PointerType;

public class StringValue extends AbstractValue{
    private Type type;
    private int[] value;
    private boolean character = false;
    
    public StringValue( Type type, boolean character, int[] value ){
        this.type = type;
        this.character = character;
        this.value = value;
    }
    
    public String getBindingType() {
        return "String";
    }
    
    public String getBindingValue() {
        return toString();
    }
    
    public boolean isCharacter(){
        return character;
    }
    
    @Override
    public int hashCode(){
        return value == null ? 0 : Arrays.hashCode( value );
    }
    
    @Override
    public boolean equals( Object obj ){
        if( obj == this )
            return true;
        
        if( obj == null )
            return false;
        
        if( obj instanceof StringValue ){
            StringValue string = (StringValue)obj;
            if( string.value == null && value == null )
                return true;
            
            if( string.value == null || value == null )
                return false;
            
            if( string.character != character )
                return false;
            
            return Arrays.equals( string.value, value );
        }
        
        return false;
    }
    
    public static StringValue string( String text, boolean wide, AnalyzeStack stack ){
        int[] chars = resolveCharacters( text, true );
        if( chars == null )
            return null;
        
        Type base;
        if( wide ){
        	base = stack.getTypedef( new SimpleName( null, "wchar_t" ) );
        }
        else{
        	base = BaseType.U_CHAR;
        }
        
        // return new StringValue( new PointerType( BaseType.U_CHAR ), false, chars );
        return new StringValue( base == null ? null : new PointerType( base ), false, chars );
    }

    public static StringValue character( String text, boolean wide, AnalyzeStack stack ){
        int[] chars = resolveCharacters( text, false );
        if( chars == null )
            return null;
        
        if( wide ){
        	Type wchar_t = stack.getTypedef( new SimpleName( null, "wchar_t" ));
        	return new StringValue( wchar_t, true, chars );
        }
        else{
        	return new StringValue( BaseType.U_CHAR, true, chars );
        }
    }
    
    public static int[] resolveCharacters( String text, boolean endWithZero ){
        int[] temp;
        if( endWithZero )
            temp = new int[ text.length()+1 ];
        else
            temp = new int[ text.length() ];
        
        int count = 0;
        int index = 0;
        
        while( index < text.length() ){
            char next = text.charAt( index++ );
            if( next == '\\' ){
                if( index == text.length() ){
                    temp[ count++ ] = -1;
                    break;
                }
                
                next = text.charAt( index++ );
                boolean done = false;
                
                switch( next ){
                    case 'a':
                        temp[ count++ ] = 7;
                        done = true;
                        break;
                    case 'b':
                        temp[ count++ ] = 8;
                        done = true;
                        break;
                    case 'f':
                        temp[ count++ ] = 12;
                        done = true;
                        break;
                    case 'n': 
                        temp[ count++ ] = 10;
                        done = true;
                        break;
                    case 'r':
                        temp[ count++ ] = 13;
                        done = true;
                        break;
                    case 't': 
                        temp[ count++ ] = 9;
                        done = true;
                        break;
                    case 'v':
                        temp[ count++ ] = 11;
                        done = true;
                        break;
                    case '\\': 
                        temp[ count++ ] = 92;
                        done = true;
                        break;
                    case '?': 
                        temp[ count++ ] = 63;
                        done = true;
                        break;
                    case '\'':
                        temp[ count++ ] = 96;
                        done = true;
                        break;
                    case '"': 
                        temp[ count++ ] = 34;
                        done = true;
                        break;
                }
                if( !done ){
                    if( next == 'x' ){
                        if( index == text.length() ){
                            temp[ count++ ] = -1;
                            break;
                        }
                        
                        int length = 0;
                        for( int i = 0; i < 3 && index+i < text.length(); i++ ){
                            char c = text.charAt( index+i );
                            switch( c ){
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                    length++;
                                    break;
                                default:
                                    i = 3;
                            }
                        }
                        if( length == 0 ){
                            temp[ count++ ] = -1;
                        }
                        else{
                            temp[ count++ ] = Integer.parseInt( text.substring( index, index+length ), 8 );
                            index += length;
                        }
                    }
                    else{
                        index--;
                        int length = 0;
                        for( int i = 0; index+i < text.length(); i++ ){
                            char c = text.charAt( index+i );
                            switch( c ){
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                                case 'a':
                                case 'b':
                                case 'c':
                                case 'd':
                                case 'e':
                                case 'f':
                                case 'A':
                                case 'B':
                                case 'C':
                                case 'D':
                                case 'E':
                                case 'F':
                                    length++;
                                    break;
                                default:
                                    i = text.length();
                            }
                        }
                        
                        if( length == 0 ){
                            temp[ count++ ] = -1;
                        }
                        else{
                            temp[ count++ ] = Integer.parseInt( text.substring( index, index+length ), 16 );
                            index += length;
                        }
                    }
                }
            }
            else{
                temp[ count++ ] = next;
            }
        }
        
        if( endWithZero )
            temp[ count++ ] = 0;
        
        if( count == temp.length )
            return temp;
        
        int[] result = new int[ count ];
        System.arraycopy( temp, 0, result, 0, count );
        return result;
    }
    
    public Type getType() {
        return type;
    }
    
    public int getLength(){
        return value == null ? 0 : value.length;
    }
    
    public int getStringLength(){
        if( value == null )
            return 0;
        
        if( value.length == 0 )
            return 0;
        
        if( value[ value.length-1 ] == 0 )
            return value.length-1;
        
        return value.length;
    }
    
    public int[] getValue() {
        return value;
    }
    
    public int sizeOf() {
        return BaseType.U_CHAR.sizeOf() * value.length;
    }
    
    public void resolveNameRanges(){
        if( type != null )
            type.resolveNameRanges();
    }
    
    @Override
    public String toString(){
        return toLabel();
    }
    
    public String toLabel() {
        String close;
        if( type == BaseType.U_CHAR )
            close = "'";
        else
            close = "\"";
        
        StringBuilder builder = new StringBuilder( value.length+5 );
        builder.append( close );
        loop: for( int v : value ){
            boolean done = false;
            switch( v ){
                case 7:
                    builder.append( "\\a" );
                    done = true;
                    break;
                case 8:
                    builder.append( "\\b" );
                    done = true;
                    break;
                case 12:
                    builder.append( "\\f" );
                    done = true;
                    break;
                case 10:
                    builder.append( "\\n" );
                    done = true;
                    break;
                case 13:
                    builder.append( "\\r" );
                    done = true;
                    break;
                case 9:
                    builder.append( "\\t" );
                    done = true;
                    break;
                case 11:
                    builder.append( "\\v" );
                    done = true;
                    break;
                case 92: 
                    builder.append( "\\\\" );
                    done = true;
                    break;
                case 63:
                    builder.append( "\\?" );
                    done = true;
                    break;
                case 96:
                    builder.append( "\\'" );
                    done = true;
                    break;
                case 34:
                    builder.append( "\\\"" );
                    done = true;
                    break;
                case 0:
                    done = true;
                    break loop;
            }
            
            if( !done ){
                if( Character.isDefined( v ))
                    builder.append( (char)v );
                else{
                    builder.append( "\\x" );
                    builder.append( Integer.toHexString( v ) );
                }
            }
        }
        builder.append( close );
        return builder.toString();
    }
}
