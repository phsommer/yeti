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
package tinyos.yeti.preprocessor.lexer.streams;

import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;
import tinyos.yeti.preprocessor.IncludeFile;
import tinyos.yeti.preprocessor.lexer.PreprocessorLexer;
import tinyos.yeti.preprocessor.lexer.PurgingReader;
import tinyos.yeti.preprocessor.lexer.State;
import tinyos.yeti.preprocessor.lexer.Stream;
import tinyos.yeti.preprocessor.lexer.Symbols;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * A stream lexing the contents of one file and returning its tokens.
 * @author Benjamin Sigg
 */
public class IncludeStream extends Stream{
    private PurgingReader reader;
    
    private PreprocessorElement filename;
    private IncludeFile file;
    
    private State states;
    private int line;
    private PreprocessorLexer lexer;
    
    /**
     * Creates a new stream
     * @param states the states of this preprocessor
     * @param filename the name of the file that gets included as it was found
     * in the stream
     * @param file the file that gets included, might be <code>null</code>
     */
    public IncludeStream( State states, PreprocessorElement filename, IncludeFile file ){
        this.states = states;
        this.filename = filename;
        this.file = file;
    }
    
    public IncludeFile getFile() {
        return file;
    }
    
    public PreprocessorElement getFilename() {
        return filename;
    }
    
    public PreprocessorLexer getLexer() {
        return lexer;
    }
    
    @Override
    protected Symbol next() throws IOException {
        if( lexer == null ){
            return null;
        }
        
        Symbol next = lexer.next();
        if( next.sym == Symbols.EOF.sym() ){
            lexer = null;
            return next();
        }
        
        return next;
    }
    
    @Override
    public void popped() throws IOException {
    	lexer = null;
        
        if( reader != null ){
            reader.close();
            reader = null;
        }
    }
    
    @Override
    public void disable() throws IOException {
    	states.changeIncludeNesting( null );
    	states.getBase().setLine( line );
    }
    
    @Override
    public void enable() throws IOException {
    	states.changeIncludeNesting( this );
    }
    
    @Override
    public void pushed() throws IOException {
        states.changeIncludeNesting( this );
        
        Reader fileReader = file.read();
        if( fileReader != null ){
            line = states.getBase().getLine();
            states.getBase().setLine( 1 );
            
            reader = new PurgingReader( fileReader, file.getFile(), states );
            lexer = new PreprocessorLexer( states, file.getFile(), reader );
        }
    }
}
