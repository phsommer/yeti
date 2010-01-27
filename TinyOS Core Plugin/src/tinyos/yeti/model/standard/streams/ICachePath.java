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
package tinyos.yeti.model.standard.streams;

import org.eclipse.core.runtime.IPath;

/**
 * Represents the path to a file in the cache. Multiple {@link ICachePath}s
 * may represents the same {@link IPath}.
 * @author Benjamin Sigg
 */
public interface ICachePath{
	/**
	 * Returns the path which is represented by this {@link ICachePath}.
	 * @return the path
	 */
	public IPath getPath();
	
	/**
	 * Locks this path, the {@link IPathConverter} may not return an
	 * {@link ICachePath} pointing to the same path as this {@link ICachePath}
	 * if the input is different. This method may be called more than once.
	 */
	public void open();
	
	/**
	 * Unlocks this path.
	 */
	public void close();
}
