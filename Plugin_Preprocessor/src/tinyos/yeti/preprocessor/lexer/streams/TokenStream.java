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

/**
 * A stream that has a list of {@link PreprocessorToken}s as source.
 * @author Benjamin Sigg
 */
public class TokenStream extends Stream{
    private List<PreprocessorToken> tokens;
    private int index;
    
    public TokenStream( List<PreprocessorToken> tokens ){
        this.tokens = tokens;
    }
    
    @Override
    protected Symbol next() throws IOException {
        if( index >= tokens.size() )
            return null;
        
        PreprocessorToken token = tokens.get( index++ );
        return states.symbol( token  );
    }
    
    @Override
    public void popped() throws IOException {
        // ignore
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
        index = 0;
    }
}
