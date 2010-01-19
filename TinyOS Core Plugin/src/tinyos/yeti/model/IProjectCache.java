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
package tinyos.yeti.model;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.model.missing.IMissingResource;
import tinyos.yeti.model.wiring.WireCache;

/**
 * Contains {@link IFileCache}s that are used to store information
 * about {@link IParseFile}s. 
 * @author Benjamin Sigg
 */
public interface IProjectCache{
	/**
	 * Connects this cache with <code>model</code>
	 * @param model the owner of this cache, not <code>null</code> and
	 * will never be changed again
	 */
	public void initialize( ProjectModel model );
	
	/**
	 * Gets a unique identifier for this type of cache. Normally this
	 * is something like the class name.
	 * @return the unique identifier
	 */
	public String getTypeIdentifier();
	
    /**
     * Clears any cache of this model. If <code>full</code>, then all
     * files have to be abandoned. If <code>false</code> only the files 
     * that are from the project have to be removed.
     * @param full whether to delete all files, or only those from the project
     * @param monitor to report progress
     */
    public void clear( boolean full, IProgressMonitor monitor );
    
    /**
     * Gets the cache which is used to store init-declarations. Init declarations
     * are accessible in all files and do not need an explicit include.
     * @return the cache or <code>null</code>
     */
    public IFileCache<IDeclaration[]> getInitCache();

    /**
     * Gets a cache which tells for each file which declarations are available
     * (included and declared in the file).
     * @return the cache or <code>null</code>
     */
    public IFileCache<IDeclaration[]> getInclusionCache();
    
    /**
     * Gets the cache which stores the dependencies of the project files. These
     * dependencies include <i>all</i> dependencies, independent from how many
     * edges, indirect links or non-project files have to be visited in order
     * to get to the dependency.
     * @return the cache with the dependencies or <code>null</code>
     */
    public IFileCache<Set<IParseFile>> getDependencyCache();
    
    /**
     * Gets the cache which stores the wirings of all files. The wiring tells
     * for files which other files are directly included. It does not follow
     * indirect paths as {@link #getDependencyCache() the dependency cache} would.
     * @return the cache for the wiring or <code>null</code>
     * @see WireCache
     */
    public IFileCache<Set<IParseFile>> getWiringCache();
    
    /**
     * Gets the cache which is used to store whole models.
     * @return the cache or <code>null</code>
     */
    public IASTModelFileCache getASTModelCache();
    
    /**
     * Gets a cache that lists for each built file which other files are missing.
     * @return the cache with the missing other files, may not be <code>null</code>
     */
    public IFileCache<IMissingResource[]> getMissingFileCache();
    
    /**
     * Gets a cache for storing references that point from a file to other
     * files.
     * @return the reference cache
     */
    public IFileCache<IASTReference[]> getReferencesCache();
}
