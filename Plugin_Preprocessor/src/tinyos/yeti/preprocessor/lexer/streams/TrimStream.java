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
import tinyos.yeti.preprocessor.lexer.State;
import tinyos.yeti.preprocessor.lexer.Stream;
import tinyos.yeti.preprocessor.lexer.Symbols;

/**
 * A stream reading from another stream and deleting all whitespaces and newlines
 * at the beginning and end of that other stream
 * @author Benjamin Sigg
 */
public class TrimStream extends Stream{
    private PushableStream stream;
    private boolean first;
    private int jump;
    
    public TrimStream( Stream stream ){
        this.stream = new PushableStream( stream, false );
    }
    
    @Override
    public void setStates( State states ){
        super.setStates( states );
        stream.setStates( states );
    }
    
    @Override
    protected Symbol next() throws IOException {
        if( first ){
            first = false;
            
            while( true ){
                Symbol next = stream.read();
                if( next == null )
                    return null;
                if( next.sym != Symbols.WHITESPACE.sym() && next.sym != Symbols.NEWLINE.sym() )
                    return next;
            }
        }
        
        LinkedList<Symbol> buffer = null;
        while( true ){
            Symbol next = stream.read();
            if( jump > 0 ){
                jump--;
                return next;
            }
            
            if( next == null ){
                return null;
            }
            
            if( next.sym == Symbols.WHITESPACE.sym() || next.sym == Symbols.NEWLINE.sym() ){
                if( buffer == null )
                    buffer = new LinkedList<Symbol>();
                buffer.addLast( next );
            }
            else{
                if( buffer != null ){
                    jump = buffer.size();
                    for( Symbol sym : buffer )
                        stream.pushTail( sym );
                    stream.pushTail( next );
                    return stream.read();
                }
                return next;
            }
        }
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
        first = true;
        jump = 0;
        stream.pushed();
    }
}
