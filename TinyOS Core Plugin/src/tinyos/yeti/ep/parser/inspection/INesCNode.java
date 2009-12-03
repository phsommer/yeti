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
package tinyos.yeti.ep.parser.inspection;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;

/**
 * A {@link INesCNode} is a representation of a high level C oder NesC element.
 * {@link INesCNode}s are connected in a graph.<br>
 * There are a number of default sub-interfaces of {@link INesCNode} in this
 * package. Clients using the {@link INesCInspector} normally only have access
 * to them, so a parser should not introduce new {@link INesCNode}s. 
 * @author Benjamin Sigg
 */
public interface INesCNode{
	/**
	 * Under the assumption that this node represents an undocumented
	 * {@link IASTModelNode}, points to another node that may contain (related) 
	 * documentation about this node. These references must not build cycles.
	 */
	InspectionKey<INesCNode> DOCUMENTATION_REFERENCE = new InspectionKey<INesCNode>( INesCNode.class, "documentation reference" );
	
	/**
	 * Gets a small human readable description of what this node is and
	 * does.
	 * @return description of this node, may be <code>null</code>
	 */
	public String getNesCDescription();
	
	/**
	 * Gets the {@link IASTModelNode} which is wrapped by this node.
	 * @return the underlying node or <code>null</code> if there is no
	 * underlying node or if the feature is not supported
	 */
	public IASTModelNode asNode();
	
	/**
	 * Gets the {@link IASTModelNodeConnection} which is wrapped by this node.
	 * @return the underlying connection or <code>null</code> if there is no
	 * underlying connection or if the feature is not supported
	 */
	public IASTModelNodeConnection asConnection();
	
	/**
	 * Gets the number of {@link InspectionKey}s this node supports.
	 * @return the number of keys, maybe <code>0</code>.
	 */
	public int getReferenceKindCount();
	
	/**
	 * Gets the index'th key that is supported by this node.
	 * @param index the index of a key
	 * @return the key
	 */
	public InspectionKey<?> getReferenceKind( int index );
	
	/**
	 * Gets all the references that match kind <code>key</code>.
	 * @param key the key for this kind of reference 
	 * @param inspector the inspector asking the question
	 * @return the nodes or <code>null</code> if <code>key</code> is not
	 * among the supported keys of this node or if there is no available data.
	 * Note that the same node may be used for more than one key. The resulting
	 * array may be empty but not contain <code>null</code> values.
	 */
	public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector );
}
