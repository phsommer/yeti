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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.INesCDefinitionCollector;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.model.IProjectDefinitionCollector;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.model.wiring.CallbackStack;
import tinyos.yeti.model.wiring.WireCache;
import tinyos.yeti.model.wiring.WireCacheEntry;
import tinyos.yeti.nesc.IMultiReader;

/**
 * A definition collector whose cache stores only the declared declarations
 * of a file, not the included ones.
 * @author Benjamin Sigg
 */
public class LocalProjectDefinitionCollector implements IProjectDefinitionCollector{
    private ProjectModel model;

    private WireCache cache;

    private boolean onStandardIncludesLoading = false;

    /** a list of includes that are always present */
    private IDeclaration[] standardIncludes;
    
    private Map<String, IMacro> macroCollection = new HashMap<String, IMacro>();
    private IMacro[] macros;
    
    private Set<IParseFile> basicFiles = new HashSet<IParseFile>();
    private IParseFile[] basicFilesArray;

    public LocalProjectDefinitionCollector( ProjectModel model ) {
        this.model = model;
        cache = new WireCache( model.getFileModel() );
    }

    public void deleteCache( IProgressMonitor monitor ){
        cache.clear( monitor );
        deleteStandardCache();
    }
    
    private void deleteStandardCache(){
        standardIncludes = null;
        macroCollection.clear();
        macros = null;
        basicFiles.clear();
        basicFilesArray = null;
    }

    public void deleteCache( IParseFile file, IProgressMonitor monitor ){
        cache.getEntry( file ).clearForBuild( monitor );
    }

    /**
     * Gets the maximal number of items in the cache
     * @return the number of items
     */
    public int getCacheSize(){
        return cache.getCacheSize();
    }

    /**
     * Sets the maximal number of items of the cache
     * @param size the number of items
     */
    public void setCacheSize( int size ){
        cache.setCacheSize( size );
    }

    public IParseFile[] getCachedFiles(){
    	if( !model.secureThread() )
    		throw new IllegalStateException( "this method must be called by a secure thread" );
    	
        return cache.getCachedFiles();
    }

    /**
     * Tells whether <code>file</code> includes the file <code>inclusion</code>.
     * @param file the file to check
     * @param inclusion the file that might be included
     * @return <code>true</code> if <code>included</code> is included in <code>file</code>
     */
    public boolean includes( IParseFile file, IParseFile inclusion ){
        return cache.getEntry( file ).depends( inclusion );
    }

    /**
     * Ensures that the standard includes are available
     * @param monitor to report progress
     */
    private void ensureStandardIncludes( IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Build Standard Includes", 1000 );
        
        if( standardIncludes == null && !onStandardIncludesLoading ){
            onStandardIncludesLoading = true;
            try{
            	deleteStandardCache();
            	
                standardIncludes = new IDeclaration[0];

                File[] includes = model.getBasicDeclarations().listGlobalInclusionFiles();
                if( includes != null ){
                    SubProgressMonitor sub = new SubProgressMonitor( monitor, 1000 );
                    sub.beginTask( "Build Standard Includes", includes.length );
                    
                    for( File file : includes ){
                        List<IDeclaration> adds = collect( file, null, false, new SubProgressMonitor( sub, 1 ) );
                        if( adds != null ){
                            IDeclaration[] temp = new IDeclaration[ standardIncludes.length + adds.size() ];
                            System.arraycopy( standardIncludes, 0, temp, 0, standardIncludes.length );
                            int index = standardIncludes.length;
                            for( IDeclaration declaration : adds ){
                                temp[ index++ ] = declaration;
                            }
                            standardIncludes = temp;
                        }
                    }
                    
                    sub.done();
                }
            }
            finally{
                onStandardIncludesLoading = false;
            }
        }
        
        monitor.done();
    }

    public void updateInclusions( IParseFile file, IMultiReader reader, IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();

        Stack stack = new Stack();
        if( file == null ){
            monitor.beginTask( "Null task", 1 );
            monitor.done();
            return;
        }

        monitor.beginTask( "Update inclusions", 1000 );

        cache.getEntry( file ).clearForUpdate( true, new SubProgressMonitor( monitor, 200) );
        if( monitor.isCanceled() ){
            monitor.done();
            return;
        }
        stack.load( file, reader, false, true, new SubProgressMonitor( monitor, 800 ) );

        monitor.done();
    }

    public void updateInclusions( IParseFile file, Set<IParseFile> parseFiles ){
        cache.getEntry( file ).depends( parseFiles, false, null );
    }

    public IDeclaration[] getBasicDeclarations( IProgressMonitor monitor ){
        ensureStandardIncludes( monitor );
        return standardIncludes;
    }
    
    public IMacro[] getBasicMacros( IProgressMonitor monitor ){
        ensureStandardIncludes( monitor );
        if( macros == null ){
            macros = macroCollection.values().toArray( new IMacro[ macroCollection.size() ] );
        }
        return macros;
    }
    
    public IParseFile[] getBasicFiles( IProgressMonitor monitor ){
    	ensureStandardIncludes( monitor );
    	if( basicFilesArray == null ){
    		basicFilesArray = basicFiles.toArray( new IParseFile[ basicFiles.size() ] );
    	}
    	return basicFilesArray;
    }

