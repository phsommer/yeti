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
package tinyos.yeti.preprocessor.expression;

import tinyos.yeti.preprocessor.output.Insights;


public abstract class Lazy {
    public abstract long value( ExprState states );
    
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        toString( builder, 0 );
        return builder.toString();
    }
    
    protected abstract void toString( StringBuilder builder, int tabs );
    
    protected void toString( StringBuilder builder, int tabs, String kind, Lazy... children ){
        for( int i = 0; i < tabs; i++ )
            builder.append( "  " );
        builder.append( kind );
        for( Lazy child : children ){
            builder.append( "\n" );
            child.toString( builder, tabs+1 );
        }
    }
    
    public static Lazy addition( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return a.value( states ) + b.value( states );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "+", a, b );
            }
        };
    }
    
    public static Lazy subtraction( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return a.value( states ) - b.value( states );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "-", a, b );
            }
        };
    }
    
    public static Lazy mod( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return a.value( states ) % b.value( states );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "%", a, b );
            }
        };
    }
    
    public static Lazy neg( final Lazy a ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return - a.value( states );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "-", a );
            }
        };
    }
    
    public static Lazy multiplication( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return a.value( states ) * b.value( states );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "*", a, b );
            }
        };
    }
    
    public static Lazy division( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                long bvalue = b.value( states );
                if( bvalue == 0 ){
                    states.reportError( "Division by zero.", Insights.expressionDivisionByZero() );
                    return 0;
                }
                
                return a.value( states ) / bvalue;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "/", a, b );
            }
        };
    }
    
    public static Lazy bitor( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return a.value( states ) | b.value( states );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "|", a, b );
            }
        };
    }
    
    public static Lazy or( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                long check = a.value( states );
                if( check != 0 )
                    return 1;
                check = b.value( states );
                if( check != 0 )
                    return 1;
                return 0;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "||", a, b );
            }
        };
    }
    
    public static Lazy bitand( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return a.value( states ) & b.value( states );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "&", a, b );
            }
        };
    }
    
    public static Lazy and( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                long check = a.value( states );
                if( check == 0 )
                    return 0;
                check = b.value( states );
                if( check == 0 )
                    return 0;
                return 1;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "&&", a, b );
            }
        };
    }
    
    public static Lazy not( final Lazy a ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return a.value( states ) == 0 ? 1 : 0;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "!", a );
            }
        };
    }
    
    public static Lazy xor( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return a.value( states ) ^ b.value( states );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "^", a, b );
            }
        };
    }
    
    public static Lazy bitreverse( final Lazy a ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return ~a.value( states );
            }          
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "~", a );
            }
        };
    }
    
    public static Lazy eq( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return (a.value( states ) == b.value( states )) ? 1 : 0;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "==", a, b );
            }
        };
    }
    
    public static Lazy neq( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return (a.value( states ) != b.value( states )) ? 1 : 0;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "!=", a, b );
            }
        };
    }
    
    public static Lazy greater( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return (a.value( states ) > b.value( states )) ? 1 : 0;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, ">", a, b );
            }
        };
    }
    

    public static Lazy greatereq( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return (a.value( states ) >= b.value( states )) ? 1 : 0;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, ">=", a, b );
            }
        };
    }
    
    public static Lazy less( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return (a.value( states ) < b.value( states )) ? 1 : 0;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "<", a, b );
            }
        };
    }
    
    public static Lazy lesseq( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return (a.value( states ) <= b.value( states )) ? 1 : 0;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "<=", a, b );
            }
        };
    }
    
    public static Lazy shiftright( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return a.value( states ) >> b.value( states );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, ">>", a, b );
            }
        };
    }
    
    public static Lazy shiftleft( final Lazy a, final Lazy b ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return a.value( states ) << b.value( states );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "<<", a, b );
            }
        };
    }
    
    public static Lazy number( final String number ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                String numberText = number;
                
                while( numberText.endsWith( "L" ) || numberText.endsWith( "l" ) || numberText.endsWith( "U" ) || numberText.endsWith( "u" )){
                    numberText = numberText.substring( 0, numberText.length()-1 );
                }
                
                if( numberText.startsWith( "0x" )){
                    numberText = numberText.substring( 2 );
                    return Long.parseLong( numberText, 16 );
                }
                if( numberText.startsWith( "0" ) && numberText.length() > 1 ){
                    return Long.parseLong( numberText.substring( 1 ), 8 );
                }
                
                return Long.parseLong( numberText );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, number );
            }
        };
    }
    
    public static Lazy condition( final Lazy condition, final Lazy valueTrue, final Lazy valueFalse ){
        return new Lazy(){
            @Override
            public long value( ExprState states ){
                long cond = condition.value( states );
                if( cond != 0 )
                    return valueTrue.value( states );
                else
                    return valueFalse.value( states );
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ){
                toString( builder, tabs, "?", condition, valueTrue, valueFalse );
            }
        };
    }
    
    public static Lazy zero(){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                return 0;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "0" );
            }
        };
    }

    public static Lazy identifier( final String name ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                states.reportMessage( "identifier " + name + " replaced by 0", Insights.expressionIdentifierReplacedByZero() );
                return 0;
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "0" );
            }
        };
    }
    
    public static Lazy macro( final String name ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                if( states.getStates().getMacro( name ) != null )
                    return 1;
                else
                    return 0;
            }
            
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, "defined( " + name + ")" );
            }
        };
    }

    public static Lazy character( final String value ){
        return new Lazy(){
            @Override
            public long value( ExprState states ) {
                String c = value.substring( 1, value.length()-1 );
                if( c.length() == 1 )
                    return c.charAt( 0 );
                
                c = c.substring( 1 );
                
                if( c.startsWith( "x" )){
                    c = c.substring( 1 );
                    try{
                        return Integer.parseInt( c, 16 );
                    }
                    catch( NumberFormatException ex ){
                        states.reportError( "Invalid character: " + value, Insights.expressionInvalidCharacter( value ) );
                        return 0;
                    }
                }
                else{
                    if( c.length() == 1 ){
                        switch( c.charAt( 0 )){
                            // plain old ASCII
                            case 'a': return 7;
                            case 'b': return 8;
                            case 'f': return 12;
                            case 'n': return 10;
                            case 'r': return 13;
                            case 't': return 9;
                            case 'v': return 11;
                            case '\\': return 92;
                            case '?': return 63;
                            case '\'': return 96;
                            case '"': return 34;
                            
                            case '0': return 0;
                            case '1': return 1;
                            case '2': return 2;
                            case '3': return 3;
                            case '4': return 4;
                            case '5': return 5;
                            case '6': return 6;
                            case '7': return 7;
                            
                            default: return 0;
                        }
                    }
                    
                    try{
                        return Integer.parseInt( c, 8 );
                    }
                    catch( NumberFormatException ex ){
                        states.reportError( "Invalid character: " + value, Insights.expressionInvalidCharacter( value ) );
                        return 0;
                    }
                }
            }
            @Override
            protected void toString( StringBuilder builder, int tabs ) {
                toString( builder, tabs, value );
            }
        };
    }
}
