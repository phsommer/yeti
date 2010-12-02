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
package tinyos.yeti.nesc12.ep;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.Debug;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.NullParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.INesCInitializer;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.IMultiReader;
import tinyos.yeti.nesc.StringMultiReader;
import tinyos.yeti.nesc12.ep.declarations.BaseDeclaration;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.ProjectIncludeProvider;
import tinyos.yeti.nesc12.parser.preprocessor.macro.PredefinedMacro;
import tinyos.yeti.preprocessor.IncludeProvider;
import tinyos.yeti.preprocessor.MessageHandler;
import tinyos.yeti.preprocessor.Preprocessor;
import tinyos.yeti.preprocessor.PreprocessorReader;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.output.Insight;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

public class NesC12Initializer implements INesCInitializer, MessageHandler {
    public static void main( String[] args ) throws IOException{
        String test = "\"interface bla \\\" module spong  \" module bumbum{bada}";
        NesC12Initializer init = new NesC12Initializer( null );
        IDeclaration[] decls = init.analyze( null, new StringMultiReader( test ), null );
        for( IDeclaration decl : decls )
            System.out.println( decl );
    }
    
    private IncludeProvider includeProvider;
    
    private List<IDeclaration> declarations;
    private IParseFile parseFile;
    
    private List<IMacro> macros = new ArrayList<IMacro>();
    
    public NesC12Initializer( IProject project ){
        if( project != null ){
        	try{
	            ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
	            if( tos != null ){
	                includeProvider = new ProjectIncludeProvider( tos );
	            }
        	}
        	catch( MissingNatureException ex ){
        		// ignore
        	}
        }
    }
    
    public void handle( Severity severity, String message, Insight insight, PreprocessorElement... elements ) {
        switch( severity ){
            case ERROR:
                Debug.error( message );
                break;
            case WARNING:
                Debug.warning( message );
                break;
            case MESSAGE:
                Debug.info( message );
                break;
        }
    }
    
    public void addMacro( IMacro macro ){
        macros.add( macro );
    }
    
    public IDeclaration[] analyze( IParseFile parseFile, IMultiReader reader, IProgressMonitor monitor ) throws IOException {
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Analyze", 1000 );
        
        declarations = new ArrayList<IDeclaration>();
        this.parseFile = parseFile;
        
        Reader content = reader.open();
        Preprocessor preprocessor = new Preprocessor();
        for( IMacro macro : macros )
            preprocessor.addMacro( PredefinedMacro.instance( macro ) );
        if( includeProvider != null )
            preprocessor.setIncludeProvider( includeProvider );
        preprocessor.setMessageHandler( this );
        PreprocessorReader processed = preprocessor.open( new NesC12FileInfo( NullParseFile.NULL ), content, new SubProgressMonitor( monitor, 950 ) );
  
        if( monitor.isCanceled() )
            return null;
        
        boolean readQuote = false;
        boolean readApo = false;
        boolean readSlash = false; 
        
        int paranthesisCount = 0;
        
        int read = 0;
        
        WordBuffer buffer = new WordBuffer( processed );
        
        while( (read = processed.read()) != -1 ){
            char c = (char)read;
            buffer.offset++;
            
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
        processed.close();
        
        IDeclaration[] result = declarations.toArray( new IDeclaration[ declarations.size() ] );
        declarations = null;
        monitor.done();
        return result;
    }
    
    private void putInterface( String name, int offset, int length ){
        put( name, offset, length, Kind.INTERFACE, Tag.INTERFACE );
    }
    private void putModule( String name, int offset, int length, boolean generic ){
        if( generic )
            put( name, offset, length, Kind.MODULE, Tag.COMPONENT, Tag.MODULE, NesC12ASTModel.GENERIC );
        else
            put( name, offset, length, Kind.MODULE, Tag.COMPONENT, Tag.MODULE );
    }
    private void putConfiguration( String name, int offset, int length, boolean generic ){
        if( generic )
            put( name, offset, length, Kind.CONFIGURATION, Tag.COMPONENT, Tag.CONFIGURATION, NesC12ASTModel.GENERIC );
        else
            put( name, offset, length, Kind.CONFIGURATION, Tag.COMPONENT, Tag.CONFIGURATION );
    }
    private void putBinaryComponent( String name, int offset, int length, boolean generic ){
        if( generic )
            put( name, offset, length, Kind.BINARY_COMPONENT, Tag.COMPONENT, Tag.BINARY_COMPONENT, NesC12ASTModel.GENERIC );
        else
            put( name, offset, length, Kind.BINARY_COMPONENT, Tag.COMPONENT, Tag.BINARY_COMPONENT );
    }
    
    private void put( String name, int offset, int length, Kind kind, Tag... tags ){
        ASTModelPath path = new ASTModelPath( parseFile, parseFile.getPath(), name );
        BaseDeclaration declaration = new BaseDeclaration( kind, name, name, parseFile, path, TagSet.get( tags ) );
        if( offset >= 0 ){
            declaration.setFileRegion( new FileRegion( parseFile, offset, length, -1 ) );
        }
        declarations.add( declaration );
    }
    
    private class WordBuffer{
        private int offset;
        private StringBuilder builder = new StringBuilder();
        
        private boolean armGeneric = false;
        private boolean armInterface = false;
        private boolean armModule = false;
        private boolean armConfiguration = false;
        private boolean armBinaryComponent = false;
        
        private char[] nameGeneric = "generic".toCharArray();
        private char[] nameInterface = "interface".toCharArray();
        private char[] nameModule = "module".toCharArray();
        private char[] nameConfiguration = "configuration".toCharArray();
        private char[] nameComponent = "component".toCharArray();
        
        private boolean toplevel = true;
        
        private PreprocessorReader reader;
        
        public WordBuffer( PreprocessorReader reader ){
            this.reader = reader;
        }
        
        public void setToplevel( boolean toplevel ) {
            this.toplevel = toplevel;
            
            armInterface = false;
            armModule = false;
            armConfiguration = false;
            armBinaryComponent = false;
            armGeneric = false;
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
                if( armInterface || armModule || armConfiguration || armBinaryComponent ){
                    int left = -1;
                    int right = -1;

                    RangeDescription range = reader.range( offset-builder.length()-1, offset-1, true );
                    if( range.getRootCount() > 0 ){
                    	RangeDescription.Range root = range.getRoot( 0 );
                    	left = root.left();
                    	right = root.right();
                    }

                    int offset = -1;
                    int length = 0;

                    if( left >= 0 && right >= left ){
                        offset = left;
                        length = right - left;
                    }

                    if( armInterface ){
                        putInterface( builder.toString(), offset, length );
                        armInterface = false;
                    }
                    else if( armModule ){
                        putModule( builder.toString(), offset, length, armGeneric );
                        armModule = false;
                        armGeneric = false;
                    }
                    else if( armConfiguration ){
                        putConfiguration( builder.toString(), offset, length, armGeneric );
                        armConfiguration = false;
                        armGeneric = false;
                    }
                    else if( armBinaryComponent ){
                        putBinaryComponent( builder.toString(), offset, length, armGeneric );
                        armBinaryComponent = false;
                        armGeneric = false;
                    }
                }
                else if( equals( nameInterface ))
                    armInterface = true;
                else if( equals( nameModule ))
                    armModule = true;
                else if( equals( nameConfiguration ))
                    armConfiguration = true;
                else if( equals( nameComponent ))
                    armBinaryComponent = true;
                else if( equals( nameGeneric ))
                    armGeneric = true;
                else
                    armGeneric = false;
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
