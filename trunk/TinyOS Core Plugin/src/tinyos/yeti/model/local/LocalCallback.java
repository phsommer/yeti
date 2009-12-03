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
package tinyos.yeti.model.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.Debug;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.INesCDefinitionCollector;
import tinyos.yeti.ep.parser.INesCDefinitionCollectorCallback;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.model.wiring.CallbackStack;
import tinyos.yeti.model.wiring.WireCacheEntry;
import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.nesc.IMultiReader;

/**
 * Local callbacks only store the declarations that were declared directly
 * in the parsed file. 
 * @author Benjamin Sigg
 *
 */
public class LocalCallback  implements INesCDefinitionCollectorCallback{
    /** base cache used to request other callbacks */
    private CallbackStack<LocalCallback> stack;

    /** the file for which this callback is used */
    private IParseFile file;

    /** if set, then only the wiring is interesting */
    private boolean wiring;

    /** if set, then only included declarations are interesting */
    private boolean onlyIncluded;

    /** if set, then this callback is currently loading a file */
    private boolean loading;

    /** generic flag used for marking this callback */
    private boolean marked = false;

    /** declarations that were found in this file */
    private List<IDeclaration> declared;

    /** The files that were included by this callback. */
    private Set<IParseFile> dependencies;

    /** The files that were directly included by this callback */
    private Set<IParseFile> wiringSet;
    
    /** referenced callbacks */
    private Set<LocalCallback> references;
    
    /** the collector used to collect declarations and includes for this file */
    private INesCDefinitionCollector collector;

    /**
     * Creates a new callback. 
     * @param stack the stack this callback is operating on
     * @param file the file for which this callback stands
     * @param onlyIncluded if set, then only declarations that are included
     * are to be reported
     * @param wiring if set, then only the wiring is interesting and no
     * declarations should be collected
     */
    public LocalCallback( CallbackStack<LocalCallback> stack, IParseFile file, boolean onlyIncluded, boolean wiring ){
        this.stack = stack;
        this.file = file;
        this.wiring = wiring;
        this.onlyIncluded = onlyIncluded;
    }

    /**
     * Marks this and all referenced callbacks.
     */
    private void mark(){
        if( !marked ){
            marked = true;
            if( references != null ){
                for( LocalCallback next : references ){
                    next.mark();
                }
            }
        }
    }

    /**
     * Gets the file for which this callback stands.
     * @return the file
     */
    public IParseFile getFile() {
        return file;
    }

    /**
     * Tells whether this callback is currently loading.
     * @return <code>true</code> if currently parsing a file
     */
    public boolean isLoading(){
        return loading;
    }

    /**
     * Gets the list of declarations that were declared in this callback.
     * @return the declared declarations
     */
    public List<IDeclaration> getDeclared() {
        return declared;
    }

    public List<IDeclaration> collectIncluded(){
        List<IDeclaration> list = new ArrayList<IDeclaration>();
        collectIncluded( list, false );
        return list;
    }

    private void collectIncluded( List<IDeclaration> list, boolean store ){
        if( !marked ){
            marked = true;

            if( store && declared != null )
                list.addAll( declared );

            if( references != null ){
                for( LocalCallback callback : references ){
                    callback.collectIncluded( list, true );
                }
            }
            
            marked = false;
        }
    }
    
    /**
     * Collects all the files that were accessed for building up this
     * {@link LocalCallback}.
     * @return all the accessed files
     */
    public Set<IParseFile> collectAccessedFiles(){
    	Set<IParseFile> files = new HashSet<IParseFile>();
    	collectAccessedFiles( files );
    	return files;
    }
    
    private void collectAccessedFiles( Set<IParseFile> files ){
    	if( !marked ){
    		marked = true;
    		
    		if( file != null )
    			files.add( file );
    		
    		if( references != null ){
    			for( LocalCallback callback : references ){
    				callback.collectAccessedFiles( files );
    			}
    		}
    		
    		marked = false;
    	}
    }

