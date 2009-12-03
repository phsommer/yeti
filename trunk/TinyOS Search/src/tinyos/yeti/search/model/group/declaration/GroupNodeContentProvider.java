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
package tinyos.yeti.search.model.group.declaration;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

import tinyos.yeti.ep.parser.IASTModelElement;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.search.model.ISearchTreeContentProvider;
import tinyos.yeti.search.model.group.GroupContentProvider;
import tinyos.yeti.search.model.group.GroupGenerator;
import tinyos.yeti.views.NodeContentProvider;

/**
 * A {@link NodeContentProvider} which is aware that he is used as delegate
 * by a {@link GroupContentProvider}. Ensures that removal of nodes is correctly
 * translated.
 * @author Benjamin Sigg
 */
public class GroupNodeContentProvider extends SearchNodeContentProvider implements ISearchTreeContentProvider{
	private GroupGenerator<?, ? super IASTModelNode> group;
	
	public GroupNodeContentProvider( TagSet tags, GroupGenerator<?, ? super IASTModelElement> group ){
		super( tags );
		this.group = group;
	}
	
	public GroupNodeContentProvider( IASTModelNodeFilter rootFilter, GroupGenerator<?, ? super IASTModelNode> group ){
		super( rootFilter );
		this.group = group;
	}

	@Override
	protected void addRoot( TreeViewer viewer, Element element ){
		group.insert( element.getNode() );
	}
	
	@Override
	protected Object[] getRootPath( Element root ){
		TreePath path = group.getRoot().getTreePath( null, root.getNode() );
		if( path == null ){
			group.insert( root.getNode() );
			path = group.getRoot().getTreePath( null, root.getNode() );
		}
		
		int count = path.getSegmentCount();
		Object[] result = new Object[ count-1 ];
		for( int i = 0; i < result.length; i++ ){
			result[i] = path.getSegment( i );
		}
		return result;
	}
	
	@Override
	protected Object getPathElement( Element element ){
		if( element.getDepth() > 0 )
			return element;
		
		Object result = group.getGroupNode( element.getNode() );
		if( result != null )
			return result;
		
		return element;
	}
	
	@Override
	protected void fireRemoved( TreeViewer viewer, TreePath path, Element removed ){
		// forward call, roots are part of some group and hence the groups need an update
		group.remove( removed.getNode() );
	}
}
