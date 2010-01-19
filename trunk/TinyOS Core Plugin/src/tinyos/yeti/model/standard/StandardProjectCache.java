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
package tinyos.yeti.model.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNodeFactory;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclarationFactory;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.model.IASTModelFileCache;
import tinyos.yeti.model.IFileCache;
import tinyos.yeti.model.IProjectCache;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.model.missing.IMissingResource;

/**
 * Standard implementation of {@link IProjectCache}, using one
 * file for each pair of nesc-file and property.
 * @author Benjamin Sigg
 */
public class StandardProjectCache implements IProjectCache{
    private IFileCache<IDeclaration[]> initCache;
    private IFileCache<IDeclaration[]> inclusionCache;
    private IFileCache<Set<IParseFile>> dependencyCache;
    private IFileCache<Set<IParseFile>> wiringCache;
    private IASTModelFileCache astModelCache;
    private IFileCache<IMissingResource[]> missingFileCache;
    private IFileCache<IASTReference[]> referenceCache;

    private ProjectModel model;

    public void initialize( ProjectModel model ){
        this.model = model;
        IStreamProvider streams = createStreamProvider( model );
        
        IDeclarationFactory factory = TinyOSPlugin.getDefault().getParserFactory().getDeclarationFactory();
        if( factory != null ){
            initCache = new DeclarationCache( model, factory, "global", streams );
            inclusionCache = new DeclarationCache( model, factory, "local", streams );
        }
        dependencyCache = new DependencyCache( model, "dependency", streams );
        wiringCache = new DependencyCache( model, "wiring", streams );
        
        IASTModelNodeFactory nodeFactory = TinyOSPlugin.getDefault().getParserFactory().getModelNodeFactory();
        if( nodeFactory != null ){
            astModelCache = new ModelNodeFileCache( model, nodeFactory, "ast", streams );
        }
        
        missingFileCache = new GenericFileCache<IMissingResource[]>( model, "build", streams );
        referenceCache = new GenericFileCache<IASTReference[]>( model, "reference", streams );
    }
    
    protected IStreamProvider createStreamProvider( ProjectModel model ){
    	return new StandardStreamProvider( model );
    }
    
    public String getTypeIdentifier(){
    	// in case of refactoring, don't change this identifier
	    return "tinyos.yeti.model.ProjectCache";
    }

    public synchronized void clear( boolean full, IProgressMonitor monitor ){
        IFileCache<?>[] caches = listCaches();
        
        List<IParseFile> toDelete = new ArrayList<IParseFile>();
        
        for( IParseFile file : model.getFileModel().getAllFiles() ){
            if( full || file.isProjectFile() ){
                toDelete.add( file );
            }
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
}