    /**
     * Tells whether this callback is loading or depends on a loading callback.
     * @return <code>true</code> if loading
     */
    public boolean loadingDependency(){
        mark();
        return dependsOnLoading();
    }

    /**
     * Tells whether this callback is loading or depends on a loading callback.
     * @return <code>true</code> if loading
     */
    private boolean dependsOnLoading(){
        boolean loading = this.loading;
        this.loading = false;
        boolean result = dependsOnOtherLoading();
        this.loading = loading;
        return result;
    }
    
    private boolean dependsOnOtherLoading(){
        if( !marked ){
            if( loading )
                return true;
            
            marked = true;

            if( references != null ){
                for( LocalCallback next : references ){
                    if( next.dependsOnLoading() ){
                        marked = false;
                        return true;
                    }
                }
            }

            marked = false;
        }
        return false;
    }

    /**
     * Loads this callback. If <code>cache</code> is loaded then the callback
     * might update its contents without parsing the file. If the cache is
     * not loaded then the callback might write into the cache to load it.
     * @param reader reader for this callbacks file, might be <code>null</code>
     * @param cache the cache responsible for this file
     * @param monitor used to inform the user about the progress and to 
     * cancel this operation
     */
    public void load( IMultiReader reader, WireCacheEntry cache, IProgressMonitor monitor ){
        if( monitor.isCanceled() ){
            return;
        }
        
        loading = true;
        try{
            if( wiring ){
                loadWiring( reader, cache, monitor );
            }
            else{
                loadDeclarations( reader, cache, monitor );
            }
        }
        catch( IOException ex ){
            ex.printStackTrace();
        }
        finally{
            collector = null;
            loading = false;
            monitor.done();
        }
    }

    private void loadWiring( IMultiReader reader, WireCacheEntry cache, IProgressMonitor monitor ) throws IOException{
        monitor.beginTask( "Load wiring", 1600 );
        
        dependencies = null;
        wiringSet = null;
        
        boolean dependenciesUncached = false;
        boolean wiringUncached = false;
        
        tryUncacheWiring( cache, monitor ); // 500 clicks
        if( wiringSet != null ){
            wiringUncached = true;
            if( file.isProjectFile() ){
                tryUncacheDependencies( cache, monitor ); // 500 clicks
                if( dependencies != null ){
                    dependenciesUncached = true;
                }
            }
        }

        if( (dependencies == null && file.isProjectFile()) || wiringSet == null ){
            // was not on the disk or in the cache, file must be parsed
            dependencies = null;
            wiringSet = null;
            
            parseFile( reader, true, monitor ); // 400 clicks

            if( !dependenciesUncached && file.isProjectFile() )
                cache.depends( dependencies, true, new SubProgressMonitor( monitor, 100 ) ); // 100 clicks
            
            if( !wiringUncached )
                cache.wiring( wiringSet, true, new SubProgressMonitor( monitor, 100 ) ); // 100 clicks
        }
        
        monitor.done();
    }
    
