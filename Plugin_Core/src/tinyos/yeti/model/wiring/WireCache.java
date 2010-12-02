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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.model.IFileCache;
import tinyos.yeti.model.IProjectCache;
import tinyos.yeti.model.ProjectModel;

/**
 * The wire cache holds information for a subset of files: which declarations
 * are available for a file, which declarations where defined in the file and
 * from which files a file includes declarations. This cache cannot fill
 * its entry, the client has to fill them.
 * @author Benjamin Sigg
 */
public class WireCache {
    /** the maximal number of elements in this cache */
    private int cacheSize = ProjectModel.INITIAL_CACHE_SIZE_GLOBAL_DEFINITIONS;

    /** all entries known to this cache */
    private Map<IParseFile, Entry> entries = new HashMap<IParseFile, Entry>();

    /** all the entries that are currently loaded */
    private LinkedList<Entry> loaded = new LinkedList<Entry>();

    /** the model for storage */
    private IProjectCache cache;
    
    /**
     * Creates a new cache.
     * @param cache the model for storage
     */
    public WireCache( IProjectCache cache ){
        this.cache = cache;
    }
    
    /**
     * Gets the maximal number of elements that can be in the cache.
     * @return the number of elements
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Sets the maximal number of elements that can be in che cache.
     * @param cacheSize the number of elements
     */
    public void setCacheSize(int cacheSize) {
        if( cacheSize < 1 )
            throw new IllegalArgumentException( "Cache must include at least one element" );

        this.cacheSize = cacheSize;
    }

    /**
     * Clears the cache, nothing will remain in it.
     * @param monitor to progress and to cancel
     */
    public void clear( IProgressMonitor monitor ){
        monitor.beginTask( "Clear", entries.size() );
        
        for( Entry entry : entries.values() ){
            entry.clearForBuild( new SubProgressMonitor( monitor, 1 ) );
            loaded.remove( entry );
            if( monitor.isCanceled() ){
                return;
            }
        }
        
        entries.clear();
        
        monitor.done();
    }

    /**
     * Gets a cache entry for one file. If there is no entry for the file,
     * then an entry will be created.
     * @param file the file whose entry is searched
     * @return the entry, never <code>null</code>
     */
    public WireCacheEntry getEntry( IParseFile file ){
        Entry entry = entries.get( file );
        if( entry == null ){
            entry = new Entry( file );
            entries.put( file, entry );
        }
        if( entry.isDeclarationsLoaded() ){
            ensureOnTop( entry, true );
        }
        return entry;
    }

    private void ensureOnTop( Entry entry, boolean wasLoaded ){
        if( wasLoaded )
            loaded.remove( entry );

        loaded.addFirst( entry );

        while( loaded.size() > cacheSize ){
            loaded.removeLast().clearForUpdate( false, null );
        }
    }

    /**
     * Gets an array that contains all files that are currently loaded in this
     * cache.
     * @return the list of files
     */
    public IParseFile[] getCachedFiles(){
        IParseFile[] result = new IParseFile[ loaded.size() ];
        int index = 0;
        for( Entry entry : loaded ){
            result[ index++ ] = entry.getFile();
        }
        return result;
    }

    private class Entry implements WireCacheEntry{
        /** declarations defined in this file */
        private List<IDeclaration> declared;
        /** the set of files which are directly included into this cache */
        private Set<IParseFile> wiring;
        
        /** the file for which this cache stands */
        private IParseFile file;
        /** the files from which this cache depends */
        private Set<IParseFile> dependings;

        
        public Entry( IParseFile file ){
            this.file = file;
        }

        public IParseFile getFile() {
            return file;
        }

        public boolean isDeclarationsLoaded(){
            return declared != null;
        }

        public boolean isDependenciesLoaded() {
            return dependings != null;
        }

        public void clearForBuild( IProgressMonitor monitor ){
            clear( true, true, monitor );
        }
        
        public void clearForUpdate( boolean fileClear, IProgressMonitor monitor ) {
            clear( fileClear, false, monitor );
        }
        
        public void clear( boolean fileClear, boolean fullClear, IProgressMonitor monitor ) {
            if( monitor == null )
                monitor = new NullProgressMonitor();
            
            monitor.beginTask( "Clear cache", fullClear ? 7 : 6 );
            
            if( isDeclarationsLoaded() ){
                declared = null;
                loaded.remove( this );
            }
            
            wiring = null;
            
            if( fileClear ){
                dependings = null;
                
                IFileCache<?> cache = WireCache.this.cache.getDependencyCache();
                if( cache != null ){
                    cache.clearCache( file, new SubProgressMonitor( monitor, 1 ) );
                }
                
                cache = WireCache.this.cache.getInclusionCache();
                if( cache != null ){
                    cache.clearCache( file, new SubProgressMonitor( monitor, 1 ) );
                }
                
                cache = WireCache.this.cache.getInitCache();
                if( cache != null ){
                    cache.clearCache( file, new SubProgressMonitor( monitor, 1 ) );
                }
                
                cache = WireCache.this.cache.getWiringCache();
                if( cache != null ){
                    cache.clearCache( file, new SubProgressMonitor( monitor, 1 ) );
                }
                
                cache = WireCache.this.cache.getASTModelCache();
                if( cache != null ){
                    cache.clearCache( file, new SubProgressMonitor( monitor, 1 ) );
                }
                
                cache = WireCache.this.cache.getReferencesCache();
                if( cache != null ){
                	cache.clearCache( file, new SubProgressMonitor( monitor, 1 ) );
                }
                
                if( fullClear ){
                    cache = WireCache.this.cache.getMissingFileCache();
                    cache.clearCache( file, new SubProgressMonitor( monitor, 1 ) );
                }
            }
            
            monitor.done();
        }

