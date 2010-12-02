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
import java.util.LinkedList;

import java_cup.runtime.Symbol;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.lexer.State;
import tinyos.yeti.preprocessor.lexer.Stream;

/**
 * A stream where new symbols can be put in front at runtime.
 * @author Benjamin Sigg
 */
public class PushableStream extends Stream{
    private LinkedList<Symbol> queue = new LinkedList<Symbol>();
    private Stream stream;
    private boolean copy;
    
    public PushableStream( Stream stream, boolean copy ){
        this.stream = stream;
        this.copy = copy;
    }
    
    @Override
    public void setStates( State states ){
        super.setStates( states );
        stream.setStates( states );
    }
    
    @Override
    protected Symbol next() throws IOException {
        if( !queue.isEmpty() ){
            Symbol next = queue.removeFirst();
            if( copy )
                next = states.symbol( (PreprocessorToken)next.value );
            return next;
        }
        
        return stream.read();
    }
    
    @Override
    public void popped() throws IOException {
        stream.popped();
    }
    
    @Override
    public void disable() throws IOException {
    	stream.disable();
    }
    
    @Override
    public void enable() throws IOException {
    	stream.enable();
    }
    
    @Override
    public void pushed() throws IOException {
        stream.pushed();
    }
    
    public void pushTail( Symbol symbol ){
        queue.addLast( symbol );
    }
    
    public void pushHead( Symbol symbol ){
        queue.addFirst( symbol );
    }
}
