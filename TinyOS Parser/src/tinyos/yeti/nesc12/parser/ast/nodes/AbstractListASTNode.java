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
package tinyos.yeti.nesc12.parser.ast.nodes;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.Range;

/**
 * A {@link AbstractListASTNode} has a variable number of children {@link AbstractASTNode}s.
 * @author Benjamin Sigg
 */
public abstract class AbstractListASTNode<N extends ASTNode> extends RangedASTNode implements ListASTNode<N>, Range{
    private List<ASTNode> children = new ArrayList<ASTNode>();

    public AbstractListASTNode( String name ) {
        super( name );
    }

    /**
     * Checks whether the new child <code>child</code> is valid or not.
     * @param child the new child
     * @throws ASTException if the child is not acceptable
     */
    protected abstract void checkChild( N child ) throws ASTException;
    
    public ASTNode getChild( int index ) {
        return children.get( index );
    }

    public boolean isError( int index ){
        ASTNode node = getChild( index );
        return node instanceof ErrorASTNode;
    }
    
    public N getNoError( int index ) {
        return getTypedChild( index );
    }
    
    @SuppressWarnings("unchecked")
    public N getTypedChild( int index ){
        ASTNode node = getChild( index );
        if( node instanceof ErrorASTNode )
            return null;
        
        return (N)node;
    }
    
    public int getChildrenCount() {
        return children.size();
    }
    
    public ASTNode remove( int index ){
        if( index < 0 || index >= children.size() )
            throw new ArrayIndexOutOfBoundsException( index );
        
        ASTNode node = children.get( index );
        if( node != null )
            disconnectChild( node );
        
        children.remove( index );
        return node;
    }
    
    public AbstractListASTNode<N> insert( int index, N node ){
        if( index < 0 || index > children.size() )
            throw new ArrayIndexOutOfBoundsException( index );
        
        if( node != null ){
            if( !(node instanceof ErrorASTNode ))
                checkChild( node );
            connectChild( node );
        }
        
        children.add( index, node );
        return this;
    }

    public ListASTNode<N> insertError( int index, ErrorASTNode error ) {
        if( index < 0 || index > children.size() )
            throw new ArrayIndexOutOfBoundsException( index );
        
        if( error != null )
            connectChild( error );
        
        children.add( index, error );
        return this;
    }
    
    public AbstractListASTNode<N> add( N node ){
        return insert( children.size(), node );
    }
    
    public ListASTNode<N> addError( ErrorASTNode error ) {
        return insertError( children.size(), error );
    }
    
    public ASTNode set( int index, N node ){
        if( index == children.size() ){
            insert( index, node );
            return null;
        }
        
        if( index < 0 || index >= children.size() )
            throw new ArrayIndexOutOfBoundsException( index );
        
        if( node != null ){
            if( !(node instanceof ErrorASTNode ))
                checkChild( node );
            connectChild( node );
        }
        
        ASTNode oldNode = children.set( index, node );
        
        if( oldNode != null )
            disconnectChild( oldNode );
        
        return oldNode;
    }
    
    public ASTNode setError( int index, ErrorASTNode error ) {
        if( index == children.size() ){
            insertError( index, error );
            return null;
        }
        
        if( index < 0 || index >= children.size() )
            throw new ArrayIndexOutOfBoundsException( index );
        
        if( error != null ){
            connectChild( error );
        }
        
        ASTNode oldNode = children.set( index, error );
        
        if( oldNode != null )
            disconnectChild( oldNode );
        
        return oldNode;
    }
}
