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

import org.eclipse.core.resources.IProject;

import tinyos.yeti.ep.IParseFile;

/**
 * The ast model is a set of {@link IASTModelNode}s. An {@link IASTModel} can
 * represent a whole application or just an incomplete part like a single file.
 * An {@link IASTModel} is not an abstract syntax tree, but high level view
 * of an AST. Since an {@link IASTModel} can contain 1000s of files, it should
 * not store too much for a single file.
 * @see TagSet
 * @see IASTModelNode
 * @see IASTModelNodeConnection
 * @author Benjamin Sigg
 */
public interface IASTModel extends Iterable<IASTModelNode>{
	/**
	 * Gets the project whose content this model describes.
	 * @return the project
	 */
	public IProject getProject();
	
    /**
     * Gets all the nodes which are child of <code>parent</code>,
     * whose {@link IASTModelNode#getIdentifier() name} equals 
     * <code>name</code> and whose {@link IASTModelNode#getTags() tags} are
     * a superset of <code>tags</code>.
     * @param parent the element whose children are searched, can be <code>null</code>
     * to indicate that all nodes should be searched
     * @param identifier the name of the elements to find, can be <code>null</code> to
     * indicate that the name should be ignored
     * @param tags the tags to search for, can be <code>null</code> to indicate
     * that the tags should be ignored
     * @return the list of nodes
     * @see ASTNodeFilterFactory#filter(IASTModelPath, String, TagSet)
     */
    public IASTModelNode[] getNodes( IASTModelPath parent, String identifier, TagSet tags );
    
    /**
     * Gets all the nodes that match the filter.
     * @param filter the filter that includes nodes
     * @return the list of nodes that passed <code>filter</code>
     */
    public IASTModelNode[] getNodes( IASTModelNodeFilter filter );
    
    /**
     * Gets the only one node which is a child of <code>parent</code>,
     * whose name equals <code>name</code> and {@link IASTModelNode#getTags() tags} 
     * are a superset of <code>tags</code>. If there is more than one possible
     * solution, the {@link IParseFile} should be compared and the one node
     * whose {@link IParseFile#getIndex()} is deepest should be returned.
     * @param parent the element whose children are searched, can be <code>null</code>
     * to indicate that all nodes should be searched
     * @param identifier the name of the elements to find, can be <code>null</code> to
     * indicate that the name should be ignored
     * @param tags the tags to search for, can be <code>null</code> to indicate
     * that the tags should be ignored
     * @return the node or <code>null</code> if no node was found
     * @see ASTNodeFilterFactory#filter(IASTModelPath, String, TagSet)
     */
    public IASTModelNode getNode( IASTModelPath parent, String identifier, TagSet tags );
    
    /**
     * Gets the one node that matches the filter. If there is more than one possible
     * solution, the {@link IParseFile} should be compared and the one node
     * whose {@link IParseFile#getIndex()} is deepest should be returned.
     * @param filter the filter that checks the nodes
     * @return the node or <code>null</code> if no node was found
     */
    public IASTModelNode getNode( IASTModelNodeFilter filter );
    
    /**
     * Searches the one node to which the given connection points. If there is more 
     * than one possible solution, the {@link IParseFile} should be compared and the
     * one node whose {@link IParseFile#getIndex()} is deepest should be returned.
     * @param connection the connection that points to a node
     * @return the node or <code>null</code> if no node was found
     */
    public IASTModelNode getNode( IASTModelNodeConnection connection );

    /**
     * Gets the one node that is described by <code>path</code>.
     * @param path the path to a node
     * @return the node whose {@link IASTModelNode#getPath()} would return
     * <code>path</code>
     * @see ASTNodeFilterFactory#path(IASTModelPath)
     */
    public IASTModelNode getNode( IASTModelPath path );
    
    /**
     * Called before an operation after which
     * {@link #remove(IASTModelNodeFilter, IASTModelConnectionFilter)},
     * {@link #removeNodes(IASTModelNodeFilter)} or
     * {@link #removeNodes(IParseFile)} is called. All nodes currently present do
     * not need to be checked by the removing methods later.<br>
     * The model can assume that any node now present would not get deleted
     * anyway.
     */
    public void markForLaterRemoving();
    
    /**
     * Removes any {@link IASTModelNode} and {@link IASTModelNodeConnection} that
     * is included either in <code>nodes</code> or in <code>connections</code>.
     * If the {@link #markForLaterRemoving()} flag was set, it is used and then
     * unset.
     * @param nodes a filter for nodes which are to be removed
     * @param connections a filter for connections which are to be removed
     */
    public void remove( IASTModelNodeFilter nodes, IASTModelConnectionFilter connections );
    
    /**
     * Deletes all {@link IASTModelNode}s whose
     * property {@link IASTModelNode#getParseFile() parse file} is equal
     * to <code>parseFile</code>. 
     * @param parseFile the name of the file
     */
    public void removeNodes( IParseFile parseFile );
    
    /**
     * Deletes all {@link IASTModelNode}s which are included by the <code>filter</code>. 
     * @param filter tells which nodes to delete
     */
    public void removeNodes( IASTModelNodeFilter filter );
    
    /**
     * Adds an additional set of nodes, the nodes were created by the same
     * parser as this model.
     * @param nodes the additional nodes
     */
    public void addNodes( IASTModelNode[] nodes );
    
    /**
     * Returns the current number of nodes stored in this model.
     * @return the number of nodes
     */
    public int getSize();
    
    /**
     * Clears this project and sets it back into the state that it was
     * shortly after its creation. No {@link IASTModelNode}s must remain
     * in this model.
     */
    public void clear();
}
