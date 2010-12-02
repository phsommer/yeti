/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2010 ETH Zurich
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
package tinyos.yeti.editors.outline;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.views.NodeContentProvider;

public class OutlineViewFilter extends ViewerFilter{
	private IOutlineFilter filter;

	public OutlineViewFilter( IOutlineFilter filter ){
		if( filter == null ){
			throw new IllegalArgumentException( "filter must not be null" );
		}
		this.filter = filter;
	}
	
	@Override
	public boolean select( Viewer viewer, Object parentElement, Object item ){
		if( item instanceof NodeContentProvider.Element ){
			NodeContentProvider.Element element = (NodeContentProvider.Element)item;
			IASTModelNode node = element.getNode();
			if( node != null ){
				return filter.include( node );
			}
			IASTModelNodeConnection connection = element.getUnresolvedConnection();
			if( connection != null ){
				return filter.include( connection );
			}
			return true;
		}
		return true;
	}

}
