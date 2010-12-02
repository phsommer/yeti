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

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;

public class MultiOutlineFilter implements IOutlineFilter{
	private List<IOutlineFilter> filters = new ArrayList<IOutlineFilter>();
	
	public void add( IOutlineFilter filter ){
		filters.add( filter );
	}
	
	public void setEditor( NesCEditor editor ){
		for( IOutlineFilter filter : filters ){
			filter.setEditor( editor );
		}
	}
	
	public boolean include( IASTModelNode node ){
		for( IOutlineFilter filter : filters ){
			if( !filter.include( node ))
				return false;
		}
		return true;
	}
	
	public boolean include( IASTModelNodeConnection connection ){
		for( IOutlineFilter filter : filters ){
			if( !filter.include( connection ))
				return false;
		}
		return true;
	}
}