        public List<IDeclaration> declared() {
            return declared;
        }

        public void declare( List<IDeclaration> declared, boolean store, IProgressMonitor monitor ){
            monitor.beginTask( "Store cache", 500 );
            
            boolean loaded = isDeclarationsLoaded();

            this.declared = declared;
            if( this.declared == null )
                this.declared = Collections.emptyList();
            
            if( store ){
                IFileCache<IDeclaration[]> cache = WireCache.this.cache.getInclusionCache();
                if( cache != null ){
                    try{
                        cache.writeCache( file, this.declared.toArray( new IDeclaration[ this.declared.size() ] ), new SubProgressMonitor( monitor, 500 ));
                    }
                    catch ( IOException e ){
                        // can't do anything... ignore
                        e.printStackTrace();
                    }
                    catch ( CoreException e ){
                        // can't do anything... ignore
                        TinyOSPlugin.warning( e.getStatus() );
                    }
                }
            }
            
            ensureOnTop( this, loaded );
            monitor.done();
        }

        public boolean hasDeclaredCache(){
            IFileCache<IDeclaration[]> cache = WireCache.this.cache.getInclusionCache();
            return cache != null && cache.canReadCache( file );
        }
        
        public List<IDeclaration> loadDeclared( IProgressMonitor monitor ) throws IOException, CoreException{
            boolean loaded = isDeclarationsLoaded();
            
            IFileCache<IDeclaration[]> cache = WireCache.this.cache.getInclusionCache();
            
            IDeclaration[] result = cache.readCache( file, monitor );
            if( result == null ){
            	declared = null;
            }
            else{
            	declared = Arrays.asList( result );
            }
            
            ensureOnTop( this, loaded );
            
            return declared;
        }
        
        public Set<IParseFile> depends() {
            return dependings;
        }

        public boolean depends( IParseFile file ){
            if( !file.isProjectFile() )
                return false;

            if( dependings == null )
                return false;

            return dependings.contains( file );
        }

        public void depends( Set<IParseFile> files, boolean store, IProgressMonitor monitor ) {
            dependings = files;
            if( store ){
                try{
                    IFileCache<Set<IParseFile>> cache = WireCache.this.cache.getDependencyCache();
                    if( cache != null ){
                        cache.writeCache( file, files, monitor );
                    }
                }
                catch ( IOException e ){
                    TinyOSPlugin.warning( e );
                }
                catch ( CoreException e ){
                    TinyOSPlugin.warning( e.getStatus() );
                }
            }
        }
        
        public boolean hasDependsCache(){
            IFileCache<Set<IParseFile>> cache = WireCache.this.cache.getDependencyCache();
            return cache != null && cache.canReadCache( file );
        }
        
        public Set<IParseFile> loadDepends( IProgressMonitor monitor ) throws IOException, CoreException{
            IFileCache<Set<IParseFile>> cache = WireCache.this.cache.getDependencyCache();
            dependings = cache.readCache( file, monitor );
            return dependings;
        }
        
        public Set<IParseFile> wiring(){
            return wiring;
        }
        
        public void wiring( Set<IParseFile> files, boolean store, IProgressMonitor monitor ){
            if( files == null )
                wiring = Collections.emptySet();
            else
                wiring = files;
         
            if( store ){
                IFileCache<Set<IParseFile>> cache = WireCache.this.cache.getWiringCache();
                if( cache != null ){
                    try{
                        cache.writeCache( file, wiring, monitor );
                    }
                    catch ( IOException e ){
                        TinyOSPlugin.warning( e );
                    }
                    catch ( CoreException e ){
                        TinyOSPlugin.warning( e.getStatus() );
                    }
                }
            }
        }
        
        public boolean hasWiringCache(){
            IFileCache<Set<IParseFile>> cache = WireCache.this.cache.getWiringCache();
            return cache != null && cache.canReadCache( file );
        }
        
        public Set<IParseFile> loadWiring( IProgressMonitor monitor ) throws IOException, CoreException{
            IFileCache<Set<IParseFile>> cache = WireCache.this.cache.getWiringCache();
            wiring = cache.readCache( file, monitor );
            return wiring;
        }
        
        public boolean isWiringLoaded(){
            return wiring != null;
        }
    }
}
