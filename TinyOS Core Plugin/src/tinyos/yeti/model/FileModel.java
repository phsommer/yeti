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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNodeFactory;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclarationFactory;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.jobs.AllReachableFilesJob;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.model.missing.IMissingResource;
import tinyos.yeti.model.standard.DeclarationCache;
import tinyos.yeti.model.standard.DependencyCache;
import tinyos.yeti.model.standard.GenericFileCache;
import tinyos.yeti.model.standard.ModelNodeFileCache;

/**
 * Contains all the {@link IParseFile}s that can be accessed.
 * @author Benjamin Sigg
 *
 */
public abstract class FileModel<I extends IParseFile> implements Iterable<IParseFile>, IFileModel{
    private Map<File, I> files = new HashMap<File, I>();

    private ProjectModel model;

    private IFileCache<IDeclaration[]> initCache;
    private IFileCache<IDeclaration[]> inclusionCache;
    private IFileCache<Set<IParseFile>> dependencyCache;
    private IFileCache<Set<IParseFile>> wiringCache;
    private IASTModelFileCache astModelCache;
    private IFileCache<IMissingResource[]> missingFileCache;
    private IFileCache<IASTReference[]> referenceCache;
    
    public FileModel( ProjectModel model ){
        this.model = model;
        
        IDeclarationFactory factory = TinyOSPlugin.getDefault().getParserFactory().getDeclarationFactory();
        if( factory != null ){
            initCache = new DeclarationCache( model, factory, "global" );
            inclusionCache = new DeclarationCache( model, factory, "local" );
        }
        dependencyCache = new DependencyCache( model, "dependency" );
        wiringCache = new DependencyCache( model, "wiring" );
        
        IASTModelNodeFactory nodeFactory = TinyOSPlugin.getDefault().getParserFactory().getModelNodeFactory();
        if( nodeFactory != null ){
            astModelCache = new ModelNodeFileCache( model, nodeFactory, "ast" );
        }
        
        missingFileCache = new GenericFileCache<IMissingResource[]>( model, "build" );
        referenceCache = new GenericFileCache<IASTReference[]>( model, "reference" );
    }
    
    public ProjectModel getModel(){
        return model;
    }
    
    public IFileCache<IDeclaration[]> getInitCache(){
        return initCache;
    }
    
    public IFileCache<Set<IParseFile>> getDependencyCache(){
        return dependencyCache;
    }
    
    public IFileCache<IDeclaration[]> getInclusionCache() {
    	return inclusionCache;
    }
    
    public IFileCache<Set<IParseFile>> getWiringCache(){
        return wiringCache;
    }
    
    public IASTModelFileCache getASTModelCache(){
        return astModelCache;
    }
    
    public IFileCache<IMissingResource[]> getMissingFileCache(){
        return missingFileCache;
    }
    
    public IFileCache<IASTReference[]> getReferencesCache(){
	    return referenceCache;
    }
    
    protected abstract I create( File file );
    
    protected abstract I create( File file, Set<File> projectFiles );
    
    protected abstract void setIndex( I file, int index );
    
    protected abstract void setProjectFileResolved( I file, boolean resolved );
    
    public Iterator<IParseFile> iterator(){
        return new Iterator<IParseFile>(){
            private Iterator<I> base = files.values().iterator();

            public boolean hasNext(){
                return base.hasNext();
            }

            public IParseFile next(){
                return base.next();
            }

            public void remove(){
                throw new UnsupportedOperationException( "remove" );
            }
        };
    }

    public List<IParseFile> getAllFiles(){
        return new ArrayList<IParseFile>( files.values() );
    }
    
    public synchronized IParseFile parseFile( File file ){
        I parseFile = files.get( file );
        if( parseFile == null ){
            parseFile = create( file );
            setIndex( parseFile, Integer.MAX_VALUE );
            return parseFile;
        }
        return parseFile;
    }

    public synchronized void clear( boolean full, IProgressMonitor monitor ){
        Iterator<? extends IParseFile> iterator = files.values().iterator();
        IFileCache<?>[] caches = listCaches();
        
        List<IParseFile> toDelete = new ArrayList<IParseFile>();
        
        while( iterator.hasNext() ){
            IParseFile next = iterator.next();
            if( full || next.isProjectFile() ){
                if( !full ){
                    iterator.remove();
                }
                toDelete.add( next );
            }
        }
        if( full ){
            files.clear();
        }
        
        monitor.beginTask( "Clear Caches", toDelete.size() * caches.length );
        for( IParseFile file : toDelete ){
            for( IFileCache<?> cache : caches ){
                SubProgressMonitor sub = new SubProgressMonitor( monitor, 1 );
                cache.clearCache( file, sub );
                sub.done();
            }
            if( monitor.isCanceled() ){
                monitor.done();
                return;
            }
        }
        
        monitor.done();
    }

