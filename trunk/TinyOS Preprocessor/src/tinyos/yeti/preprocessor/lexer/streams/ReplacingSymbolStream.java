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
import tinyos.yeti.preprocessor.lexer.*;
import tinyos.yeti.preprocessor.parser.elements.Token;

/**
 * A stream that forces a direct macro replacement for each identifier as long
 * as the identifier is not in a string.
 * @author Benjamin Sigg
 */
public class ReplacingSymbolStream extends Stream{
    private boolean inString = false;
    private Stream symbols;
    
    public ReplacingSymbolStream( Stream symbols ) {
        this.symbols = symbols;
    }

    @Override
    public void setStates( State states ) {
        super.setStates( states );
        symbols.setStates( states );
    }
    
    @Override
    public void pushed() throws IOException {
        symbols.pushed();
    }
    
    @Override
    public void disable() throws IOException {
    	symbols.disable();
    }
    
    @Override
    public void enable() throws IOException {
    	symbols.enable();
    }
    
    @Override
    public void popped() throws IOException {
        symbols.popped();
    }
    
    @Override
    protected Symbol next() throws IOException {
        Symbol next = symbols.read();
        if( next == null )
            return null;
        
        if( next.sym == Symbols.QUOTE.sym() ){
            inString = !inString;
        }
        else if( next.sym == Symbols.NEWLINE.sym() ){
            inString = false;
        }
        else if( !inString && !states.getConditionalLexer().isReadingDefinedIdentifier() ){
            PreprocessorToken token = (PreprocessorToken)next.value;
            switch( token.getKind() ){
                case IDENTIFIER:
                case K_DEFINE:
                case K_UNDEF:
                case K_INCLUDE:
                case K_IF:
                case K_ELIF:
                case K_ENDIF:
                    String name = token.getText();
                    Macro macro = states.getMacro( name );
                    if( macro != null && !macro.isInProgress() ){
                        push( new MacroStream( states, new Token( token ), macro ) );
                        return null;
                    }
            }
        }
        
        return next;
    }
}
