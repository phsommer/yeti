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
package tinyos.yeti.ep.parser;


/**
 * An {@link IASTModelNodeConnection} is a set of properties that identify
 * a child or a reference of an {@link IASTModelNode}. A connection can also
 * point to a non existent node.
 * @author Benjamin Sigg
 */
public interface IASTModelNodeConnection extends IASTModelElement{
    /**
     * Gets the path to the owner of this connection.
     * @return the owner, never <code>null</code>
     */
    public IASTModelPath getPath();
        
    /**
     * Tells whether this points to a real child or just references
     * another node. A true child means that the parent of the {@link IASTModelNode}s
     * {@link IASTModelNode#getPath() path} method would return the path {@link #getPath()},
     * a reference means that the two results are not related.
     * @return <code>true</code> if this is a reference, <code>false</code>
     * otherwise
     */
    public boolean isReference();
    
    /**
     * Gets the path to which this reference points. This path can be <code>null</code>,
     * in that case the other informations of this connection should suffice to
     * find the node to which this points.
     * @return the reference to which this node points or <code>null</code>
     */
    public IASTModelPath getReferencedPath();
}
