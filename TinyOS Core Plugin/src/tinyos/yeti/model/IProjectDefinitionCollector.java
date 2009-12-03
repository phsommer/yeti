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

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.nesc.IMultiReader;

/**
 * Responsible to collect declarations that are included to files directly
 * or indirectly.
 * @author Benjamin Sigg
 */
public interface IProjectDefinitionCollector {
    /**
     * Gets the cache size of this collector. If this collector does not
     * use a cache then the behavior is not specified.
     * @return the size of the cache
     */
    public int getCacheSize();
    
    /**
     * Sets the size of the cache of this collector. If this collector does
     * not use a cache, then nothing happens.
     * @param size the size of the cache
     * @throws IllegalArgumentException if the size is not valid
     */
    public void setCacheSize( int size );
    
    /**
     * Gets the list of cached files of this collector. If this collector
     * does not use a cache then an empty array should be returned
     * @return the list of cached files
     */
    public IParseFile[] getCachedFiles();
    
    /**
     * Updates the internal wiring of the file <code>file</code>. The method
     * {@link #includes(IParseFile, IParseFile)} might be affected by an invocation of
     * {@link #updateInclusions(IParseFile, IMultiReader, IProgressMonitor)}.
     * @param file some file that was changed
     * @param reader the contents of the file or <code>null</code>
     * @param monitor used to inform the user about the state or to cancel this operation
     */
    public void updateInclusions( IParseFile file, IMultiReader reader, IProgressMonitor monitor );
    
    /**
     * Updates the internal wiring for <code>file</code>.
     * @param file some file that gets updated
     * @param parseFiles the new dependencies
     */
    public void updateInclusions( IParseFile file, Set<IParseFile> parseFiles );
    
    /**
     * Gets a list of the basic declarations which are present anywhere.
     * @param monitor to report progress
     * @return the list of declarations 
     */
    public IDeclaration[] getBasicDeclarations( IProgressMonitor monitor );
    
    /**
     * Gets a list of the basic macros which are present anywhere.
     * @param monitor to report progress
     * @return the list of macros
     */
    public IMacro[] getBasicMacros( IProgressMonitor monitor );
    
    /**
     * Gets the list of files which define the 
     * {@link #getBasicDeclarations(IProgressMonitor) basic declarations}
     * and the {@link #getBasicMacros(IProgressMonitor) basic macros}.
     * @param monitor to report progress
     * @return the list of files
     */
    public IParseFile[] getBasicFiles( IProgressMonitor monitor );
    
    /**
     * Lists all declarations that are available in <code>file</code>.
     * @param file the file to check
     * @param reader the contents of <code>file</code> or <code>null</code>
     * @param onlyIncluded if set, then only declarations that are included from
     * another file are to be reported, if not set any available declaration
     * is to be reported
     * @param monitor used to inform the user about the state or to cancel this operation
     * @return the declarations
     */
    public List<IDeclaration> collect( File file, IMultiReader reader, boolean onlyIncluded, IProgressMonitor monitor );

    /**
     * Tells whether the file <code>parseFile</code> includes the file
     * <code>includedParseFile</code> directly or indirectly. Returns
     * <code>false</code> if either of the two files is not a file from
     * the project itself.
     * @param parseFile some file
     * @param includedParseFile some included file
     * @return <code>true</code> if the two files are related
     */
    public boolean includes( IParseFile parseFile, IParseFile includedParseFile );

    /**
     * Completely erases the cache of this collector
     * @param monitor to inform about progress or to cancel
     */
    public void deleteCache( IProgressMonitor monitor );
    
    /**
     * Called when a file was changed to delete its cache.
     * @param file the file to delete
     * @param monitor to inform about progress or to cancel
     */
    public void deleteCache( IParseFile file, IProgressMonitor monitor );
}
