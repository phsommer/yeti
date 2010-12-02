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
package tinyos.yeti.nesc12.lexer;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import tinyos.yeti.nesc12.parser.ScopeStack;
import tinyos.yeti.nesc12.parser.sym;
import tinyos.yeti.preprocessor.BaseFileInfo;
import tinyos.yeti.preprocessor.Preprocessor;
import tinyos.yeti.preprocessor.PreprocessorReader;

@Deprecated
public class CreateAndCheck {
    public static void main( String[] args ) throws Exception{
        check();
    }
    
    public static void check() throws Exception{
        String[] files = new String[]{
              // "bin/data/tinyos/parser/tests/core_cia.c"
               "bin/data/tinyos/parser/tests/mini.c"
        };
        
        Map<Integer, String> kinds = kinds();
        
        for( String file : files ){
            System.out.println( "File: " + file );
            FileReader reader = new FileReader( file );
            
            Preprocessor p = new Preprocessor();
            PreprocessorReader preader = p.open( new BaseFileInfo( new File( file )), reader, null );
            
            Scanner lexer;
            Lexer lex = new NesCLexer( preader );
            lex.setScopeStack( new ScopeStack( null ) );
            lexer = lex;
            
            
            System.out.println( "TOKENS:" );
            while( true ){
                Symbol symbol = lexer.next_token();
                Token token = (Token)symbol.value;
                if( token != null ){
                    System.out.println( kinds.get( symbol.sym ) + 
                        " [" + token.getText() + " " + token.getLeft() + "/" + token.getRight() + "]" );
                }
                if( symbol.sym == sym.EOF )
                    break;
            
            }
            reader.close();
            /*
            System.out.println( "SOURCE:" );
            in = new FileInputStream( file );
            Reader read = new InputStreamReader( in );
            
            int current = 0;
            loop: for( PreprocessorToken token : tokens ){
                int begin = token.getBegin();
                int end = token.getEnd();
                System.out.print( token.getKind().name() );
                System.out.print( " [" );
                System.out.print( toString( token.getText() ));
                System.out.print( "][" );
                
                while( current <= end ){
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
                
                System.out.println( "]" );
            }
            
            read.close();*/
        }
    }
    
    private static Map<Integer, String> kinds() throws Exception{
        Map<Integer, String> result = new HashMap<Integer, String>();
        Class<sym> clazz = sym.class;
        
        for( Field field : clazz.getFields() ){
            int mod = field.getModifiers();
            if( Modifier.isPublic( mod ) && 
                    Modifier.isStatic( mod ) &&
                    Modifier.isFinal( mod )){
                
                if( field.getType().equals( int.class )){
                    int value = field.getInt( null );
                    result.put( value, field.getName() );
                }
            }
        }
        
        return result;
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
