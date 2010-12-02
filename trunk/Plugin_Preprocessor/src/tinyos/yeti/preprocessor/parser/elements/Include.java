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

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.preprocessor.IncludeCallback;
import tinyos.yeti.preprocessor.IncludeFile;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.lexer.State;
import tinyos.yeti.preprocessor.lexer.Symbols;
import tinyos.yeti.preprocessor.lexer.streams.IncludeStream;
import tinyos.yeti.preprocessor.output.Insights;
import tinyos.yeti.preprocessor.parser.ElementVisitor;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

public class Include extends PreprocessorElement {
    public static enum Kind{
        USER_HEADER, SYSTEM_HEADER, ILLEGGAL_HEADER
    }
    
    private TokenSequence tokens;
    
    public Include( PreprocessorToken token, TokenSequence tokens ){
        super( token );
        this.tokens = tokens;
    }
    
    public Kind kind(){
        return resolve().kind;
    }
    
    public String filename(){
        return resolve().file;
    }
    
    /**
     * Opens a lexer for the included file.
     * @param states the set of properties of this preprocessor
     * @return the new lexer or <code>null</code> if the file could not be found.
     */
    public IncludeStream stream( State states ){
        Resolve resolve = resolve();
        IncludeFile file = null;
        
        switch( resolve.kind ){
            case SYSTEM_HEADER:
                file = states.getIncludeProvider().searchSystemFile( resolve.file, states.requestMonitor() );
                break;
            case USER_HEADER:
                file = states.getIncludeProvider().searchUserFile( resolve.file, states.requestMonitor() );
                break;
            case ILLEGGAL_HEADER:
                states.reportError( "Directive #include requires an argument in the form \"filename\" or <filename>", Insights.directiveIncludeInvalidArgument(), tokens );
                return null;
        }

        if( file != null ){
        	IncludeCallback callback = states.getIncludeCallback();
            if( callback != null ){
            	callback.included( this, file );
            }
            
            return new IncludeStream( states, resolve.filename, file );
        }
        
        states.reportError( "Could not find file for inclusion: " + resolve.file, Insights.directiveIncludeMissingFile( resolve.file, resolve.kind == Kind.SYSTEM_HEADER ), tokens );
        return null;
    }
    
    @Override
    public PreprocessorElement[] getChildren() {
        if( tokens == null )
            return null;
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
    
    public TokenSequence getTokens(){
        return tokens;
    }
    
    @Override
    protected void toString( StringBuilder builder, int tabs ) {
        toString( builder, tabs, "include", null, tokens );
    }
    
    private Resolve resolve(){
        Resolve resolve = new Resolve();
        resolve.kind = Kind.ILLEGGAL_HEADER;
        
        if( tokens == null )
            return resolve;
        
        List<PreprocessorElement> tokens = this.tokens.tokens();
        
        int begin = -1;
        boolean beginQuote = false;
        int end = -1;
        boolean endQuote = false;
        
        for( int i = 0, n = tokens.size(); i<n; i++ ){
            PreprocessorToken token = tokens.get( i ).getToken();
            if( token.getKind() == Symbols.QUOTE ){
                beginQuote = true;
                begin = i;
                break;
            }
            if( token.getKind() == Symbols.SMALLER ){
                beginQuote = false;
                begin = i;
                break;
            }
        }
        
        for( int i = tokens.size()-1; i>=0; i-- ){
            PreprocessorToken token = tokens.get( i ).getToken();
            if( token.getKind() == Symbols.QUOTE ){
                endQuote = true;
                end = i;
                break;
            }
            if( token.getKind() == Symbols.GREATER ){
                endQuote = false;
                end = i;
                break;
            }
        }
        
        if( begin != -1 && end != -1 && begin != end && (beginQuote == endQuote) ){
            if( beginQuote )
                resolve.kind = Kind.USER_HEADER;
            else
                resolve.kind = Kind.SYSTEM_HEADER;
            
            final StringBuilder builder = new StringBuilder();
            final List<PreprocessorElement> filename = new ArrayList<PreprocessorElement>();
            
            ElementVisitor visitor = new ElementVisitor(){
                public void visit( PreprocessorElement element ) {
                    PreprocessorToken token = element.getToken();
                    if( token != null && token.getText() != null ){
                        builder.append( token.getText() );
                        filename.add( element );
                    }
                }
                public void endVisit( PreprocessorElement element ) {
                    // ignore
                }
            };
            
            for( int i = begin+1; i<end; i++ ){
                tokens.get( i ).visit( visitor );
            }
            
            resolve.file = builder.toString();
            resolve.filename = new TokenSequence( filename.toArray( new PreprocessorElement[ filename.size() ] ));
        }
        
        return resolve;
    }
    
    private static class Resolve{
        public String file;
        public Kind kind;
        public PreprocessorElement filename;
    }
}
