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

import java.io.*;

public class CreateAndCheck {
    public static void main( String[] args ) throws Exception{
        if( "create".equals( args[0] )){
            createLexer();
            createParser();
        }
        
        if( "check".equals( args[0] )){
            check();
        }
    }
    
    private static void check() throws Exception{
        String[] tests = new String[]{
                "0x100"
        };
        for( String test : tests ){
            parser p = new parser( new Lexer( new StringReader( test ) ));
            Lazy lazy = (Lazy)p.parse().value;
            
            System.out.println( test + " = " + lazy.value( null ) );
            System.out.println( lazy );
        }
    }
    
    private static void createLexer(){
        String dir = "src/tinyos/yeti/preprocessor/expression";
        String name = "bin/data/tinyos/preprocessor/expression_tokens.jflex";
        
        tinyos.yeti.preprocessor.lexer.CreateAndCheck.runMain( "JFlex.Main", "-d", dir, name );
    }
    
    private static void createParser() throws Exception{
        String name = "bin/data/tinyos/preprocessor/expression_parser.cup";
        System.setIn( new FileInputStream( name ) );

        tinyos.yeti.preprocessor.lexer.CreateAndCheck.runMain( "java_cup.Main",
                "-package", "tinyos.yeti.preprocessor.expression",
                "-expect", "0" );
        
        // copy the files
        move( "parser.java", "src/tinyos/yeti/preprocessor/expression/parser.java" );
        move( "sym.java", "src/tinyos/yeti/preprocessor/expression/sym.java" );
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
}
