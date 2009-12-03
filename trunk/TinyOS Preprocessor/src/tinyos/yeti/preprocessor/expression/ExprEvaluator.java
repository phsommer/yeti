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
package tinyos.yeti.preprocessor.expression;

import tinyos.yeti.preprocessor.lexer.State;
import tinyos.yeti.preprocessor.output.Insights;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.stream.TokenReader;

public class ExprEvaluator {
    /*
    public static void main( String[] args ){
        PreprocessorElement[] tokens = new PreprocessorElement[]{
                new Token( new PreprocessorToken( Symbols.WHITESPACE, " " )),
                new Token( new PreprocessorToken( Symbols.K_DEFINED, "defined" )),
                new Token( new PreprocessorToken( Symbols.WHITESPACE, " " )),
                new Identifier( new PreprocessorToken( Symbols.IDENTIFIER, "A" )),
                new Token( new PreprocessorToken( Symbols.WHITESPACE, " " )),
                new Token( new PreprocessorToken( Symbols.TEXT, "&&" )),
                new Token( new PreprocessorToken( Symbols.WHITESPACE, " " )),
                new Token( new PreprocessorToken( Symbols.TEXT, "55" )),
                new Token( new PreprocessorToken( Symbols.GREATER, ">" )),
                new Token( new PreprocessorToken( Symbols.TEXT, "=" )),
                new Token( new PreprocessorToken( Symbols.WHITESPACE, " " )),
                new Token( new PreprocessorToken( Symbols.TEXT, "199901L" )),
        };
        
        TokenSequence seq = new TokenSequence( tokens );
        
        State states = new State( null, null, new MessageHandler(){
            public void handle( Severity severity, String message, PreprocessorElement... elements ){
                System.out.println( severity + " " + message );
            }            
        });
        
        System.out.println( evaluate( states, seq ));
    }
    */
    
    /**
     * Reads <code>element</code> as expression, parses the expression and
     * returns the result of the evaluation of the expression.
     * @param states a set of properites
     * @param element the element to see as expression
     * @return the evaluated value or 0 if an exception occurred
     */
    public static long evaluate( State states, PreprocessorElement element ){
        if( element == null )
            throw new NullPointerException( "element must not be null" );
        
        try{
            TokenReader reader = new TokenReader( element );
            parser parser = new parser( new Lexer( reader ));
            ExprState exprState = new ExprState( states, element );
            parser.setStates( exprState );
            Lazy lazy = (Lazy)parser.parse().value;
            long result = lazy.value( exprState );
            if( exprState.isValid() )
                return result;
            
            states.reportError( "Error on evaluation, set expression to 0.", Insights.expressionEvaluationFailed(), element );
            return 0;
        }
        catch( Exception ex ){
            states.reportError( ex.getMessage(), Insights.expressionEvaluationFailed(), element );
            states.reportError( "Error on evaluation, set expression to 0.", Insights.expressionEvaluationFailed(), element );
            return 0;
        }
    }
}
