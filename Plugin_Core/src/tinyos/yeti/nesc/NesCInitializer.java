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
package tinyos.yeti.nesc;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.INesCInitializer;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc.parser.SimpleDeclaration;

public class NesCInitializer implements INesCInitializer {
    public static void main( String[] args ) throws IOException{
        String test = "\"interface bla \\\" module spong  \" module bumbum{bada}";
        NesCInitializer init = new NesCInitializer();
        IDeclaration[] decls = init.analyze( null, new StringMultiReader( test ), null );
        for( IDeclaration decl : decls )
            System.out.println( decl );
    }
    
    private List<IDeclaration> declarations;
    private IParseFile parseFile;
    
    public void addMacro( IMacro macro ){
        // ignore
    }
    
    public IDeclaration[] analyze( IParseFile parseFile, IMultiReader reader, IProgressMonitor monitor ) throws IOException {
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Analyze '" + parseFile.getName() + "'", 1 );
        
        declarations = new ArrayList<IDeclaration>();
        this.parseFile = parseFile;
        
        Reader content = reader.open();
        
        boolean readQuote = false;
        boolean readApo = false;
        boolean readSlash = false; 
        
        int paranthesisCount = 0;
        
        int read = 0;
        
        WordBuffer buffer = new WordBuffer();
        
        while( (read = content.read()) != -1 ){
            char c = (char)read;
            
            if( c == '\\' ){
                readSlash = !readSlash;
                if( !readQuote && !readApo ){
                    buffer.push( c );
                }
            }
            else if( readSlash && (readQuote || readApo) ){
                readSlash = false;
            }
            else{
                readSlash = false;
                
                if( readApo ){
                    if( c == '\'' )
                        readApo = false;
                }
                else if( readQuote ){
                    if( c == '\"' )
                        readQuote = false;
                }
                else if( c == '\'' ){
                    readApo = true;
                    buffer.push( c );
                }
                else if( c == '\"' ){
                    readQuote = true;
                    buffer.push( c );
                }
                else if( c == '{' ){
                    paranthesisCount++;
                    buffer.push( c );
                    buffer.setToplevel( paranthesisCount == 0 );
                }
                else if( c == '}' ){
                    paranthesisCount--;
                    buffer.push( c );
                    buffer.setToplevel( paranthesisCount == 0 );
                }
                else{
                    buffer.push( c );
                }
            }
        }
        
        content.close();
        content.close();
        
        IDeclaration[] result = declarations.toArray( new IDeclaration[ declarations.size() ] );
        declarations = null;
        monitor.done();
        return result;
    }
    
    private void putInterface( String name ){
        put( name, Kind.INTERFACE, Tag.INTERFACE );
    }
    private void putModule( String name ){
        put( name, Kind.MODULE, Tag.COMPONENT, Tag.MODULE );
    }
    private void putConfiguration( String name ){
        put( name, Kind.CONFIGURATION, Tag.COMPONENT, Tag.CONFIGURATION );
    }
    
    private void put( String name, Kind kind, Tag... tags ){
        SimpleDeclaration declaration = new SimpleDeclaration( name, parseFile, kind, TagSet.get( tags ));
        declarations.add( declaration );
    }
    
    private class WordBuffer{
        private StringBuilder builder = new StringBuilder();
        
        private boolean armInterface = false;
        private boolean armModule = false;
        private boolean armConfiguration = false;
        
        private char[] nameInterface = "interface".toCharArray();
        private char[] nameModule = "module".toCharArray();
        private char[] nameConfiguration = "configuration".toCharArray();
        
        private boolean toplevel = true;
        
        public void setToplevel( boolean toplevel ) {
            this.toplevel = toplevel;
            
            armInterface = false;
            armModule = false;
            armConfiguration = false;
        }
        
        public void push( char c ){
            if( builder.length() == 0 ){
                if( ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') ){
                    builder.append( c );
                }
            }
            else{
                if( Character.isJavaIdentifierPart( c ))
                    builder.append( c );
                else{
                    complete();
                }
            }
        }
        
        private void complete(){
            if( toplevel ){
                if( armInterface ){
                    putInterface( builder.toString() );
                    armInterface = false;
                }
                else if( armModule ){
                    putModule( builder.toString() );
                    armModule = false;
                }
                else if( armConfiguration ){
                    putConfiguration( builder.toString() );
                    armConfiguration = false;
                }
                else if( equals( nameInterface ))
                    armInterface = true;
                else if( equals( nameModule ))
                    armModule = true;
                else if( equals( nameConfiguration ))
                    armConfiguration = true;
            }
            
            builder.setLength( 0 );
        }
        
        private boolean equals( char[] name ){
            if( name.length != builder.length() )
                return false;
            
            for( int i = 0, n = name.length; i<n; i++ ){
                if( name[i] != builder.charAt( i ))
                    return false;
            }
            
            return true;
        }
    }
}
