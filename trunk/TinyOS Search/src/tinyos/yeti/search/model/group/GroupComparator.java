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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class GroupComparator extends ViewerComparator{
	private DelegateBinder binder;
	private ViewerComparator delegate;
	
	public GroupComparator( DelegateBinder binder, ViewerComparator delegate ){
		this.binder = binder;
		this.delegate = delegate;
	}
	
	public void setBinder( DelegateBinder binder ){
		this.binder = binder;
	}
	
	@Override
	public int compare( Viewer viewer, Object e1, Object e2 ){
		if( e1 instanceof Group.Wrapper )
			e1 = ((Group.Wrapper)e1).getNode();
		if( e2 instanceof Group.Wrapper )
			e2 = ((Group.Wrapper)e2).getNode();
		
		boolean g1 = e1 instanceof Group;
		boolean g2 = e2 instanceof Group;
		
		if( g1 && g2 )
			return super.compare( viewer, e1, e2 );
		if( g1 )
			return -1;
		if( g2 )
			return 1;
		
		e1 = binder.groupChildToContentNode( e1 );
		e2 = binder.groupChildToContentNode( e2 );
		
		return delegate.compare( viewer, e1, e2 );
	}
}
