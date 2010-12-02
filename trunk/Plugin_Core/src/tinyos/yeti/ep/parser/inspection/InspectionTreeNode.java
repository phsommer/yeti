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

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper around an {@link INesCNode} allowing to see the node as a tree.
 * @author Benjamin Sigg
 *
 */
public class InspectionTreeNode{
	private INesCInspector inspector;
	private INesCNode node;
	private InspectionKey<?> key;
	private InspectionTreeNode parent;
	private InspectionTreeNode[] children;
	
	/**
	 * Creates a new node.
	 * @param inspector the root
	 * @param key the key under which this node is known to the parent, may be <code>null</code>
	 * @param node the node itself
	 * @param parent the parent of this node, may be <code>null</code>
	 */
	public InspectionTreeNode( INesCInspector inspector, InspectionKey<?> key, INesCNode node, InspectionTreeNode parent ){
		this.inspector = inspector;
		this.key = key;
		this.node = node;
		this.parent = parent;
	}
	
	private void resolveChildren(){
		if( children == null ){
			List<InspectionTreeNode> nodes = new ArrayList<InspectionTreeNode>();
		
			if( node != null ){
				for( int i = 0, n = node.getReferenceKindCount(); i<n; i++ ){
					InspectionKey<?> key = node.getReferenceKind( i );
					INesCNode[] references = node.getReferences( key, inspector );
					if( references != null ){
						for( INesCNode reference : references ){
							nodes.add( new InspectionTreeNode( inspector, key, reference, this ) );
						}
					}
				}
			}
			
			children = nodes.toArray( new InspectionTreeNode[ nodes.size() ] );
		}
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		if( key != null ){
			builder.append( "(" );
			builder.append( key.getUsage() );
			builder.append( ") " );
		}
		if( node != null ){
			builder.append( node.getNesCDescription() );
		}
		return builder.toString();
	}
	
	public InspectionTreeNode[] getChildren(){
		resolveChildren();
		return children;
	}
	
	public InspectionTreeNode getParent(){
		return parent;
	}
	
	public boolean isLeaf(){
		resolveChildren();
		return children.length == 0;
	}
}
