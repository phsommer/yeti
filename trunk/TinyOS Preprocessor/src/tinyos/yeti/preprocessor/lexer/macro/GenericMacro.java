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
package tinyos.yeti.preprocessor.lexer.macro;

import java.io.IOException;
import java.io.StringReader;

import java_cup.runtime.Symbol;
import tinyos.yeti.preprocessor.lexer.PreprocessorLexer;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.lexer.PurgingReader;
import tinyos.yeti.preprocessor.lexer.Symbols;
import tinyos.yeti.preprocessor.parser.elements.Token;
import tinyos.yeti.preprocessor.parser.elements.TokenSequence;

/**
 * A generic macro uses a lexer to transform a line given as {@link String} into
 * a {@link TokenSequence}.
 * @author Benjamin Sigg
 */
public class GenericMacro extends DefaultMacro {
    /**
     * Creates a new macro.
     * @param name the name of the macro
     * @param parameters the name of the parameter
     * @param vararg which kind of variable argument this macro uses
     * @param tokens the token sequence of this macro
     */
    public GenericMacro( String name, String[] parameters, VarArg vararg, String tokens ){
        super( name, parameters, vararg, null );
        if( tokens != null )
            setTokenSequence( tokens );
    }
    
    /**
     * Sets the token sequence using a {@link PreprocessorLexer} to evaluate
     * <code>line</code>.
     * @param line the line to evaluate
     */
    public void setTokenSequence( String line ){
        try{
            PurgingReader reader = new PurgingReader( new StringReader( line ), null, null );
            PreprocessorLexer lexer = new PreprocessorLexer( null, null, reader );
            lexer.setNoAutoNewlineAtEnd( true );
            TokenSequence sequence = new TokenSequence();
            
            while( true ){
                Symbol next = lexer.next();
                PreprocessorToken token = (PreprocessorToken)next.value;
                if( token.getKind() == Symbols.EOF )
                    break;
                sequence.tokens().add( new Token( token ) );
            }
            
            setTokenSequence( sequence );
        }
        catch( IOException ex ){
            // that should not happen...
            throw new RuntimeException( ex );
        }
    }
}