    private void loadDeclarations( IMultiReader reader, WireCacheEntry cache, IProgressMonitor monitor ) throws IOException{
        int clicks = 2100;
        monitor.beginTask( "Load declarations", clicks );
        
        
        declared = null;
        dependencies = null;
        wiringSet = null;
        
        boolean declaredUncached = false;
        boolean dependenciesUncached = false;
        boolean wiringUncached = false;
        
        tryUncacheDeclarations( cache, monitor ); // 500 clicks
        clicks -= 500;
        if( declared == null ){
            parseFile( reader, onlyIncluded, monitor ); // 400 clicks
            clicks -= 400;
        }
        else{
            for( IDeclaration declaration : declared ){
                stack.declared( declaration );
            }
            
            declaredUncached = true;
            
            tryUncacheWiring( cache, monitor );
            if( wiringSet == null ){
                parseFile( reader, true, monitor ); // 400 clicks
                clicks -= 400;
            }
            else{
                wiringUncached = true;
                
                if( file.isProjectFile() ){
                    tryUncacheDependencies( cache, monitor ); // 500 clicks
                    clicks -= 500;
                }
                
                if( dependencies == null && file.isProjectFile() ){
                    parseFile( reader, true, monitor ); // 400 clicks
                    clicks -= 400;
                }
                else{
                    // 300 clicks needed later
                    
                    dependenciesUncached = true;
                    SubProgressMonitor wiringSetMonitor = new SubProgressMonitor( monitor, clicks-300 );
                    wiringSetMonitor.beginTask( "Wiring", wiringSet.size() );
                    
                    for( IParseFile file : wiringSet ){
                        stack.included( file );
                        LocalCallback callback = stack.load( file, wiring, new SubProgressMonitor( wiringSetMonitor, 1 ) );
                        if( callback != null ){
                            reference( callback );
                        }
                    }
                    
                    wiringSetMonitor.done();
                }
            }
        }
        

        if( !declaredUncached && !onlyIncluded ){
        	cache.declare( declared, true, new SubProgressMonitor( monitor, 100 ) ); // 100 clicks
        }
        
        if( !dependenciesUncached && file.isProjectFile() )
            cache.depends( dependencies, true, new SubProgressMonitor( monitor, 100 ) ); // 100 clicks

        if( !wiringUncached )
            cache.wiring( wiringSet, true, new SubProgressMonitor( monitor, 100 ) ); // 100 clicks
        
        monitor.done();
    }
    
    private void parseFile( IMultiReader reader, boolean reportIncludesOnly, IProgressMonitor monitor ) throws IOException{
        if( Debug.DEBUG ){
            Debug.info( "miss: '" + file.getPath() + "'" );
        }
        
        if( reader == null ){
            File readable = file.toFile();
            if( readable != null && readable.canRead() ){
                reader = new FileMultiReader( readable );
            }
        }

        if( reader != null ){
            collector = stack.createCollector( file );
            if( collector != null ){
                collector.setReportIncludesOnly( reportIncludesOnly );
                collector.parse( reader, this, new SubProgressMonitor( monitor, 400 ) );
            }
        }        
    }

    private void tryUncacheDependencies( WireCacheEntry cache, IProgressMonitor monitor ){
        if( cache.isDependenciesLoaded() ){
            // just read the cache
            dependencies = cache.depends();
            if( Debug.DEBUG ){
                Debug.info( "hit (dependency): '" + file.getPath() + "'" );
            }
        }
        else if( cache.hasDependsCache() ){
            try {
                dependencies = cache.loadDepends( new SubProgressMonitor( monitor, 500 ) );
                if( Debug.DEBUG ){
                    Debug.info( "backup (dependency): '" + file.getPath() + "'" );
                }
            } 
            catch( IOException e ){
                e.printStackTrace();
            }
            catch( CoreException e ){
                TinyOSPlugin.warning( e.getStatus() );
            }
        }
    }
    

    private void tryUncacheWiring( WireCacheEntry cache, IProgressMonitor monitor ){
        if( cache.isWiringLoaded() ){
            // just read the cache
            wiringSet = cache.wiring();
            if( Debug.DEBUG ){
                Debug.info( "hit (wiring): '" + file.getPath() + "'" );
            }
        }
        else if( cache.hasWiringCache() ){
            try {
                wiringSet = cache.loadWiring( new SubProgressMonitor( monitor, 500 ) );
                if( Debug.DEBUG ){
                    Debug.info( "backup (wiring): '" + file.getPath() + "'" );
                }
            } 
            catch( IOException e ){
                e.printStackTrace();
            }
            catch( CoreException e ){
                TinyOSPlugin.warning( e.getStatus() );
            }
        }
    }

