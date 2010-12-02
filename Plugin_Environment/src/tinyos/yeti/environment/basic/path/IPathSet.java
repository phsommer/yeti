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
package tinyos.yeti.environment.basic.path;

import java.io.File;

import tinyos.yeti.environment.basic.platform.IExtendedPlatform;

/**
 * Used by the {@link AbstractPathManager} to remember which files, boards
 * or other items were already handled. 
 * @author Benjamin Sigg
 */
public interface IPathSet extends IPathReplacer{
    /**
     * Searches the platform with name <code>name</code>.
     * @param name the name of the platform
     * @return the platform
     */
    public IExtendedPlatform getPlatform( String name );
    
    /**
     * Stores an additional file.
     * @param file the additional file to store, <code>null</code> will be ignored
     */
    public void store( File file );
    
    /**
     * Transforms a relative into a set of absolute paths pointing to the
     * source directories. Absolute paths are returned unchanged.
     * @param path some path
     * @return a set of absolute paths, may be of length 0
     */
    public String[] relativeToAbsolute( String path );
    
    /**
     * Gets the path manager to access more information
     * @return the path manager
     */
    public IPathManager getPathManager();
    
    /**
     * Checks whether the file with name <code>name</code> has a valid
     * file extension.
     * @param name the name to check
     * @return <code>true</code> if the file is valid
     */
    public boolean validFileExtension( String name );
    
    /**
     * Marks <code>directory</code> as handled. Other steps might ask
     * for handled directories in order to optimize their work.
     * @param directory the directory to mark processed
     * @return whether the directory is really new or not
     */
    public boolean setProcessed( String directory );
    
    /**
     * Tells whether <code>directory</code> is already processed.
     * @param directory the directory which is to check
     * @return <code>true</code> if {@link #setProcessed(String)} was already
     * called with <code>directory</code>
     */
    public boolean isProcessed( String directory );
    
    /**
     * Tells whether some directory is excluded from processing.
     * @param directory the directory to check
     * @return <code>true</code> if the directory must not be processed (its
     * children might be processed)
     */
    public boolean isExcluded( File directory );
}
