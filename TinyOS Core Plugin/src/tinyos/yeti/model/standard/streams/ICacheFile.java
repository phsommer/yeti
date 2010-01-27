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

import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents a file in the cache. This is basically a wrapper around an
 * {@link IFile}, {@link ICacheFile}s are created by the {@link StreamProvider}
 * (or similar classes) and can keep track of their creation and deletion.
 * @author Benjamin Sigg
 *
 */
public interface ICacheFile{
	/**
	 * Locks the path of this file. Can be called more than once. A file is
	 * to be locked before modifying it and unlocked after the modification
	 * is done. Creating and deleting also counts as modification.
	 * @see #close()
	 */
	public void open();
	
	/**
	 * Unlocks the path of this file.
	 * @see #open()
	 */
	public void close();
	
	public void create( InputStream source, boolean force, IProgressMonitor monitor ) throws CoreException;
	
	public void delete( boolean force, IProgressMonitor monitor ) throws CoreException;
	
	public boolean exists();
	
	public InputStream getContents() throws CoreException;
	
	public IContainer getParent();
	
	public boolean isAccessible();
	
	public void setContents( InputStream source, int updateFlags, IProgressMonitor monitor ) throws CoreException;
	
	public void setDerived( boolean derived ) throws CoreException;
}
