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
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.preprocessor.comment.NesCDocComment;

/**
 * An {@link ASTModelNode} is a single element in the abstract syntax tree.
 * @author Benjamin Sigg
 */
public interface ASTNode {
    /**
     * Tells whether <code>node</code> is an ancestor of <code>this</code>.
     * @param node some other node
     * @return <code>true</code> if <code>node</code> is an ancestor of this,
     * <code>false</code> otherwise. <code>this</code> is not an ancestor
     * of <code>this</code>
     */
    public boolean isAncestor( ASTNode node );
    
    /**
     * Calls one of the visit and one of the endVisit methods of <code>visitor</code>,
     * forwards <code>visitor</code> to its children but only if the visit
     * method returned <code>true</code>.
     * @param visitor the visitor, not <code>null</code>
     */
    public void accept( ASTVisitor visitor );
    
    /**
     * Simulates the steps a compiler would make in order to collect various
     * informations. This node should forward the call to its children in any order
     * that fits to the node. This node can push new information onto the stack, that
     * information will only be visible for this node and its children. The stack
     * should have the same height on entering and leaving this method.
     * @param stack a set of hierarchically data that can be modified by this
     * node
     */
    public void resolve( AnalyzeStack stack );
    
    /**
     * Gets the number of children of this node.
     * @return the number of children
     */
    public int getChildrenCount();
    
    /**
     * Gets the index'th child.
     * @param index the index of a child
     * @return the child
     */
    public ASTNode getChild( int index );
    
    /**
     * Gets an informal name of this node, the name is only useful for debugging
     * purposes.
     * @return the informal name
     */
    public String getASTNodeName();
    
    /**
     * Gets the range which is occupied by this node in the file that was parsed.
     * @return the range, might be <code>null</code> in case of new nodes. It must
     * never be <code>null</code> or invalid for an {@link ASTNode} which was
     * created by the parser and not modified by the client.
     */
    public Range getRange();
    
    /**
     * Gets the parent of this node.
     * @return the parent
     */
    public ASTNode getParent();
    
    /**
     * Sets the parent of this node. <br>
     * <b>Note</b>: this method should only be called by a true parent 
     * of <code>this</code> node.
     * @param parent the new parent
     */
    public void setParent( ASTNode parent );
    
    /**
     * Tells whether this node spans across an area that was included.
     * @return <code>true</code> if the whole area of this node was included
     */
    public boolean isIncluded();
    
    /**
     * Sets whether this node spans across an area that was included.
     * @param included <code>true</code> if the whole area of this node was included
     */
    public void setIncluded( boolean included );
    
    /**
     * If this node represents an element that could be documented, for example
     * a function, then this method returns the range of its declaration. The
     * range is used to find out which comment comes before this element.
     * @return the range of the declaration or <code>null</code>
     */
    public Range getCommentAnchor();
    
    /**
     * Sets the comments associated with this node.
     * @param comments the comments, may be <code>null</code>
     */
    public void setComments( NesCDocComment[] comments );
    
    /**
     * Gets the comments that are associated with this node.
     * @return the comments
     */
    public NesCDocComment[] getComments();
}
