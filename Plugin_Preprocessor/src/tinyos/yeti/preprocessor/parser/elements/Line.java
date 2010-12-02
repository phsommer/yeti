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
package tinyos.yeti.preprocessor.parser.elements;

import static tinyos.yeti.preprocessor.parser.stream.SkipableElementStream.missingToken;
import static tinyos.yeti.preprocessor.parser.stream.SkipableElementStream.newline;
import static tinyos.yeti.preprocessor.parser.stream.SkipableElementStream.or;
import static tinyos.yeti.preprocessor.parser.stream.SkipableElementStream.whitespace;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.lexer.State;
import tinyos.yeti.preprocessor.lexer.Symbols;
import tinyos.yeti.preprocessor.output.Insights;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.stream.SkipableElementStream;

public class Line extends PreprocessorElement {
    private TokenSequence tokens;
    
    public Line( PreprocessorToken token, TokenSequence tokens ) {
        super( token );
        this.tokens = tokens;
    }

    public void apply( State states ){
        SkipableElementStream stream = new SkipableElementStream( tokens, true );
        stream.skipWhitespaces();
        if( !stream.hasNext() ){
            states.reportError( "Missing line in #line directive", Insights.directiveLine(), tokens );
            return;
        }
        
        PreprocessorElement element = stream.next();
        try{
            int line = Integer.parseInt( element.getToken().getText() );
            states.getBase().setLine( states.getBase().getLine() - 1 - element.getToken().getLine() + line );
        }
        catch( NumberFormatException ex ){
            states.reportError( "Directive #line requires an integer as first argument", Insights.directiveLine(), element );
            return;
        }
        
        stream.skipWhitespaces();
        if( stream.hasNext() ){
            StringBuilder builder = new StringBuilder();
            element = stream.next();
            if( element.getToken().getKind() == Symbols.QUOTE ){
                // in a string
                while( stream.hasNext() ){
                    element = stream.next();
                    PreprocessorToken token = element.getToken();
                    if( token != null ){
                        if( token.getKind() == Symbols.QUOTE ){
                            // string closed
                            states.getBase().setFile( states.getFileInfoFactory().createLine( states.getBase().getFile(), builder.toString() ));
                            break;
                        }
                        else{
                            if( token.getText() != null )
                                builder.append( token.getText() );
                        }
                    }
                }
            }
            else{
                // a lonely token
                states.reportError( "Not a file", Insights.directiveLine(), element );
            }
            
            stream.skip( or( missingToken(), or( newline(), whitespace() )));
            if( stream.hasNext() ){
                states.reportWarning( "To many arguments for directive #line", Insights.directiveLine(), tokens );
            }
        }
    }
    
    @Override
    public PreprocessorElement[] getChildren() {
        return new PreprocessorElement[]{ tokens };
    }

    @Override
    public int getChildrenCount() {
        return 1;
    }
    
    @Override
    public PreprocessorElement getChild( int index ) {
        return tokens;
    }
    
    @Override
    protected void toString( StringBuilder builder, int tabs ) {
        toString( builder, tabs, "line", null, tokens );
    }
}
