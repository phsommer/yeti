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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.Debug;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.model.ProjectModel;

/**
 * A set of methods helpful for any {@link IStreamProvider}.
 * @author Benjamin Sigg
 */
public abstract class StreamProvider implements IStreamProvider{
	protected ProjectModel model;

	public StreamProvider( ProjectModel model ){
		this.model = model;
	}

	/**
	 * Recursively deletes <code>file</code> and all its empty parent folders.
	 * @param file the file to delete
	 * @param monitor to monitor progress
	 */
    protected void clearFile( IFile file, IProgressMonitor monitor ){
        if( monitor == null )
        	monitor = new NullProgressMonitor();
        
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
    		return new Path( derivedFileName( file, extension, true ) );
    	
        IPath path = difference( parent, file.getParentFile() );
        if( path == null )
            return new Path( derivedFileName( file, extension, false ) );
        else
            return path.append( derivedFileName( file, extension, false ) );
    }
    
    protected abstract String derivedFileName( File file, String extension, boolean absolute );
    
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
}
