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
package tinyos.yeti.preprocessor.lexer;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import tinyos.yeti.preprocessor.FileInfo;

import java_cup.runtime.Scanner;

/**
 * Calls jflex with the file from which jflex will create the lexer and
 * contains code to check the result.
 */
public class CreateAndCheck {
    public static void main( String[] args ) throws Exception{
        if( "create".equals( args[0] ))
            create();
        
        if( "check".equals( args[0] ))
            check();
    }
    
    public static void runMain( String className, String... args ){
        try{
            Class<?> clazz = Class.forName( className );
            Method main = clazz.getMethod( "main", String[].class );
            main.invoke( null, new Object[]{ args } );
        }
        catch( Exception e ){
            e.printStackTrace();
        }
    }
        
    public static void create(){
        String dir = "src/tinyos/yeti/preprocessor/lexer";
        String name = "bin/data/tinyos/preprocessor/tokens.jflex";
        
        runMain( "JFlex.Main", "-d", dir, name );
    }
    
    public static void check() throws Exception{
        String[] files = new String[]{
              //"bin/data/tinyos/preprocessor/tests/mini.h"
              "bin/data/tinyos/preprocessor/tests/demo.h"
        };
        
        for( final String file : files ){
            System.out.println( "File: " + file );
            FileInputStream in = new FileInputStream( file );
            PurgingReader reader = new PurgingReader( new InputStreamReader( in ), null, null );
            
            Scanner lexer;
            
            Lexer lex = new Lexer( reader );
            lex.setReader( reader );
            lex.setFile( new FileInfo(){
                public String getPath(){
                    return file;
                }
                public String getName(){
                    return file;
                }
            });
            
            lexer = lex;
            
            
            List<PreprocessorToken> tokens = new LinkedList<PreprocessorToken>();
            
            System.out.println( "TOKENS:" );
            while( true ){
                PreprocessorToken next = (PreprocessorToken)lexer.next_token().value;
                tokens.add( next );
                System.out.println( next.getKind() + " [" + toString( next.getText() ) + "]" );
                if( next.getKind() == Symbols.EOF )
                    break;
            
            }
            in.close();
            
            System.out.println( "SOURCE:" );
            in = new FileInputStream( file );
            Reader read = new InputStreamReader( in );
            
            int current = 0;
            loop: for( PreprocessorToken token : tokens ){
                int[] begins = token.getBegin();
                int[] ends = token.getEnd();
                System.out.print( token.getKind().name() );
                System.out.print( " [" );
                System.out.print( toString( token.getText() ));
                System.out.print( "][" );

                if( begins != null && ends != null ){
                    for( int i = 0, n = begins.length; i<n; i++ ){
                        int begin = begins[i];
                        int end = ends[i];
                        
                        while( current < end ){
                            int next = read.read();
                            if( next == -1 ){
                                System.out.println( "BREAK" );
                                break loop;
                            }
                            
                            current++;
                            if( current >= begin ){
                                System.out.print( toString( (char)next ));
                            }
                        }        
                    }
                }
                
                System.out.println( "]" );
            }
            
            read.close();
        }
    }
    
    private static String toString( char value ){
        if( value == '\n')
            return "\\n";
        if( value == '\r')
            return "\\r";
        
        return String.valueOf( value );
    }
    
    private static String toString( Object value ){
        if( value == null )
            return "null";
        
        String check = value.toString();
        return check.replaceAll( "\n", "\\\\n" ).replaceAll( "\r", "\\\\r" );
    }
}
