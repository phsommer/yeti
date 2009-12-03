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
 * A node derived from abstract syntax tree. Each node represents a semantic
 * element, like an interface or a function. A node should have a name that
 * reflects the name its original element has, e.x. the node of an "interface x"
 * would have the name "x". Together with the {@link TagSet} that is directly
 * associated with this node, the name creates a unique identifier (assuming
 * the NesC application is not faulty. If the application is faulty, then
 * the uniqueness of the name does not have to be guaranteed).
 * @author Benjamin Sigg
 */
public interface IASTModelNode extends IASTModelElement{    
	/**
     * Gets the identifier of this node. Together with the {@link #getPath() paths}
     * parent and the {@link #getTags() tags} the identifier should build a unique
     * name.<br>
     * It is not always possible to have a unique identifier, but as more unique
     * as better.
     * @return the identifier
     */
    public String getIdentifier();
    
    /**
     * Gets the name of this element. The name is never seen by the user, many
     * nodes can share the same name. The name and the {@link #getLabel() label}
     * are closely related: the label is a short description of this element
     * that can include the name.<br>
     * If this element represents a declaration of some kind, then the name
     * of that declaration and this name should be the same. For example
     * if this element represents "int x = 4;", then the name should be "x".
     * @return the name, may be <code>null</code>
     */
	public String getNodeName();
       
    /**
     * Gets the path to this node. This path contains information about how
     * this node came into existence.
     * @return the path, never <code>null</code>. The path should not be
     * ambiguous
     */
    public IASTModelPath getPath();
    
    /**
     * Gets the logical path to this node. The logical path contains information
     * about the place of this node in the whole application. For example two
     * nodes with different {@link #getPath() paths} can have the same logical
     * path if they denote the same object. 
     * @return either the logical path or if not known the normal path, never <code>null</code>
     * @see #getPath()
     */
    public IASTModelPath getLogicalPath();
    
    /**
     * Gets the children and references of this node.
     * @return the children and references, or <code>null</code> if this 
     * kind of node is always a leaf
     */
    public IASTModelNodeConnection[] getChildren();
    
    /**
     * Removes any connection to children which pass <code>filter</code>.
     * @param filter the rule to which connections should remain
     */
    public void removeConnections( IASTModelConnectionFilter filter );
    
    /**
     * Gets the contents of this node as graphical representation. Many
     * nodes will return <code>null</code> here, only special ones like
     * interfaces, modules or configurations should have a graphical representation.
     * @return the contents, may be <code>null</code>
     */
    public IASTFigureContent getContent();
    
    /**
     * Gets documentation that is associated with this node.
     * @return documentation, may be <code>null</code>
     */
    public INesCDocComment getDocumentation();
}