    public List<IDeclaration> collect( File file, IMultiReader reader, boolean onlyIncluded, IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();

        monitor.beginTask( "Collect file '" + file.getName() + "'", 1000 );
        Stack stack = new Stack();

        IParseFile parseFile = model.parseFile( file );
        if( parseFile == null ){
            monitor.done();
            return null;
        }

        cache.getEntry( parseFile ).clearForUpdate( true, new SubProgressMonitor( monitor, 100 ));
        LocalCallback callback = stack.load( parseFile, reader, onlyIncluded, false, new SubProgressMonitor( monitor, 800 ) );
        
        if( callback == null || monitor.isCanceled() ){
            monitor.done();
            return null;
        }

        ensureStandardIncludes( new SubProgressMonitor( monitor, 100 ));

        List<IDeclaration> included = callback.collectIncluded();
        List<IDeclaration> declared = callback.getDeclared();

        int size = onStandardIncludesLoading ? 0 : standardInclusionSize();
        if( included != null )
            size += included.size();

        if( !onlyIncluded && declared != null )
            size += declared.size();

        List<IDeclaration> result = new ArrayList<IDeclaration>( size );

        if( !onlyIncluded && declared != null ){
            result.addAll( declared );
        }
        if( included != null )
            result.addAll( included );

        if( !onStandardIncludesLoading ){
            insertStandardIncludes( result, null );
        }
        monitor.done();
        return result;
    }

    private int standardInclusionSize(){
        return standardIncludes.length;
    }

    private void insertStandardIncludes( Collection<? super IDeclaration> writeable, IProgressMonitor monitor ){
        ensureStandardIncludes( monitor );
        for( IDeclaration include : standardIncludes )
            writeable.add( include );
    }

    private class Stack implements CallbackStack<LocalCallback>{
        /** the set of all files that are currently processed or were processed and remain loaded */
        private Map<IParseFile, LocalCallback> files = new HashMap<IParseFile, LocalCallback>();
        /** the stack of currently active callbacks */
        private LinkedList<LocalCallback> callbacks = new LinkedList<LocalCallback>();

        public INesCDefinitionCollector createCollector( IParseFile file ){
        	if( onStandardIncludesLoading ){
        		basicFiles.add( file );
        	}
        	
            TinyOSPlugin plugin = TinyOSPlugin.getDefault();
            if( plugin == null )
                return null;

            INesCDefinitionCollector collector = plugin.getParserFactory().createCollector( model.getProject(), file );
            if( collector == null )
                return null;
            
            IMacro[] macros = model.getBasicDeclarations().listBasicMacros();
            
            //model.getProject().getMakeTarget().getPlatform().getMacros()
            
            if( macros != null ){
                for( IMacro macro : macros ){
                    collector.addMacro( macro );
                }
            }
            
            collector.setReportMacros( onStandardIncludesLoading );
            
            return collector;
        }

        public void declared( IDeclaration declaration ){
            int count = callbacks.size();

            for( LocalCallback callback : callbacks ){
                if( count == 0 )
                    break;

                count--;
                callback.included( declaration );
            }
        }

        public void defined( IMacro macro ){
        	macroCollection.put( macro.getName(), macro );
        	macros = null;
        }
        
        public void undefined( String macro ){
        	macroCollection.remove( macro );
        	macros = null;
        }
        
        public void included( IParseFile file ){
            if( file.isProjectFile() ){
                for( LocalCallback callback : callbacks ){
                    callback.include( file );
                }
            }
            if( onStandardIncludesLoading ){
            	basicFiles.add( file );
            }
        }

        public ProjectModel getModel() {
            return model;
        }

        public LocalCallback load( String name, boolean wiring, IProgressMonitor monitor, Kind... kind ){
            IDeclaration declaration = model.getDeclaration( name, kind );
            if( declaration != null ){
                IParseFile file = declaration.getParseFile();
                if( file != null ){
                    return load( file, wiring, monitor );
                }
            }

            monitor.beginTask( "Missing file", 1 );
            monitor.done();

            return null;
        }

        public LocalCallback load( IParseFile file, boolean wiring, IProgressMonitor monitor ){
            return load( file, null, false, wiring, monitor );
        }

        public LocalCallback load( IParseFile file, IMultiReader reader, boolean onlyIncluded, boolean wiring, IProgressMonitor monitor ){
            monitor.beginTask( "Load file", 1000 );

            // maybe there exists already a Callback for this file
            LocalCallback callback = files.get( file );
            if( callback != null ){
                monitor.done();
                return callback;
            }

            callback = new LocalCallback( this, file, onlyIncluded, wiring );
            files.put( file, callback );

            WireCacheEntry entry = cache.getEntry( file );

            callbacks.addLast( callback );
            callback.load( reader, entry, new SubProgressMonitor( monitor, 500 ) );
            callbacks.removeLast();

            if( monitor.isCanceled() )
                return null;

            if( callbacks.isEmpty() ){
                for( LocalCallback accessed : files.values() ){
                    accessed.finish( new SubProgressMonitor( monitor, 500 ) );

                    if( monitor.isCanceled() )
                        return null;
                }
            }

            return callback;
        }

    }
}