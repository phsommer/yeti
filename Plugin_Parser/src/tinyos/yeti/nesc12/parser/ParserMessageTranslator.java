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
package tinyos.yeti.nesc12.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ParserMessageHandler;
import tinyos.yeti.nesc12.parser.ast.ASTMessageHandler;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.preprocessor.MessageHandler;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.output.Insight;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.elements.TokenSequence;
import tinyos.yeti.preprocessor.parser.stream.ElementStream;

/**
 * Used by the {@link Parser} to collect messages from various places.
 * @author Benjamin Sigg
 */
public class ParserMessageTranslator implements MessageHandler, ASTMessageHandler{
    private Parser parser;
    private ParserMessageHandler handler;

    private List<PreprocessorMessage> preprocessorMessages = new ArrayList<PreprocessorMessage>();
    
    public ParserMessageTranslator( Parser parser, ParserMessageHandler handler ){
        this.parser = parser;
        this.handler = handler;
    }
    
    public ParserMessageHandler getHandler() {
        return handler;
    }

    public void handle( MessageHandler.Severity severity, String message, Insight insight, PreprocessorElement... elements ) {
        preprocessorMessages.add( new PreprocessorMessage( severity, message, insight, trim( elements ) ) );
    }
    
    private PreprocessorElement[] trim( PreprocessorElement[] elements ){
        PreprocessorElement[] result = new PreprocessorElement[ elements.length ];
        for( int i = 0, n = result.length; i<n; i++ ){
            result[i] = trim( elements[i] );
        }
        return result;
    }
    
    private PreprocessorElement trim( PreprocessorElement element ){
        ElementStream stream = new ElementStream( element, true );
        LinkedList<PreprocessorElement> list = new LinkedList<PreprocessorElement>();
        
        while( stream.hasNext() ){
            PreprocessorElement next = stream.next();
            if( next.getChildrenCount() == 0 ){
                list.add( next );
            }
        }
        
        // remove empty tokens
        loop:while( list.size() > 0 ){
            PreprocessorToken token = list.getFirst().getToken();
            if( token == null )
                break;
            
            switch( token.getKind() ){
                case EOF:
                case WHITESPACE:
                case NEWLINE:
                    list.removeFirst();
                    break;
                default:
                    break loop;
            }
        }
        
        loop:while( list.size() > 1 ){
            PreprocessorToken token = list.getLast().getToken();
            if( token == null )
                break;
            
            switch( token.getKind() ){
                case EOF:
                case WHITESPACE:
                case NEWLINE:
                    list.removeLast();
                    break;
                default:
                    break loop;
            }
        }
        
        TokenSequence sequence = new TokenSequence( list.toArray( new PreprocessorElement[ list.size() ] ));
        return sequence;
    }
    
    public void preprocessorDone(){
        for( PreprocessorMessage message : preprocessorMessages ){
            switch( message.severity ){
                case ERROR:
                    handler.error( message.message, true, message.insight, parser.resolveLocation( true, message.elements ) );
                    break;
                case WARNING:
                    handler.warning( message.message, true, message.insight, parser.resolveLocation( true, message.elements ) ); 
                    break;
                case MESSAGE:
                    handler.message( message.message, true, message.insight, parser.resolveLocation( true, message.elements ) );
                    break;
            }
        }
    }

    public void report( ASTMessageHandler.Severity severity, String message, Insight insight, ASTNode... nodes ) {
        switch( severity ){
            case ERROR: 
                handler.error( message, false, insight, parser.resolveLocation( true, nodes ) );
                break;
            case WARNING:
                handler.warning( message, false, insight, parser.resolveLocation( true, nodes ) ); 
                break;
            case MESSAGE:
                handler.message( message, false, insight, parser.resolveLocation( true, nodes ) );
                break;
        }            
    }

    public void report( ASTMessageHandler.Severity severity, String message, Insight insight, RangeDescription... ranges ) {
        switch( severity ){
            case ERROR: 
                handler.error( message, false, insight, ranges );
                break;
            case WARNING:
                handler.warning( message, false, insight, ranges ); 
                break;
            case MESSAGE:
                handler.message( message, false, insight, ranges );
                break;
        }                        
    }

    private static class PreprocessorMessage{
        public MessageHandler.Severity severity;
        public String message;
        public PreprocessorElement[] elements;
        public Insight insight;

        public PreprocessorMessage( MessageHandler.Severity severity, String message, Insight insight, PreprocessorElement[] elements ){
            this.severity = severity;
            this.message = message;
            this.insight = insight;
            this.elements = elements;
        }
    }
}
