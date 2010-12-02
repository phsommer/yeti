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
package tinyos.yeti.preprocessor;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.Symbol;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.preprocessor.lexer.ConditionalLexer;
import tinyos.yeti.preprocessor.lexer.CountingReader;
import tinyos.yeti.preprocessor.lexer.Macro;
import tinyos.yeti.preprocessor.lexer.MacroLexer;
import tinyos.yeti.preprocessor.lexer.PreprocessorLexer;
import tinyos.yeti.preprocessor.lexer.PreprocessorScanner;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.lexer.PurgingReader;
import tinyos.yeti.preprocessor.lexer.State;
import tinyos.yeti.preprocessor.lexer.Stream;
import tinyos.yeti.preprocessor.output.Insight;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.parser;
import tinyos.yeti.preprocessor.parser.elements.Source;
import tinyos.yeti.preprocessor.process.CancellationException;

/**
 * A preprocessor is a class that can read C-source files and apply a 
 * preprocessor to it.<br>
 * The file is read as a stream and several modules modify this stream. The exact
 * order of modifications goes as follows:
 * <ol>
 * 	<li>File: the file to read</li>
 *  <li>{@link PurgingReader}: reads the file and removes any comments</li>
 *  <li>{@link Stream}s: a set of dynamic streams, organized in a stack, are
 *  replacing identifiers with macros, modules may push new streams onto the
 *  stack any time. Streams have a limited life time (=number of tokens they 
 *  provide) and are afterwards popped from the stack.</li>
 *  <li>{@link PreprocessorLexer}: offers an interface between {@link Stream}s
 *  and {@link PreprocessorScanner}s, also the root-stream (reading the file)
 *  is stored in this object.</li>
 *  <li>{@link MacroLexer}: Recognizes identifiers which can be replaced by
 *  macros. Uses {@link Stream}s to apply macros. Uses information provided
 *  by the {@link ConditionalLexer} to ignore identifiers which need not to
 *  be replaced.</li>
 *  <li>{@link ConditionalLexer}: detects conditional directives and swallows
 *  tokens that should not appear. </li>
 *  <li>{@link parser}: scans the stream one line at a time. Directives are
 *  forwarded to modules which can execute them, normal code is transformed
 *  into a list of tokens and returned to the client.</li>
 * </ol>  
 * Finally the {@link State} object connects all the modules and calculates
 * more complex information as for example whether if-directives have
 * to be evaluated or not.
 * @author Benjamin Sigg
 */
public class Preprocessor {
    private IncludeProvider includeProvider = new IncludeProvider(){
        public IncludeFile searchSystemFile( String filename, IProgressMonitor monitor ) {
            monitor.beginTask( "Null task", 0 );
            monitor.done();
            return null;
        }
        public IncludeFile searchUserFile( String filename, IProgressMonitor monitor ) {
            monitor.beginTask( "Null task", 0 );
            monitor.done();
            return null;
        }
    };
    
    private MessageHandler messageHandler = new MessageHandler(){
        public void handle( Severity severity, String message, Insight information, PreprocessorElement... elements ){
            System.out.println( "[" + severity + "] " + message );
            for( PreprocessorElement e : elements ){
                PreprocessorToken token = e.getToken();
                boolean newline = false;
                if( token != null ){
                    if( token.getFile() != null ){
                        System.out.print( "  in " + token.getFile() );
                        newline = true;
                    }
                    if( token.getLine() >= 0 ){
                        System.out.print( " at " + token.getLine() );
                        newline = true;
                    }
                }
                if( newline )
                    System.out.println();
            }
        }
    };
    
    private FileInfoFactory fileInfoFactory = new FileInfoFactory(){
        public FileInfo createLine( FileInfo current, String newname ){
            File file = new File( current.getPath() );
            File parent = file.getParentFile();
            if( parent == null )
                return new BaseFileInfo( new File( newname ));
            else
                return new BaseFileInfo( new File( parent, newname ));
        }
    };
    
    private List<Macro> macros = new ArrayList<Macro>();
    
