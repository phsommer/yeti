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
package tinyos.yeti.preprocessor.output;

import java.util.Collection;
import java.util.ListIterator;

import tinyos.yeti.preprocessor.lexer.InclusionPath;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * A list containing {@link PreprocessorToken}s. Can be used to
 * search tokens in respect to some location or range.
 * @author Benjamin Sigg
 */
public class TokenList {
    private PreprocessorToken[] tree;
    private int[] offsets;
    
    /**
     * Creates a new tree. The tokens in the collection <code>tokens</code>
     * must be ordered by their location.
     * @param tokens the list of tokens
     */
    public TokenList( Collection<? extends PreprocessorToken> tokens ){
        tree = tokens.toArray( new PreprocessorToken[ tokens.size() ] );
        offsets = new int[ tree.length+1 ];
        
        int offset = 0;
        for( int i = 0, n = tree.length; i<n; i++ ){
            offsets[i] = offset;
            offset += tree[i].getText().length();
        }
        
        offsets[ tree.length ] = offset;
    }
    
    public ListIterator<PreprocessorToken> listIterator( final int index ){
        return new ListIterator<PreprocessorToken>(){
            private int location = index;
            
            public void add( PreprocessorToken e ) {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return location < tree.length;
            }

            public boolean hasPrevious() {
                return location >= 0;
            }

            public PreprocessorToken next() {
                return tree[ location++ ];
            }

            public int nextIndex() {
                return location;
            }

            public PreprocessorToken previous() {
                return tree[ --location ];
            }

            public int previousIndex() {
                return location-1;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void set( PreprocessorToken e ) {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    /**
     * Binary search for the index of the token that includes <code>location</code>.
     * @param location some location in the text
     * @return the index of the token
     */
    public int getIndexOf( int location ){
        int left = 0;
        int right = tree.length-1;
        
        int middle = (left + right) / 2;
        while( middle != left ){
            if( offsets[middle] > location ){
                right = middle;
            }
            else{
                left = middle;
            }
            middle = (left+right)/2;
        }
        
        if( offsets[right] <= location )
            return right;
        
        return left;
    }
    
    /**
     * Gets the token at location <code>index</code>.
     * @param index the index of the token
     * @return the token itself
     */
    public PreprocessorToken getToken( int index ){
        return tree[ index ];
    }
    
    public int getOffset( int index ){
        return offsets[ index ];
    }
    
    public int getLength( int index ){
        return offsets[ index+1 ] - offsets[ index ];
    }
    
    public int size(){
        return tree.length;
    }
    
    public int indexOf( PreprocessorToken token ){
        for( int i = 0, n = tree.length; i<n; i++ )
            if( tree[i] == token )
                return i;
        
        return -1;
    }
    
    /**
     * Gets a token that is left (or equal) of 
     * <code>index</code> and in the basic input file. The left token will
     * be as near as possible.
     * @param index the location of some token
     * @return the token or <code>null</code>
     */
    public PreprocessorToken getNearestLeft( int index ){
        PreprocessorToken token = getToken( index );
        InclusionPath path = token.getPath();
        if( path == null ){
            return token;
        }
        
        path = path.getRoot();
        PreprocessorElement element = path.getElement();
        return lastToken( element );
    }
    
    private PreprocessorToken lastToken( PreprocessorElement element ){
        if( element == null )
            return null;
        
        for( int i = element.getChildrenCount()-1; i >= 0; i-- ){
            PreprocessorToken token = lastToken( element.getChild( i ) );
            if( token != null )
                return token;
        }
        
        return element.getToken();
    }
    
    /**
     * Gets the a token that is right (or equal) of 
     * <code>index</code> and in the basic input file. The right token will
     * be as near as possible.
     * @param index the location of some token
     * @return the token or <code>null</code>
     */
    public PreprocessorToken getNearestRight( int index ){
        PreprocessorToken token = getToken( index );
        InclusionPath path = token.getPath();
        if( path == null ){
            return token;
        }
        
        path = path.getRoot();
        PreprocessorElement element = path.getElement();
        return firstToken( element );
    }
    
    private PreprocessorToken firstToken( PreprocessorElement element ){
        if( element == null )
            return null;
        
        for( int i = 0, n = element.getChildrenCount(); i < n; i++ ){
            PreprocessorToken token = firstToken( element.getChild( i ) );
            if( token != null )
                return token;
        }
        
        return element.getToken();
    }
}
