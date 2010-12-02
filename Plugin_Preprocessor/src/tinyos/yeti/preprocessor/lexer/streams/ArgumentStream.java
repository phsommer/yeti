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
import java.util.List;

import java_cup.runtime.Symbol;

import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.lexer.Stream;
import tinyos.yeti.preprocessor.lexer.Symbols;

/**
 * A stream that writes several other streams, separating them with
 * a comma.
 * @author Benjamin Sigg
 */
public class ArgumentStream extends Stream{
    private List<Stream> streams;
    private int index;
    
    public ArgumentStream( List<Stream> streams ){
        this.streams = streams;
    }
    
    @Override
    protected Symbol next() throws IOException {
        if( index < streams.size() ){
            push( streams.get( index ));
            index++;
            
            if( index > 1 ){
                return states.symbol( new PreprocessorToken( 
                        Symbols.COMMA, ",", states.getInclusionPath() ));
            }
        }
        
        return null;
    }
    
    @Override
    public void pushed() throws IOException {
        index = 0;
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
    public void popped() throws IOException {
        // ignore
    }
}
