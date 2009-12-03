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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.Image;

/**
 * A group is a set of objects and other groups.
 * @author Benjamin Sigg
 */
public class Group implements Iterable<Group.Wrapper>{
	private String name;
	private Image image;
	private Object location;
	
	private List<Wrapper> children = new ArrayList<Wrapper>();
	
	public Group( String name, Image image, Object location ){
		this.name = name;
		this.image = image;
		this.location = location;
	}
	
	public String getName(){
		return name;
	}
	
	public Image getImage(){
		return image;
	}
	
	public Object getLocation(){
		return location;
	}
	
	public Iterator<Wrapper> iterator(){
		return children.iterator();
	}
	
	public void add( Object child ){
		children.add( new Wrapper( child ) );
	}
	
	public int remove( Object child ){
		for( int i = 0, n = children.size(); i<n; i++ ){
			if( children.get( i ).getNode() == child ){
				children.remove( i );
				return i;
			}
		}
		return -1;
	}
	
	public Group.Wrapper getChild( Object child ){
		for( Wrapper wrapper : children ){
			if( wrapper.getNode() == child ){
				return wrapper;
			}
		}
		return null;
	}
	
	/**
	 * Searches the path from this group to <code>child</code>.
	 * @param initialPath the initial path to search starting with <code>this</code>
	 * @param child some child of this group, might be an entry of <code>initialPath</code>.
	 * @return the path or <code>null</code>, this node is not part of the path
	 */
	public TreePath getTreePath( Group[] initialPath, Object child ){
		LinkedList<Object> path = findPath( child, initialPath, 1 );
		if( path == null )
			return null;
		return new TreePath( path.toArray() );
	}
	
	private LinkedList<Object> findPath( Object node, Group[] initialPath, int index ){
		for( Wrapper child : children ){
			LinkedList<Object> result = child.findPath( node, initialPath, index );
			if( result != null ){
				return result;
			}
		}
		return null;
	}
	
	public Object[] getChildren(){
		return children.toArray( new Object[ children.size() ] );
	}
	
	public boolean hasChildren(){
		return !children.isEmpty();
	}
	
	@Override
	public String toString(){
		return children.toString();
	}
	
	public class Wrapper{
		private Object node;
		
		public Wrapper( Object node ){
			this.node = node;
		}
		
		public Group getParent(){
			return Group.this;
		}
		
		public Object getNode(){
			return node;
		}
		
		@Override
		public String toString(){
			return String.valueOf( node );
		}
		
		private LinkedList<Object> findPath( Object node, Group[] initialPath, int index ){
			if( initialPath != null && index < initialPath.length ){
				if( this.node != initialPath[ index ])
					return null;
			}
			
			if( node == this.node ){
				LinkedList<Object> result = new LinkedList<Object>();
				result.add( this );
				return result;
			}
			if( this.node instanceof Group ){
				LinkedList<Object> result = ((Group)this.node).findPath( node, initialPath, index+1 );
				if( result != null ){
					result.addFirst( this );
				}
				return result;
			}
			return null;
		}
	}
}