    private IFileCache<?>[] listCaches(){
        List<IFileCache<?>> list = new ArrayList<IFileCache<?>>();
        if( initCache != null )
            list.add( initCache );
        if( inclusionCache != null )
            list.add( inclusionCache );
        if( dependencyCache != null )
            list.add( dependencyCache );
        if( wiringCache != null )
            list.add( wiringCache );
        if( astModelCache != null )
            list.add( astModelCache );
        if( missingFileCache != null )
            list.add( missingFileCache );
        if( referenceCache != null )
        	list.add( referenceCache );
        
        return list.toArray( new IFileCache[ list.size() ] );
    }
    
    /**
     * Gets the files of this model ordered by their index.
     * @return the files known to this model
     */
    public IParseFile[] getFiles(){
        IParseFile[] result = files.values().toArray( new IParseFile[ files.size() ] );
        Arrays.sort( result, new Comparator<IParseFile>(){
            public int compare( IParseFile o1, IParseFile o2 ){
                if( o1.getIndex() < o2.getIndex() )
                    return -1;
                if( o1.getIndex() > o2.getIndex() )
                    return 1;
                return 0;
            }
        });
        return result;
    }

    public IParseFile[] getFiles( String extension ){
        List<IParseFile> list = new ArrayList<IParseFile>( files.size() );
        String dot = "." + extension;

        for( I file : files.values() ){
            if( file.getName().endsWith( dot )){
                list.add( file );
            }
        }

        IParseFile[] result = list.toArray( new IParseFile[ list.size() ] );
        Arrays.sort( result, new Comparator<IParseFile>(){
            public int compare( IParseFile o1, IParseFile o2 ){
                if( o1.getIndex() < o2.getIndex() )
                    return -1;
                if( o1.getIndex() > o2.getIndex() )
                    return 1;
                return 0;
            }
        });
        return result;
    }

    /**
     * Refreshes the contents of this model and makes sure that all reachable
     * files are listed up.
     * @param target the make target which defines the files
     */
    public void refresh( MakeTarget target, IProgressMonitor monitor ){
        TinyOSPlugin plugin = TinyOSPlugin.getDefault();
        if( plugin == null )
            return;

        AllReachableFilesJob job = new AllReachableFilesJob( model.getProject(), target, "h", "nc" );
        model.runJob( job, monitor );
        
        File[] files = job.getFiles();
        if( files == null )
            files = new File[]{};
        
        refresh( files );
    }

    private synchronized void refresh( File[] files ){
        for( I file : this.files.values() )
            setIndex( file, -1 );

        final Set<File> projectFiles = new HashSet<File>();
        
        try{
            model.getProject().acceptSourceFiles( new IResourceVisitor(){
                public boolean visit( IResource resource ) throws CoreException{
                    IPath path = resource.getLocation();
                    if( path != null ){
                        projectFiles.add( path.toFile() );
                    }
                    return true;
                }
            });
        }
        catch ( CoreException e ){
            TinyOSPlugin.warning( e.getStatus() );
        }
        
        int index = 0;

        for( File file : files ){
            I parseFile = this.files.get( file );
            if( parseFile == null ){
                parseFile = create( file, projectFiles );
                this.files.put( file, parseFile );
            }
            setIndex( parseFile, index++ );
        }

        Iterator<Map.Entry<File, I>> iterator = this.files.entrySet().iterator();
        while( iterator.hasNext() ){
            if( iterator.next().getValue().getIndex() == -1 )
                iterator.remove();
        }
    }

    protected IContainer resolveProjectFile( final I file ){
        try{
        	final File located = file.toFile();
        	
        	class Search implements IResourceVisitor{
        		public IContainer result;
        		
        		public boolean visit( IResource resource ) throws CoreException{
        			if( result != null )
        				return false;
        			
        			IPath path = resource.getLocation();
                    if( path != null ){
                        if( located.equals( path.toFile() ) ){
                        	result = model.getProject().getSourceContainer( resource.getProjectRelativePath() );
                        }
                    }
                    return true;
        		}
        	}
        	
        	Search visitor = new Search();
        	model.getProject().acceptSourceFiles( visitor );
            return visitor.result;
        }
        catch( CoreException ex ){
            ex.printStackTrace();
        }
        
        return null;
    }
    
}
