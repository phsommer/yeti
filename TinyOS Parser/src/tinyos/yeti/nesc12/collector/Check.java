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
package tinyos.yeti.nesc12.collector;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.INesCDefinitionCollectorCallback;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.model.ParseFile;
import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.ScopeStack;
import tinyos.yeti.preprocessor.Preprocessor;
import tinyos.yeti.preprocessor.PreprocessorReader;

/**
 * This class is used to debug the {@link Lexer}.
 * @author Benjamin Sigg
 * @deprecated only for debugging
 */
@Deprecated
public class Check{
    public static void main( String[] args ) throws Exception{
        String[] files = new String[]{
                // "bin/data/tinyos/parser/tests/core_cia.c"
                // "bin/data/tinyos/parser/tests/mini.c"
                // "bin/data/tinyos/parser/tests/InclusionTest.txt"
                // "bin/data/tinyos/parser/tests/A.txt"
                "bin/data/tinyos/parser/tests/ModA.txt"
        };

        for( String file : files ){
            System.out.println( "File: " + file );
            checkLexer( file );
            checkParser( file );
        }
    }
    
    private static void checkLexer( String file ) throws Exception{
        Map<Integer, String> kinds = kinds();

        FileReader reader = new FileReader( file );

        Preprocessor p = new Preprocessor();
        PreprocessorReader preader = p.open( new NesC12FileInfo( new ParseFile( new File( file ), null ){
            public boolean isProjectFile(){
                return false;
            }
            public IContainer getProjectSourceContainer(){
            	return null;
            }
        }), reader, null );

        Scanner lexer;
        Lexer lex = new CollectorLexer( preader );
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
    }
    
    private static void checkParser( String file ) throws Exception{
        IncludingParser parser = new IncludingParser( null, new ParseFile( new File( file ), null ){
            public boolean isProjectFile(){
                return false;
            }
            public IContainer getProjectSourceContainer(){
            	return null;
            }
        });
        parser.parse( new FileMultiReader( new File( file ) ), new INesCDefinitionCollectorCallback(){
            public void declarationFound( IDeclaration declaration ){
                System.out.println( "found: " + declaration.getLabel() );
            }
            public void macroDefined( IMacro macro ){
                System.out.println( "declared: " + macro.getName() );
            }
            public void macroUndefined( String name ){
                System.out.println( "undeclared: " + name );
            }
            public void elementIncluded( String name, IProgressMonitor monitor, Kind... kind ){
                System.out.println( "include: " + name );
            }
            public void fileIncluded(String parseFile, boolean requireLoad, IProgressMonitor monitor) {
            	System.out.println( "included: " + parseFile );
            }
            public void fileIncluded( File file, boolean requireLoad, IProgressMonitor monitor){
                System.out.println( "included: " + file.getName() );
            }
        }, new NullProgressMonitor());
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
}