    private MacroCallback macroCallback;
    
    private IncludeCallback includeCallback;
    
    private CommentCallback comments;
    
    public Preprocessor(){
    	// nothing
    }
    
    public void addMacro( Macro macro ){
        macros.add( macro );
    }
    
    /**
     * Sets the source for include files.
     * @param includeProvider include files, not <code>null</code>
     */
    public void setIncludeProvider( IncludeProvider includeProvider ) {
        if( includeProvider == null )
            throw new NullPointerException( "includeProvider should not be null" );
        this.includeProvider = includeProvider;
    }
    
    /**
     * Sets the handler for errors, warnings and messages.
     * @param messageHandler the handler, must not be <code>null</code>
     */
    public void setMessageHandler( MessageHandler messageHandler ) {
        if( messageHandler == null )
            throw new NullPointerException( "messageHandler should not be null" );
        this.messageHandler = messageHandler;
    }
    
    /**
     * Sets the factory that will create new file information when needed.
     * @param fileInfoFactory the factory
     */
    public void setFileInfoFactory( FileInfoFactory fileInfoFactory ){
        if( fileInfoFactory == null )
            throw new NullPointerException( "fileInfoFactory must not be null" );
        
        this.fileInfoFactory = fileInfoFactory;
    }

    /**
     * Sets a callback that will be informed whenever a new macro is found
     * @param macroCallback the callback
     */
    public void setMacroCallback( MacroCallback macroCallback ){
        this.macroCallback = macroCallback;
    }
    
    /**
     * Sets a callback that will be informed whenever a file is included.
     * @param includeCallback the callback
     */
    public void setIncludeCallback( IncludeCallback includeCallback ){
		this.includeCallback = includeCallback;
	}
    
    /**
     * The comment observer will be notified about any comment that is read.
     * @param comments the observer, can be <code>null</code>
     */
    public void setComments( CommentCallback comments ){
		this.comments = comments;
	}
    
    /**
     * Parses and processes one file and returns a list of tokens that were found.
     * @param filename the name of the file
     * @param input the contents of the file
     * @param monitor used to cancel this operation
     * @return the parsed and processed file
     * @throws IOException if the file can't be parsed or processed
     */
    public Source process( FileInfo filename, Reader input, IProgressMonitor monitor ) throws IOException{
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        try {
            PurgingReader reader = new PurgingReader( input, filename, null );
            State states = new State( filename, fileInfoFactory, reader, includeProvider, messageHandler, comments, monitor );
            reader.setState( states );
            reader.setTopLevel( true );
            
            parser parser = new parser( states.getScanner() );
            parser.setStates( states );
            Symbol symbol;
        
            for( Macro macro : macros )
                states.putMacro( macro );
            
            states.setMacroCallback( macroCallback );
            states.setIncludeCallback( includeCallback );
            symbol = parser.parse();
            
            Object result = symbol == null ? null : symbol.value;
            Source source = (Source)result;
            
            if( source == null )
                throw new IOException( "Could not parse " + filename );
            
            source.setInput( reader );
            source.calculateLocation( 0 );
            return source;
        }
        catch( CancellationException e ){
            // do nothing
            return null;
        }
        catch( Exception e ) {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
        finally{
            monitor.done();
        }
    }
    
    public void process( FileInfo filename, Reader input, Writer output, IProgressMonitor monitor ) throws IOException{
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Preprocess '" + filename.getName() + "'", 10000 );
        Source source = process( filename, input, new SubProgressMonitor( monitor, 8000 ) );
        if( monitor.isCanceled() ){
            monitor.done();
            return;
        }
        
        source.output( output );
        monitor.done();
    }
    
    public PreprocessorReader open( FileInfo filename, Reader input, IProgressMonitor monitor ) throws IOException{
        CountingReader counting = new CountingReader( input );
        Source source = process( filename, counting, monitor );
        if( source == null )
            return null;
        return new PreprocessorReader( source, filename, counting.getCount(), counting.getNewlines()+1 );
    }
}
