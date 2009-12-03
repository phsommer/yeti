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
package tinyos.yeti.model.standard;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.Debug;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.model.IFileCache;
import tinyos.yeti.model.ProjectModel;

public abstract class StandardFileCache<V> implements IFileCache<V>{
    protected ProjectModel model;
    private String extension;
    
    public StandardFileCache( ProjectModel model, String extension ){
        this.model = model;
        this.extension = extension;
    }
    
    /**
     * Gets the file which represents <code>file</code>.
     * @param file the file which gets cached
     * @return the cache file for <code>file</code>
     */
    protected IFile getCacheFile( IParseFile file ){
        return getDerivedFile( file, extension );
    }

    protected IFile getDerivedFile( IParseFile file, String extension ){
        File origin = file.toFile();
        if( origin == null )
            return null;
        
        if( file.isProjectFile() ){
            IPath path = derivedPath( null, origin, extension );
            return model.getProject().getInternBinaryContainer().getFile( path );
        }
        else{
            IPath path = derivedPath( null, origin, extension );
            return model.getProject().getExternBinaryContainer().getFile( path );
        }
    }
    
    protected IPath derivedPath( File parent, File file, String extension ){
    	if( parent == null )
    		return new Path( file.getAbsolutePath() + "." + extension );
    	
        IPath path = difference( parent, file.getParentFile() );
        if( path == null )
            return new Path( file.getName() + "." + extension );
        else
            return path.append( file.getName() + "." + extension );
    }
    
    protected IPath difference( File parent, File file ){
        if( file == null && parent != null ){
            Debug.error( "file null" );
            return null;
        }
        
        if( file == parent || file.equals( parent ))
            return null;
        
        IPath path = difference( parent, file.getParentFile() );
        if( path == null ){
            path = new Path( file.getName() );
        }
        else{
            path = path.append( file.getName() );
        }
        
        return path;
    }
    
    public boolean canReadCache( IParseFile file ){
        IFile cache = getCacheFile( file );
        if( cache == null )
            return false;
        
        return cache.exists();
    }
    

    public void clearCache( IParseFile file, IProgressMonitor monitor ){
        IFile cache = getCacheFile( file );
        if( cache != null ){
            clearFile( cache, monitor );
        }
    }

    protected void clearFile( IFile file, IProgressMonitor monitor ){
        IResource resource = file;
        monitor.beginTask( "Delete", IProgressMonitor.UNKNOWN );
        try{
            while( resource.exists() ){
                if( resource instanceof IFile ){
                    resource.delete( true, new SubProgressMonitor( monitor, 0 ) );
                    resource = resource.getParent();
                }
                else if( resource instanceof IFolder ){
                    IFolder folder = (IFolder)resource;
                    IResource[] members = folder.members( IFolder.INCLUDE_PHANTOMS | IFolder.INCLUDE_TEAM_PRIVATE_MEMBERS );
                    if( members.length == 0 ){
                        folder.delete( true, new SubProgressMonitor( monitor, 0 ) );
                    }
                    else{
                        break;
                    }
                    resource = resource.getParent();
                }
                else{
                    break;
                }
            }
        }
        catch( CoreException ex ){
            TinyOSPlugin.warning( ex.getStatus() );
        }
        monitor.done();
    }

    protected void create( IFile file, InputStream in, IProgressMonitor monitor ) throws CoreException{
        ensureParentExists( file );
        
        if( file.exists() ){
            file.setContents( in, IResource.NONE, monitor );
        }
        else{
            file.create( in, true, monitor );
        }
        
        file.setDerived( true );
    }
    
    protected void ensureParentExists( IResource resource ) throws CoreException{
        IContainer parent = resource.getParent();
        if( !parent.exists() ){
            ensureParentExists( parent );
            if( parent instanceof IFolder ){
                IFolder folder = (IFolder)parent;
                folder.create( true, true, null );
                folder.setDerived( true );
            }
        }
    }
    
}
