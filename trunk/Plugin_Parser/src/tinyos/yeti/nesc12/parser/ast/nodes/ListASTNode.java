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

import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;

/**
 * A list {@link ASTModelNode} has a flexible number of children.
 * @author Benjamin Sigg
 * @param <N> the kind of nodes in this list
 */
public interface ListASTNode<N extends ASTNode> extends ASTNode{
    /**
     * Removes the index'th child.
     * @param index the index of the child to remove
     * @return the child that has been removed
     * @throws ASTException if removing the child is not allowed
     */
    public ASTNode remove( int index ) throws ASTException;
    
    /**
     * Inserts a new child at location <code>index</code>.
     * @param index the location of the child
     * @param node the new child, <code>null</code> is accepted, but will
     * always imply an invalid AST. An {@link ErrorASTNode} will always
     * be accepted.
     * @return <code>this</code>
     * @throws ASTException if <code>node</code> is not a valid child
     */
    public ListASTNode<N> insert( int index, N node ) throws ASTException;
    
    /**
     * Inserts an error into the list.
     * @param index the index of the error
     * @param error the new error
     * @return <code>this</code>
     */
    public ListASTNode<N> insertError( int index, ErrorASTNode error );
    
    /**
     * Adds a new child at the end of the list.
     * @param node the new child, <code>null</code> is accepted, but will
     * always imply an invalid AST. An {@link ErrorASTNode} will always
     * be accepted.
     * @return <code>this</code>
     * @throws ASTException if <code>node</code> is not a valid child
     */
    public ListASTNode<N> add( N node ) throws ASTException;
    
    /**
     * Adds an error at the end of this list.
     * @param error the new error
     * @return <code>this</code>
     */
    public ListASTNode<N> addError( ErrorASTNode error );
    
    /**
     * Sets a new child at a given location.
     * @param index the location of the child, the last position will have
     * the same effect as {@link #add(ASTNode)}.
     * @param node the new child, <code>null</code> is accepted, but will
     * always imply an invalid AST. An {@link ErrorASTNode} will always
     * be accepted.
     * @return the old value at <code>index</code>, might be <code>null</code>
     * @throws ASTException if <code>node</code> is not a valid child
     */
    public ASTNode set( int index, N node ) throws ASTException;
    
    /**
     * Sets an error at the given location.
     * @param index the location
     * @param error the new error
     * @return the old node at that location
     */
    public ASTNode setError( int index, ErrorASTNode error );
    
    /**
     * Gets the index'th {@link ASTModelNode} if it is not an error node.
     * @param index the location of the node
     * @return the node if it is not an error
     */
    public N getNoError( int index );
}
