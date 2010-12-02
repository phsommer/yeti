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
package tinyos.yeti.model.wiring;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.model.IFileModel;


/**
 * Represents the declarations that are accessible for a single file.
 * @author Benjamin Sigg
 */
public interface WireCacheEntry {
    /**
     * Gets the file for which this entry stands.
     * @return the file
     */
    public IParseFile getFile();

    /**
     * Gets the declarations that are defined in this file. The list returned
     * by this method must not be modified.
     * @return the declarations declared in this file or <code>null</code> if
     * {@link #isDeclarationsLoaded()} returns <code>false</code>
     */
    public List<IDeclaration> declared();

    /**
     * Gets the set of files from which this one depends. This set includes
     * all files (from the project) whose modification would trigger a rebuild
     * of {@link #getFile() this caches owner file}. This set is recursive, if
     * a file in this set depends on some other file, than that other file is
     * in this set as well.
     * @return the set of files, may be <code>null</code> if 
     * {@link #isDependenciesLoaded()} returns <code>false</code>
     */
    public Set<IParseFile> depends();

    /**
     * Tells whether {@link #loadDepends(IProgressMonitor)} is possible.
     * @return <code>true</code> if a dependency backup cache is available
     */
    public boolean hasDependsCache();

    /**
     * Loads the dependencies from the file cache.
     * @param monitor to interact with the operation
     * @return the dependencies
     * @throws IOException if the cache is not accessible
     * @throws CoreException if the cache is not accessible
     */
    public Set<IParseFile> loadDepends( IProgressMonitor monitor ) throws IOException, CoreException;

    /**
     * Tells this cache entry from which files it depends.
     * @param files the set of files
     * @param store if set, then this entry should store the new dependencies
     * using the current {@link IFileModel}.
     * @param monitor only necessary if <code>store</code> is <code>true</code>, 
     * used to inform the user about progress or the cancel the operation
     */
    public void depends( Set<IParseFile> files, boolean store, IProgressMonitor monitor );

    /**
     * Tells whether this file depends on <code>file</code>.
     * @param file the file which may be included into the path of this file
     * @return <code>true</code> if this depends on <code>file</code>, if
     * either this or <code>file</code> is not a project file, then <code>false</code>
     * is returned
     */
    public boolean depends( IParseFile file );

    /**
     * Sets the content of this entry and thus changes this entry to
     * loaded (see {@link #isDeclarationsLoaded()})
     * @param declared the declarations that are declared in this file, may be <code>null</code>
     * @param store if set, then this entry should store the declarations in
     * the current {@link IFileModel}.
     * @param monitor used to cancel the operation and to inform about the progress
     */
    public void declare( List<IDeclaration> declared, boolean store, IProgressMonitor monitor );

    /**
     * Tells whether this cache has a backup file for declared declarations.
     * @return <code>true</code> if {@link #loadDeclared(IProgressMonitor)} 
     * will work
     */
    public boolean hasDeclaredCache();

    /**
     * Loads the declared elements of this cache. This method will set the included elements to be an empty list.
     * @param monitor to interact with the operation 
     * @return the loaded declarations
     * @throws IOException if the backup file is not accessible
     * @throws CoreException if the backup file is not accessible
     */
    public List<IDeclaration> loadDeclared( IProgressMonitor monitor ) throws IOException, CoreException;

    /**
     * Gets the wiring of this cache. The wiring depends on {@link #isWiringLoaded()}.
     * This set includes all the files which would be accessed when generating
     * a list of declarations available in this file. It is however not recursive,
     * so a file in this set can wire to another file which is not part of this set.
     * @return the files that are directly included into this cache
     */
    public Set<IParseFile> wiring();

    /**
     * Sets the files that are directly included into this cache.
     * @param files the files
     * @param store <code>true</code> if the files should be stored in the
     * backup cache
     * @param monitor only needed if <code>store</code> is <code>true</code>
     */
    public void wiring( Set<IParseFile> files, boolean store, IProgressMonitor monitor );

    /**
     * Tells whether there is a backup cache for the wiring
     * @return <code>true</code> if a backup cache is available
     */
    public boolean hasWiringCache();

    /**
     * Loads the wiring from the backup cache.
     * @param monitor to interact with this operation
     * @return the wiring
     * @throws IOException if the backup file is not accessible
     * @throws CoreException if the backup file is not accessible
     */
    public Set<IParseFile> loadWiring( IProgressMonitor monitor ) throws IOException, CoreException;

    /**
     * Whether the wiring is loaded right now.
     * @return <code>true</code> if the wiring is loaded
     */
    public boolean isWiringLoaded();

    /**
     * Tells whether this cache actually has some content or is just a handle
     * to be filled.
     * @return <code>true</code> if this cache has content, <code>false</code>
     * if not.
     */
    public boolean isDeclarationsLoaded();

    /**
     * Tells whether the dependencies of this cache is known.
     * @return <code>true</code> if the wiring is known
     */
    public boolean isDependenciesLoaded();

    /**
     * Clears this cache for making small update on the caches.
     * @param resourceChanged <code>true</code> if this cache should be cleared
     * because the underlying resource was changed, <code>false</code>
     * if the cache should be cleared to free memory.
     * @param monitor to inform about progress or to cancel
     */
    public void clearForUpdate( boolean resourceChanged, IProgressMonitor monitor );
    
    /**
     * Clears this cache such that it can be used for a new build.
     * @param monitor to inform about progress or to cancel
     */
    public void clearForBuild( IProgressMonitor monitor );
}
