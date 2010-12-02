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

import java_cup.runtime.Symbol;

/**
 * A lexer that has a state. Various symbols can change the state, so this lexer
 * acts like a stated machine.
 * @author Benjamin Sigg
 *
 */
public class StatedLexer implements PreprocessorScanner{
    private PreprocessorScanner base;
    
    private int state;
    private int[][] transitions;
    
    public StatedLexer( PreprocessorScanner base, int initialState, int stateCount ){
        this.base = base;
        
        state = initialState;
        int tokenCount = Symbols.values().length;
        transitions = new int[ stateCount ][ tokenCount ];
        for( int i = 0; i < stateCount; i++ )
            for( int j = 0; j < tokenCount; j++ )
                transitions[i][j] = i;
    }
    
    public PreprocessorLexer getBase() {
        return base.getBase();
    }

    public int getState() {
        return state;
    }
    
    public void setState( int state ) {
        this.state = state;
    }
    
    public Symbol next() throws IOException {
        Symbol next = base.next();
        PreprocessorToken token = (PreprocessorToken)next.value;
        state = transitions[state][token.getKind().ordinal()];
        return next;
    }
    
    public void wire( int state, Symbols sym, int result ){
        if( result == -1 )
            result = state;
        
        if( sym == null ){
            for( int i = 0; i < transitions[state].length; i++ )
                transitions[state][i] = result;
        }
        else{
            transitions[state][sym.ordinal()] = result;
        }
    }
}
