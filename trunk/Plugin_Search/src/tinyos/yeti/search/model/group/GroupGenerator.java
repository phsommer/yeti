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
package tinyos.yeti.search.model.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

import tinyos.yeti.ep.parser.IASTModelNode;

/**
 * Groups together a set of {@link IASTModelNode}s and invents an new
 * root node for them.
 * @author Benjamin Sigg
 */
public abstract class GroupGenerator<K, N>{
	private Group group;
	private Map<Key, Group> groups = new HashMap<Key, Group>();
	private TreeViewer viewer;
	
	public GroupGenerator(){
		clear();
	}
	
	public void setViewer( TreeViewer viewer ){
		this.viewer = viewer;
	}
	
	public Group getRoot(){
		return group;
	}
	
	public void clear(){
		group = createRoot();
		groups.clear();
	}
	
	public void insert( N node ){
		K[] keys = getKeys( node );
		Group group = null;
		Group parent = this.group;
		
		List<Group.Wrapper> path = new ArrayList<Group.Wrapper>();
		
		Key key = null;
		for( K groupKey : keys ){
			key = new Key( key, keyAspect( groupKey ));
			group = groups.get( key );
			if( group == null ){
				group = createGroupFor( groupKey );
				groups.put( key, group );
				parent.add( group );
				if( viewer != null && !viewer.getTree().isDisposed() ){
					if( path.isEmpty() ){
						viewer.add( this.group, this.group.getChild( group ) );
					}
					else{
						viewer.add( new TreePath( path.toArray() ), parent.getChild( group ) );
					}
				}
			}
			path.add( parent.getChild( group ));
			parent = group;
		}
		group.add( node );
		if( viewer != null && !viewer.getTree().isDisposed() ){
			viewer.add( new TreePath( path.toArray() ), group.getChild( node ) );
		}
	}
	
	public void remove( N node ){
		K[] groupKey = getKeys( node );
		
		Group[] groups = new Group[ groupKey.length+1 ];
		Key[] groupsKey = new Key[ groups.length ];
		
		groups[0] = this.group;
		
		Key key = null;
		for( int i = 0; i < groupKey.length; i++ ){
			key = new Key( key, keyAspect( groupKey[i] ));
			groups[i+1] = this.groups.get( key );
			groupsKey[i+1] = key;
			if( groups[i+1] == null )
				return;
		}
		
		TreePath path = null;
		for( int i = groups.length-1; i >= 0; i-- ){
			if( i+1 < groups.length ){
				path = group.getTreePath( groups, groups[i+1] );
				groups[i].remove( groups[i+1] );
				this.groups.remove( groupsKey[i+1] );
			}
			else{
				path = group.getTreePath( groups, node );
				groups[i].remove( node );
			}
			if( groups[i].hasChildren() )
				break;
		}
		
		fireRemoved( path );
	}
	
	protected void fireRemoved( TreePath path ){
		if( viewer != null ){
			viewer.remove( path );
		}
	}
	
	public Group.Wrapper getGroupNode( N node ){
		K[] groupKeys = getKeys( node );
		Key key = null;
		for( K groupKey : groupKeys ){
			key = new Key( key, keyAspect( groupKey ));
		}
		
		Group group = groups.get( key );
		if( group == null )
			return null;
		return group.getChild( node );
	}
	
	protected abstract Group createRoot();
	
	protected abstract K[] getKeys( N node );
	
	protected abstract Group createGroupFor( K groupKey );
	
	protected Object keyAspect( K value ){
		if( value instanceof IASTModelNode )
			return ((IASTModelNode)value).getLogicalPath();
		
		return value;
	}
	
	private static class Key{
		private Key parent;
		private Object value;
		
		public Key( Key parent, Object value ){
			this.parent = parent;
			this.value = value;
		}

		@Override
		public int hashCode(){
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((parent == null) ? 0 : parent.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals( Object obj ){
			if( this == obj )
				return true;
			if( obj == null )
				return false;
			if( getClass() != obj.getClass() )
				return false;
			Key other = (Key)obj;
			if( parent == null ){
				if( other.parent != null )
					return false;
			}
			else if( !parent.equals( other.parent ) )
				return false;
			if( value == null ){
				if( other.value != null )
					return false;
			}
			else if( !value.equals( other.value ) )
				return false;
			return true;
		}
	}
}
