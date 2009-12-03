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
package tinyos.yeti.preprocessor.lexer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import java_cup.runtime.Symbol;

/**
 * A stream reads symbols from some unknown source.
 * @author Benjamin Sigg
 */
public abstract class Stream {
    private Stream pushed;
    private List<Stream> garbage = new LinkedList<Stream>();
    private boolean recursion = false;
    protected State states;
    
    /**
     * Reads a symbol from this stream.
     * @return the next symbol or <code>null</code> if this stream is finished
     */
    public Symbol read() throws IOException{
    	boolean recursion = this.recursion;
    	this.recursion = true;
    	try{
    		while( true ){
	        	Stream pushed = this.pushed;
	        	
	        	do{
	        		pushed = this.pushed;
		            if( pushed != null ){
		            	Symbol next = pushed.read();
		                if( next != null ){
		                	if( this.pushed == null ){
		                		// reassign the stream
		                		this.pushed = pushed;
		                		garbage.remove( pushed );
		                		pushed.enable();
		                	}
		                    return next;
		                }
		            
		                // this method can be called recursively if for example a
		                // macro-stream reads ahead to find its parameters.
		                if( pushed == this.pushed ){
		                	pushed.disable();
		                	garbage.add( pushed );
		                	this.pushed = null;
		                	break;
		                }
		            }
	            }
	            while( pushed != this.pushed );
	            
	            Symbol next = next();
	            if( next != null )
	                return next;
	            if( this.pushed == null )
	                return null;
	        }
    	}
    	finally{
    		this.recursion = recursion;
    		if( !recursion ){
    			for( Stream stream : garbage ){
    				stream.popped();
    			}
    			garbage.clear();
    		}
    	}
    }
    
    public Stream getPushed() {
        return pushed;
    }
    
    /**
     * Lets read symbols from another stream.
     * @param stream the stream to read symbols from
     */
    public void push( Stream stream ) throws IOException{
        if( pushed != null ){
        	// recursively push the stream to the top
        	pushed.push( stream );
        	
            // throw new IllegalStateException( "Can't push more than one stream" );
        }
        else{
        	if( stream.getPushed() != null || stream == this )
        		throw new IllegalStateException( "Stream is already on stack" );

        	pushed = stream;
        	pushed.setStates( states );
        	stream.pushed();
        }
    }
    
    public void setStates( State states ){
        this.states = states;
        if( pushed != null )
            pushed.setStates( states );
    }
    
    public State getStates(){
        return states;
    }
    
    /**
     * Reads the next symbol. Returns <code>null</code> either if no new
     * symbols can be found, or when the symbols should be read from a stream
     * that was {@link #push(Stream) pushed}.
     * @return the next symbol or <code>null</code>
     */
    protected abstract Symbol next() throws IOException;
    
    /**
     * Called when this stream is first pushed onto the stack.
     */
    public abstract void pushed() throws IOException;
    
    /**
     * Informs this stream that it is no longer in the stack.
     */
    public abstract void disable() throws IOException;
    
    /**
     * Informs this stream that it was reassigned to the stack. Note:
     * this method is not called the first time this stream is pushed onto the
     * stack.
     */
    public abstract void enable() throws IOException;
    
    /**
     * Called when this stream is permanently popped from the stack. A call
     * to this method is always preceded by a call to {@link #disable()}
     */
    public abstract void popped() throws IOException;
}
