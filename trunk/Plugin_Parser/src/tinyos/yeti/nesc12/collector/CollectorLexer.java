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
package tinyos.yeti.nesc12.collector;

import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;
import tinyos.yeti.nesc12.parser.RawLexer;
import tinyos.yeti.nesc12.parser.sym;

public class CollectorLexer extends Lexer implements RawLexer{
    private Symbol follow;
    private Symbol cache;

    public CollectorLexer( Reader reader ){
        super( reader );
    }
    
    @Override
    protected void sendLater( Symbol symbol ) {
        follow = new Symbol( sym.S_FOLLOW, symbol.left, symbol.right, symbol.value );
    }
    
    @Override
    public Symbol next_token() throws IOException {
        if( cache != null ){
            Symbol result = cache;
            cache = null;
            return result;
        }
        if( follow != null ){
            Symbol result = follow;
            follow = null;
            return result;
        }
        return super.next_token();
    }


    public boolean nextIsEOF() throws IOException{
        if( follow != null )
            return false;
        
        if( cache == null ){
            cache = next_token();
        }
        
        if( cache == null )
            return true;
        
        return cache.sym == sym.EOF;
    }    

}
