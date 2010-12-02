package tinyos.yeti.model.standard.streams;

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
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.model.standard.IStreamProvider;
import tinyos.yeti.model.standard.streams.IPathConverter.Namespace;

/**
 * A set of methods helpful for any {@link IStreamProvider}.
 * @author Benjamin Sigg
 */
public abstract class StreamProvider implements IStreamProvider{
	protected ProjectModel model;

	private IPathConverter converter;
	
	public StreamProvider( ProjectModel model, IPathConverter converter ){
		this.model = model;
		this.converter = converter;
	}

	/**
	 * Recursively deletes <code>file</code> and all its empty parent folders.
	 * @param file the file to delete
	 * @param monitor to monitor progress
	 */
    protected void clearFile( ICacheFile file, IProgressMonitor monitor ){
        if( monitor == null )
        	monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Delete", IProgressMonitor.UNKNOWN );
        try{
        	
        	IResource resource = file.getParent();
        	file.delete( true, new SubProgressMonitor( monitor, 0 ) );
        	
            while( resource.exists() ){
                if( resource instanceof IFolder ){
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
    
    protected void create( ICacheFile file, InputStream in, IProgressMonitor monitor ) throws CoreException{
        ensureExists( file.getParent() );
        
        if( file.exists() ){
            file.setContents( in, IResource.NONE, monitor );
        }
        else{
            file.create( in, true, monitor );
        }
        
        file.setDerived( true );
    }
    
    protected void ensureExists( IContainer parent ) throws CoreException{
        if( !parent.exists() ){
            ensureExists( parent.getParent() );
            if( parent instanceof IFolder ){
                IFolder folder = (IFolder)parent;
                folder.create( true, true, null );
                folder.setDerived( true );
            }
        }
    }
    
    /**
     * Gets the file which will store data for the property <code>extension</code>
     * associated with the file <code>file</code>.
     * @param file the file for which data gets stored
     * @param extension the kind of data to store
     * @return a cache file for which {@link ICacheFile#open()} is called
     * or <code>null</code> if {@link IParseFile#toFile() file.toFile}
     * returns <code>null</code>
     */
    protected ICacheFile getDerivedFile( IParseFile file, String extension ){
        File origin = file.toFile();
        if( origin == null )
            return null;
        
        if( file.isProjectFile() ){
            ICachePath path = derivedPath( Namespace.INTERN, origin, extension );
            return new FileHandle( model.getProject().getInternBinaryContainer(), path );
        }
        else{
            ICachePath path = derivedPath( Namespace.EXTERN, origin, extension );
            return new FileHandle( model.getProject().getExternBinaryContainer(), path );
        }
    }
    
    protected ICachePath derivedPath( Namespace namespace, File file, String extension ){
    	// return new Path( derivedFilePath( file, extension ) );
    	return converter.convert( namespace, derivedFilePath( file, extension ) );
    }
    
    /**
     * Given a source file and the kind of property to store, determines
     * the file in which the property gets stored.
     * @param file the source file
     * @param extension the property to store
     * @return relative path to the file in which the properties are to be stored,
     * the path is relative to {@link ProjectTOS#getInternBinaryContainer()}
     * or {@link ProjectTOS#getExternBinaryContainer()}
     */
    protected abstract IPath derivedFilePath( File file, String extension );
    
    protected class FileHandle implements ICacheFile{
    	private ICachePath path;
    	
    	private IFile file;
    	
    	private int count = 0;
    	
    	public FileHandle( IFolder parent, ICachePath path ){
    		this.path = path;
    		
    		file = parent.getFile( path.getPath() );
    		
    		open();
    	}
    	
    	public void open(){
    		if( count == 0 ){
    			if( !exists() ){
    				path.open();
    			}
    		}
    		count++;
    	}
    	
    	public void close(){
    		count--;
    		if( count == 0 ){
    			if( !exists() ){
    				path.close();
    			}
    		}
    	}

		public void create( InputStream source, boolean force, IProgressMonitor monitor ) throws CoreException{
			file.create( source, force, monitor );
		}

		public void delete( boolean force, IProgressMonitor monitor ) throws CoreException{
			file.delete( force, monitor );
		}

		public boolean exists(){
			return file.exists();
		}

		public InputStream getContents() throws CoreException{
			return file.getContents();
		}

		public IContainer getParent(){
			return file.getParent();
		}

		public boolean isAccessible(){
			return file.isAccessible();
		}

		public void setContents( InputStream source, int updateFlags, IProgressMonitor monitor ) throws CoreException{
			file.setContents( source, updateFlags, monitor );
		}

		public void setDerived( boolean derived ) throws CoreException{
			file.setDerived( derived );
		}
    }
    
/* no longer required since source paths must be handled like library paths */
//    protected IPath difference( File parent, File file ){
//        if( file == null && parent != null ){
//            Debug.error( "file null" );
//            return null;
//        }
//        
//        if( file == parent || file.equals( parent ))
//            return null;
//        
//        IPath path = difference( parent, file.getParentFile() );
//        if( path == null ){
//            path = new Path( file.getName() );
//        }
//        else{
//            path = path.append( file.getName() );
//        }
//        
//        return path;
//    }
}
