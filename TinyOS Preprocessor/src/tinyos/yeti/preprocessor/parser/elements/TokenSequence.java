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

import tinyos.yeti.preprocessor.parser.ElementVisitor;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

public class TokenSequence extends PreprocessorElement {
    protected List<PreprocessorElement> tokens = new ArrayList<PreprocessorElement>();
    
    public TokenSequence(){
        super( null );
    }
    
    public TokenSequence( PreprocessorElement token ){
        super( null );
        tokens.add( token );
    }
    
    public TokenSequence( PreprocessorElement... token ){
        super( null );
        for( PreprocessorElement e : token )
            tokens.add( e );
    }
    
    @Override
    public void visit( ElementVisitor visitor ) {
        visitor.visit( this );
        for( PreprocessorElement child : tokens )
            child.visit( visitor );
        visitor.endVisit( this );
    }
    
    @Override
    public void visitReverse( ElementVisitor visitor ) {
        visitor.visit( this );
        for( int i = tokens.size()-1; i>=0; i-- )
            tokens.get( i ).visit( visitor );
        visitor.endVisit( this );        
    }
    
    public List<PreprocessorElement> tokens(){
        return tokens;
    }
    
    @Override
    public PreprocessorElement[] getChildren() {
        int count = 0;
        for( PreprocessorElement p : tokens )
            if( p != null )
                count++;
        
        PreprocessorElement[] result = new PreprocessorElement[ count ];
        count = 0;
        for( PreprocessorElement p : tokens )
            if( p != null )
                result[ count++ ] = p;
        
        return result;
    }
    
    @Override
    public int getChildrenCount() {
        return tokens.size();
    }
    
    @Override
    public PreprocessorElement getChild( int index ) {
        return tokens.get( index );
    }
    
    public void copy( TokenSequence sequence ){
        tokens.addAll( sequence.tokens() );
    }
    
    @Override
    protected void toString( StringBuilder builder, int tabs ) {
        toString( builder, tabs, "token_sequence", null, tokens );
    }
}
