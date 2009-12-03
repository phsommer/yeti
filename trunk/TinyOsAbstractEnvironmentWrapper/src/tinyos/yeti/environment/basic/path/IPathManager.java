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
import java.util.Map;

import tinyos.yeti.environment.basic.commands.ICommand;
import tinyos.yeti.environment.basic.progress.ICancellation;

/**
 * A path manager handles all the necessities that arise by working
 * with a file system. 
 * @author Benjamin Sigg
 */
public interface IPathManager extends IPathTranslator{
    /**
     * Gets all the files that might be included in the compilation given
     * <code>request</code>.
     * @param request a description of the files that might be included
     * @param cancellation to cancel this operation
     * @return the list of files or <code>null</code> in case of an error
     */
    public File[] getAllReachableFiles( IPathRequest request, ICancellation cancellation );

    /**
     * Searches for a file with name <code>fileName</code>.
     * @param fileName the name of the file, this name might include some
     * parts of a directory path.
     * @param request where to search the file, {@link IPathRequest#getFileExtensions()}
     * can be ignored
     * @param cancellation to cancel this operation
     * @return the file or <code>null</code> if not found or not valid
     */
    public File locate( String fileName, IPathRequest request, ICancellation cancellation );

    /**
     * Gets the files that are included in whatever the parser parses.
     * @param cancellation to cancel this operation
     * @return the additional files
     */
    public File[] getStandardInclusionFiles( ICancellation cancellation );
    
    /**
     * Gets the standard set of environment variables. Please read
     * {@link ICommand#getEnvironmentParameters()} to learn about the
     * format of these strings.
     * @return the set of variables
     */
    public Map<String,String> getEnvironmentVariables();
    
    /**
     * Gets the path to the makerules-file.
     * @return the makerules
     */
    public String getMakerulesPath();
    
    /**
     * Gets the path to the tos-directory in which source code is stored.
     * @return the path to the tos directory
     */
    public String getTosDirectoryPath();
    
    /**
     * Gets the path to the root of the tinyos installation.
     * @return the root
     */
    public String getTosRootPath();
    
    /**
     * Gets the directory in which platforms are supposed to be.
     * @return the platform directory
     */
    public String getPlatformDirectory();
}
