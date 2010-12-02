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
import tinyos.yeti.preprocessor.lexer.streams.MacroStream;
import tinyos.yeti.preprocessor.lexer.streams.TrimStream;
import tinyos.yeti.preprocessor.parser.elements.Token;

/**
 * A {@link MacroLexer} can replace some of the tokens it reads
 * with other tokens. This lexer handles everything regarding #define
 * and #undef.
 * @author Benjamin Sigg
 */
public class MacroLexer extends StatedLexer{
    private State states;
    
    private final int STATE_NORMAL            = 0;
    private final int STATE_BEGIN_LINE        = 1;
    private final int STATE_SHARP             = 2;
    private final int STATE_DEFINE            = 3;
    private final int STATE_UNDEF             = 4;
    
    /** whether a string is currently read, in this case macros are not applied */
    private boolean inString = false;
    
    public MacroLexer( PreprocessorScanner original ){
        super( original, 1, 5 );
        
        wire( STATE_NORMAL, Symbols.NEWLINE, STATE_BEGIN_LINE );
        
        wire( STATE_BEGIN_LINE, null, STATE_NORMAL );
        wire( STATE_BEGIN_LINE, Symbols.NEWLINE, STATE_BEGIN_LINE );
        wire( STATE_BEGIN_LINE, Symbols.WHITESPACE, STATE_BEGIN_LINE );
        wire( STATE_BEGIN_LINE, Symbols.SHARP, STATE_SHARP );
        
        wire( STATE_SHARP, null, STATE_NORMAL );
        wire( STATE_SHARP, Symbols.WHITESPACE, STATE_SHARP );
        wire( STATE_SHARP, Symbols.NEWLINE, STATE_BEGIN_LINE );
        wire( STATE_SHARP, Symbols.K_DEFINE, STATE_DEFINE );
        wire( STATE_SHARP, Symbols.K_UNDEF, STATE_UNDEF );
        
        wire( STATE_DEFINE, Symbols.NEWLINE, STATE_BEGIN_LINE );
        
        wire( STATE_UNDEF, Symbols.NEWLINE, STATE_BEGIN_LINE );
    }
    
    public void setStates( State states ) {
        this.states = states;
    }
    
    /**
     * Reads the next symbol, replaces an identifier by a macro if necessary.
     * The exact rules how to determine the next symbol are as follows.
     * <ol>
     *  <li>If macro replacement is turned of because of a swallowed block 
     *  (see {@link State#shouldIgnoreMacros()}) then just the next symbol of
     *  the underlying stream is returned.</li>
     *  <li>If the next symbol is not an identifier, than it is returned anyway.</li>
     *  <li>If <code>#define</code> or <code>#undefine</code> was just read, then an
     *  identifier is not replaced.</li>
     *  <li>If the {@link ConditionalLexer} requests the next identifier to
     *  be read (see {@link ConditionalLexer#isReadingDefinedIdentifier()}),
     *  then it is not replaced.</li>
     * </ol>
     * @return the next symbol or <code>null</code> in case of end of file.
     */
    @Override
    public Symbol next() throws IOException {
    	if( states.shouldIgnoreMacros() ){
    		return super.next();
    	}
    	
        boolean rewind;
        Symbol next;
        do{
            rewind = false;
            next = super.next();
            
            PreprocessorToken token = (PreprocessorToken)next.value;
            
            if( token.getKind() == Symbols.QUOTE ){
                inString = !inString;
            }
            else if( !inString && token.getKind() == Symbols.IDENTIFIER ){
                if( getState() != STATE_UNDEF && getState() != STATE_DEFINE ){
                    if( !states.getConditionalLexer().isReadingDefinedIdentifier() ){
                        Macro macro = states.getMacro( token.getText() );
                        if( macro != null && !macro.isInProgress() ){
                            states.getBase().push( new TrimStream( new MacroStream( states, new Token( token ), macro ) ));
                            rewind = true;
                        }
                    }
                }
            }
            else if( token.getKind() == Symbols.NEWLINE ){
                inString = false;
            }
        }while( rewind );
        
        return next;
    }    
}
