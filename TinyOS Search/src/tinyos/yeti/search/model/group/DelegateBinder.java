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

/**
 * Used to manage {@link GroupContentProvider}, {@link GroupLabelProvider}
 * and {@link GroupComparator} and their delegates. 
 * @author Benjamin Sigg
 */
public interface DelegateBinder{
	/**
	 * Given the input of a {@link Viewer} this method tells what input
	 * the delegates should use.
	 * @param root the input
	 * @return the input of the delegates
	 */
	public Object getDelegateInput( Group root );
	
	/**
	 * Given a element that might added to a {@link Group} this
	 * method tells what element the delegates should access. Note that
	 * <code>element</code> might already be in the correct representation.
	 * @param element some element
	 * @return the element as it should be seen by the delegates
	 */
	public Object groupChildToContentNode( Object element );
}
