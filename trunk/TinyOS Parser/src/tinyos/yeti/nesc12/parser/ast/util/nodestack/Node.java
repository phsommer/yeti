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
package tinyos.yeti.nesc12.parser.ast.util.nodestack;

import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

/**
 * Represents a single node on the {@link NodeStack}.
 * @author Benjamin Sigg
 */
public interface Node{
	/**
     * Gets the identifier for this node.
     * @return the identifier
     */
    public String getIdentifier();
    
    /**
     * Sets the true representation of this node. If already
     * set or if <code>node</code> is <code>null</code>, ignore the call.
     * @param node the representation
     */
    public void setNode( ModelNode node );

    /**
     * Gets the true representation of this node.
     * @return the true representation or <code>null</code> if not set
     * @see #setNode(ModelNode)
     */
    public ModelNode getNode();
    
    /**
     * Instead of a {@link ModelNode}, only a {@link ModelConnection} represents
     * the node. Some flags of the connection might be set, put most settings
     * will just be dropped.
     * @param connection the connection
     */
    public void setConnection( ModelConnection connection );
    
    /**
     * Gets the connection which represents this node.
     * @return the connection or <code>null</code> if not set
     */
    public ModelConnection getConnection();
    
    /**
     * Adds a new region to this node.
     * @param region where the node belongs
     */
    public void addFileRegion( FileRegion region );
    
    /**
     * Adds a child to this node.
     * @param node the child
     * @param ast the node which creates the new connection
     */
    public void addChild( ModelNode node, ASTNode ast );
    
    /**
     * Removes all children which have the given identifier <code>target</code>.
     * @param target the identifier of the connection to remove
     */
    public void removeChild( String target );
    
    /**
     * Adds a reference to this node.
     * @param node the referenced node
     * @param ast the node which creates the new connection
     */
    public void addReference( ModelNode node, ASTNode ast );

    /**
     * Adds a connection to this node.
     * @param connection a child or a reference
     */
    public void addConnection( ModelConnection connection );
    
    /**
     * Sets the range of the {@link ASTNode} which is the source of this node.
     * @param range the range
     */
    public void setRange( Range range );
    
    /**
     * Gets the range of the underlying {@link ASTNode}
     * @return the range, might be <code>null</code>
     */
    public Range getRange();
    
    /**
     * Called when this node gets popped from the stack, this is the last opportunity
     * to transfer data to the true representation (if present) of this node.
     */
    public void pop();
    
    /**
     * Activates the error flag indicating that there was an error reported
     * in this node.
     */
    public void putErrorFlag();
    
    /**
     * Activates the warning flag indicating that there was a warning reported
     * in this node.
     */
    public void putWarningFlag();

}
