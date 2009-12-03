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
package tinyos.yeti.search.model;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.search.ui.SearchResultEvent;

public interface ISearchTreeContentProvider extends ITreeContentProvider{
	/**
	 * Asynchronously updates this provider according to the content of the
	 * event.
	 * @param event some event
	 * @return <code>true</code> if the event could be handled, <code>false</code>
	 * if this method did not know how to react on <code>event</code>
	 */
	public boolean handle( SearchResultEvent event );
}
