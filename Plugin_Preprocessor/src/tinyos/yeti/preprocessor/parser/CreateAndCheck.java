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
package tinyos.yeti.preprocessor.parser;

import java.io.*;

import tinyos.yeti.preprocessor.BaseFileInfo;
import tinyos.yeti.preprocessor.Preprocessor;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.parser.elements.Source;
import tinyos.yeti.preprocessor.parser.stream.ElementStream;

/**
 * Class which creates and checks the parser.
 */
public class CreateAndCheck {
    public static void main( String[] args ) throws Exception{
        if( "create".equals( args[0] ))
            create();
        
        if( "check".equals( args[0] ))
            check();
    }
    
    private static void create() throws Exception{
        String name = "bin/data/tinyos/preprocessor/parser.cup";
        System.setIn( new FileInputStream( name ) );
        
        tinyos.yeti.preprocessor.lexer.CreateAndCheck.runMain( 
                "java_cup.Main",
                "-package", "tinyos.yeti.preprocessor.parser",
                "-expect", "0",
                "-nopositions"
        );
        
        // copy the files
        move( "parser.java", "src/tinyos/yeti/preprocessor/parser/parser.java" );
        move( "sym.java", "src/tinyos/yeti/preprocessor/parser/sym.java" );
    }
    
    private static void move( String source, String dest ) throws IOException{
        System.out.println( "Move " + source + " to " + dest );
        
        FileInputStream in = new FileInputStream( source );
        FileOutputStream out = new FileOutputStream( dest );
        
        int next;
        while( (next=in.read()) != -1 )
            out.write( next );
        
        in.close();
        out.close();
        
        new File( source ).delete();
        System.out.println( "... done" );
    }
    
    private static void check() throws Exception{
        String[] files = new String[]{
                //"bin/data/tinyos/preprocessor/tests/complex.h"
                "bin/data/tinyos/preprocessor/tests/mini.h"
                // "bin/data/tinyos/preprocessor/tests/demo.h"
        };
       
        for( String file : files ){
            Preprocessor preprocessor = new Preprocessor();
            
            FileReader reader = new FileReader( file );
            Source source = preprocessor.process( new BaseFileInfo( new File( file ) ), reader, null );
            reader.close();
            
            // System.out.println( source );
            
            System.out.println();
            System.out.println( "OUTPUT" );
            
            ElementStream stream = new ElementStream( source, true );
            while( stream.hasNext() ){
                PreprocessorToken token = stream.next().getToken();
                if( token != null && token.getText() != null ){
                    System.out.print( token.getText() );
                }
            }
            
            System.out.println();
            System.out.println( "DONE" );
            
            
            stream = new ElementStream( source, true );
            while( stream.hasNext() ){
                PreprocessorToken token = stream.next().getToken();
                if( token != null && token.getText() != null ){
                    System.out.print( token.getText() );
                    System.out.print( " [" );
                    System.out.print( token.getLine() );
                    System.out.print( " - " );
                    
                    int[] begin = token.getBegin();
                    int[] end = token.getEnd();
                    
                    if( begin != null && end != null ){
                        for( int i = 0; i < begin.length; i++ ){
                            System.out.print( "(" );
                            System.out.print( begin[i] );
                            System.out.print( "," );
                            System.out.print( end[i] );
                            System.out.print( ")" );
                        }
                    }
                    
                    System.out.println( "]" );
                }
            } 
        }
    }
}
