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
package tinyos.yeti.environment.basic.path.steps;

import java.io.File;

import tinyos.yeti.environment.basic.path.IPathSet;
import tinyos.yeti.environment.basic.progress.ICancellation;

public abstract class AbstractSearchStep implements ISearchStep{ 

    /**
     * Searches in <code>directory</code> for files that should be included
     * to the find-all-files-search.
     * @param paths the storage
     * @param directory the file which supposedly is a directory
     * @param recursive whether to search recursively or not
     * @param cancellation to cancel this operation
     */
    protected void collect( IPathSet paths, File directory, boolean recursive, ICancellation cancellation ){
        if( directory != null && directory.isDirectory() && directory.exists() ){
            if( paths.setProcessed( directory.getAbsolutePath() )){
                boolean exclude = paths.isExcluded( directory );

                if( !exclude || recursive ){
                    File[] children = directory.listFiles();
                    if( children != null ){
                        for( File child : children ){
                            if( child.isFile() ){
                                if( !exclude && paths.validFileExtension( child.getPath() ) ){
                                    paths.store( child );
                                }
                            }
                            else if( recursive ){
                                collect( paths, child, true, cancellation );
                            }
                            if( cancellation.isCanceled() )
                                return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Recursively searches for the file named <code>filename</code>.
     * @param fileName the name of a file
     * @param paths the storage
     * @param rootDirectory a file which might be a directory
     * @param cancellation to cancel this operation
     * @return <code>true</code> if the file was found, <code>false</code>
     * otherwise
     */
    protected boolean locateRecursive( String fileName, IPathSet paths, File rootDirectory, ICancellation cancellation ){
        if( locate( fileName, paths, rootDirectory, cancellation ) )
            return true;

        if( rootDirectory != null ){
            File[] files = rootDirectory.listFiles();
            if( files != null ){
                for( File file : files ){
                    if( file.isDirectory() ){
                        if( locateRecursive( fileName, paths, file, cancellation ) )
                            return true;
                    }

                    if( cancellation.isCanceled() )
                        return false;
                }
            }
        }

        return false;
    }

    /**
     * Searches in <code>directory</code> for <code>fileName</code>.
     * @param fileName the name of some file
     * @param paths the storage
     * @param directory a file which might be a directory
     * @param cancellation to cancel this operation
     * @return <code>true</code> if the file was found, <code>false</code>
     * otherwise
     */
    protected boolean locate( String fileName, IPathSet paths, File directory, ICancellation cancellation ){
        if( directory == null || !directory.isDirectory() || !directory.exists() )
            return false;

        String path = directory.getAbsolutePath();

        if( paths.setProcessed( path )){
            if( !paths.isExcluded( directory )){
                File check = new File( path + "/" + fileName );
                if( check.exists() ){
                    paths.store( check );
                    return true;
                }
            }
        }

        return false;
    }
}
