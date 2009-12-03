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
package tinyos.yeti.model;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.IParseFile;

/**
 * A file cache can store some values on the disk and later read them
 * @author Benjamin Sigg
 *
 * @param <V> the kind of value the cache stores on the disk
 */
public interface IFileCache<V>{

    /**
     * Tells whether <code>file</code> has a valid cache associated.
     * @param file the file whose cache might get read
     * @return <code>true</code> if the cache can be read
     */
    public boolean canReadCache( IParseFile file );
    
    /**
     * Starts reading the cache of <code>file</code>.
     * @param file the file to read
     * @param monitor to inform about progress and to cancel the operation
     * @return the value
     * @throws IOException if the file can't be accessed
     * @throws CoreException if the file can't be accessed
     * @see #writeCache(IParseFile, Object, IProgressMonitor)
     */
    public V readCache( IParseFile file, IProgressMonitor monitor ) throws IOException, CoreException;
    
    /**
     * Writes <code>values</code> into a file that is associated with
     * <code>file</code>.
     * @param file the owner of the declarations
     * @param value the value to store, might be <code>null</code>
     * @param monitor to inform about progress and to cancel the operation
     * @throws IOException if the file can't be accessed
     * @throws CoreException if the file can't be accessed
     */
    public void writeCache( IParseFile file, V value, IProgressMonitor monitor ) throws IOException, CoreException;
    
    /**
     * Deletes the cache for <code>file</code>.
     * @param file the file for which the cache is to be deleted
     * @param monitor to inform about progress or to cancel the operation
     */
    public void clearCache( IParseFile file, IProgressMonitor monitor );
}
