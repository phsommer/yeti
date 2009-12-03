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
package tinyos.yeti.views;

import tinyos.yeti.views.NodeContentProvider.Element;

/**
 * This comparator returns 0 for any pair of elements where one element
 * is not part of the root selection.
 * @author Benjamin Sigg
 *
 */
public class RootNodeComparator extends NodeLabelComparator{
	public RootNodeComparator( String property ){
		super( property );
	}
	
    @Override
    public int compare( Element a, Element b ){
        if( a.getParent() != null || b.getParent() != null )
            return 0;
        
        return super.compare( a, b );
    }
}
