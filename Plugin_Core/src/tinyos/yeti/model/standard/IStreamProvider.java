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
package tinyos.yeti.model.standard;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.IParseFile;

/**
 * Provides input and output streams for cache files.
 * @author Benjamin Sigg
 */
public interface IStreamProvider{
	/**
	 * Opens a stream to read the cache for <code>file</code>.
	 * @param file the file whose cache is read
	 * @param extension what kind of cache is to be read
	 * @return the stream or <code>null</code> if not available
	 * @throws CoreException in case of a file that can not be accessed
	 */
	public InputStream read( IParseFile file, String extension ) throws CoreException;
	
	/**
	 * Tells whether {@link #read(IParseFile, String)} will return a stream
	 * for the arguments <code>file</code> and <code>extension</code>
	 * @param file some file 
	 * @param extension the kind of cache to read
	 * @return whether data is available or not
	 */
	public boolean canRead( IParseFile file, String extension );
	
	/**
	 * Opens a stream to write the cache for <code>file</code>. Notice
	 * that the contents may not be stored persistent until the stream is closed.
	 * @param file the file whose cache is written
	 * @param extension what kind of cache is to be written
	 * @return the stream
	 * @throws CoreException in case of a file that can not be accessed
	 */
	public OutputStream write( IParseFile file, String extension ) throws CoreException;
	
	/**
	 * Deletes all data that is stored persistent about <code>file</code> and
	 * <code>extension</code>.
	 * @param file some file whose cache gets cleared
	 * @param extension the kind of data to clear
	 * @param monitor to report progress
	 */
    public void clear( IParseFile file, String extension, IProgressMonitor monitor );
}
