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

import java_cup.runtime.Symbol;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.lexer.Stream;
import tinyos.yeti.preprocessor.lexer.Symbols;
import tinyos.yeti.preprocessor.parser.sym;

/**
 * A stream that wraps another stream and puts out tokens such that a valid
 * c string is formed (includes a new set of quotes around the string).
 * @author Benjamin Sigg
 */
public class StringStream extends Stream{
    private Stream base;
    private boolean inString;
    
    private boolean first;
    private boolean last;
    private boolean pushed;
    private boolean whitespace;
    
    private Symbol storage;
    
    public StringStream( Stream base ){
        this.base = base;
    }
    
    @Override
    public Symbol read() throws IOException {
        while( true ){
            Symbol next = readNext();
            if( next == null )
                return null;
            
            if( !inString ){
                boolean check = next.sym == sym.WHITESPACE || next.sym == sym.NEWLINE;
                if( check ){
                    if( whitespace ){
                        // omit
                        continue;
                    }
                    else{
                        PreprocessorToken token = (PreprocessorToken)next.value;
                        whitespace = true;
                        return states.symbol( new PreprocessorToken(
                                Symbols.WHITESPACE, token.getFile(), token.getLine(),
                                token.getBegin(), token.getEnd(), " ", states.getInclusionPath() ));
                    }
                }
            }
            whitespace = false;
            return next;
        }
    }
    
    private Symbol readNext() throws IOException{
        if( storage != null ){
            Symbol result = storage;
            storage = null;
            return result;
        }
        
        Symbol read = super.read();
        boolean pushed = this.pushed;
        this.pushed = true;
        
        if( pushed && read != null ){
            PreprocessorToken token = (PreprocessorToken)read.value;
            switch( read.sym ){
                case sym.QUOTE:
                    inString = !inString;
                    read = states.symbol( 
                            new PreprocessorToken(
                                    Symbols.MASKED_QUOTE, token.getFile(),
                                    token.getLine(), token.getBegin(), 
                                    token.getEnd(), "\\\"", states.getInclusionPath() ));
                    break;
                case sym.MASKED_QUOTE:
                    if( inString ){
                        storage = read;
                    }
                    else{
                        inString = true;
                        storage = states.symbol(  
                                new PreprocessorToken(
                                        Symbols.QUOTE, token.getFile(),
                                        token.getLine(), token.getBegin(),
                                        token.getEnd(), "\"", states.getInclusionPath() ));
                    }
                    read = states.symbol( 
                            new PreprocessorToken(
                                    Symbols.TEXT, token.getFile(),
                                    token.getLine(), token.getBegin(),
                                    token.getEnd(), "\\\\", states.getInclusionPath() ));
                    break;
                default:
                    String text = token.getText();
                    String change = text.replaceAll( "\\\\", "\\\\" );
                    if( !text.equals( change )){
                        read = states.symbol( 
                                new PreprocessorToken(
                                        Symbols.TEXT, token.getFile(), 
                                        token.getLine(), token.getBegin(),
                                        token.getEnd(), change, states.getInclusionPath() ));
                    }
            }
        }
        
        return read;
    }

    @Override
    protected Symbol next() throws IOException {
        if( first ){
            push( base );
        }
        
        if( first || last ){
            pushed = false;
            
            if( first )
                first = false;
            else
                last = false;
            
            return states.symbol(  
                    new PreprocessorToken( Symbols.QUOTE, "\"", states.getInclusionPath() ));    
        }
        
        return null;
    }

    @Override
    public void popped() throws IOException {
        // nothing to do
    }
    
    @Override
    public void disable() throws IOException {
    	// ignore
    }
    
    @Override
    public void enable() throws IOException {
    	// ignore
    }
    
    @Override
    public void pushed() throws IOException {
        whitespace = false;
        pushed = false;
        inString = false;
        first = true;
        last = true;
    }
}
