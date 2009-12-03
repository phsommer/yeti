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
package tinyos.yeti.nesc12.parser;

import java.io.File;
import java.util.LinkedHashSet;

import org.eclipse.core.runtime.NullProgressMonitor;

import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ParserMessageHandler;
import tinyos.yeti.nesc12.parser.ast.visitors.ASTPrinterVisitor;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.output.Insight;

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
        
        for( String file : files ){
            System.out.println( "File: " + file );
            FileMultiReader reader = new FileMultiReader( new File( file ) );
            
            Parser parser = new Parser( null );
            // parser.setDebug( true );
            parser.setCreateMessages( true );
            parser.setMessageHandler( new ParserMessageHandler(){

                public void error( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
                    print( "error", message, ranges );
                }

                public void message( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
                    print( "warning", message, ranges );
                }

                public void warning( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
                    print( "message", message, ranges );
                }
                
                private void print( String severity, String message, RangeDescription... ranges ){
                    LinkedHashSet<Integer> lines = new LinkedHashSet<Integer>();
                    for( RangeDescription range : ranges ){
                    	for( int i = 0, n = range.getRootCount(); i<n; i++ ){
                    		RangeDescription.Range root = range.getRoot( i );
                    		lines.add( root.line() );
                    	}
                    }
                    
                    System.out.print( "[" + severity + ", line(s): " );
                    boolean first = true;
                    for( int i : lines ){
                        if( first )
                            first = false;
                        else
                            System.out.print( ", " );
                        
                        System.out.print( i );
                    }
                    System.out.println( "] " + message);
                }
            });
            parser.parse( reader, new NullProgressMonitor() );
            
            
            ASTPrinterVisitor visitor = new ASTPrinterVisitor();
            parser.getRootASTNode().accept( visitor );
            System.out.println( visitor );
            
            /*
            
            Preprocessor p = new Preprocessor();
            PreprocessorReader preader = p.open( file, reader );
        
            Lexer lexer = new Lexer( preader );
            parser parser = new parser( lexer );
            
            lexer.setScopeStack( parser.scopes() );
            
            Symbol result = parser.parse();
            reader.close();
            
            AbstractASTNode ast = (AbstractASTNode)result.value;
            if( ast != null ){
                ASTPrinterVisitor visitor = new ASTPrinterVisitor( preader );
                ast.accept( visitor );
                System.out.println( visitor );
            }*/
        }
    }    
}