    private void tryUncacheDeclarations( WireCacheEntry cache, IProgressMonitor monitor ){
        if( cache.isDeclarationsLoaded() ){
            // just read the cache
            declared = cache.declared();
            if( Debug.DEBUG ){
                Debug.info( "hit (declarations): '" + file.getPath() + "'" );
            }
        }
        else if( cache.hasDeclaredCache() ){
            // try read from the disk
            try {
                declared = cache.loadDeclared( new SubProgressMonitor( monitor, 500 ) );
                if( Debug.DEBUG ){
                    Debug.info( "backup (declarations): '" + file.getPath() + "'" );
                }
            } 
            catch( IOException e ){
                TinyOSPlugin.warning( e );
            }
            catch( CoreException e ){
                TinyOSPlugin.warning( e.getStatus() );
            }
        }
    }

    public void finish( IProgressMonitor monitor ){
        monitor.beginTask( "Finish", 1000 );
/*
        if( !dependsOnLoading ){
            if( !wiring && !cache.isDeclarationsLoaded() && !onlyIncluded ){
                cache.load( null, getDeclared(),
                        new SubProgressMonitor( monitor, 500 ));
            }
            if( !cache.isDependenciesLoaded() ){
                cache.depends( (includedFiles == null) ? Collections.<IParseFile>emptySet() : includedFiles, true,
                        new SubProgressMonitor( monitor, 500 ) );
            }
        }
*/
        monitor.done();
    }

    public void declarationFound(IDeclaration declaration) {
        if( !wiring && declaration != null ){
            if( declared == null )
                declared = new ArrayList<IDeclaration>();

            declared.add( declaration );
            if( collector != null )
                collector.addDeclaration( declaration );
            stack.declared( declaration );
        }
    }

    public void elementIncluded( String name, IProgressMonitor monitor, Kind... kind ) {
        LocalCallback callback = stack.load( name, wiring, monitor, kind );
        if( callback != null ){
            reference( callback );
            stack.included( callback.getFile() );
            wire( callback.getFile() );
        }
    }

    /**
     * Called when a callback above this one found a new declaration which thus
     * is included into this one.
     * @param declaration the declaration that was found
     */
    public void included( IDeclaration declaration ){
        if( !wiring ){
            if( collector != null )
                collector.addDeclaration( declaration );
        }
    }
    
    public void macroDefined( IMacro macro ){
        stack.defined( macro );
    }
    
    public void macroUndefined( String name ){
        stack.undefined( name );
    }

    public void fileIncluded( String name, boolean requireLoad, IProgressMonitor monitor ) {
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( name, 1000 );
        
        IParseFile file = stack.getModel().parseFile( name, new SubProgressMonitor( monitor, 500 ));
        if( file == null || monitor.isCanceled() ){
            monitor.done();
            return;
        }

        fileIncluded( file, requireLoad, new SubProgressMonitor( monitor, 500 ) );
        monitor.done();
    }

    public void fileIncluded( File file, boolean requireLoad, IProgressMonitor monitor ) {
        IParseFile parseFile = stack.getModel().parseFile( file );
        if( parseFile == null || monitor.isCanceled() ){
            monitor.beginTask( "Null task", 0 );
            monitor.done();
            return;
        }

        fileIncluded( parseFile, requireLoad, monitor );
    }

    private void fileIncluded( IParseFile file, boolean requireLoad, IProgressMonitor monitor ){
        stack.included( file );
        wire( file );
        LocalCallback callback = stack.load( file, wiring || !requireLoad, monitor );
        if( callback != null ){
            reference( callback );
        }
    }

    /**
     * Stores an included file in this callback.
     * @param file the file that got included
     */
    public void include( IParseFile file ){
        if( file != null && file.isProjectFile() ){
            if( dependencies == null )
                dependencies = new HashSet<IParseFile>();

            dependencies.add( file );
        }
    }

    private void reference( LocalCallback callback ){
        if( references == null )
            references = new HashSet<LocalCallback>();

        references.add( callback );
        
        include( callback.getFile() );
    }
    
    private void wire( IParseFile file ){
        if( wiringSet == null )
            wiringSet = new HashSet<IParseFile>();
        
        wiringSet.add( file );
    }
}

