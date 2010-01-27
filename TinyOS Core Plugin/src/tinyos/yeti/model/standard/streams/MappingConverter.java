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
package tinyos.yeti.model.standard.streams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import tinyos.yeti.Debug;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;

/**
 * Maps {@link IPath}s to new paths, adds numbers to the new paths to prevent
 * collisions.
 * @author Benjamin Sigg
 */
public abstract class MappingConverter implements IPathConverter{
	/** the paths and their associated {@link CachePath} that are currently open */
	private Map<IPath, CachePath> openPaths = new HashMap<IPath, CachePath>();
	private Set<IPath> destinations = new HashSet<IPath>();
	
	private IFile infoFile;
	
	public MappingConverter( ProjectTOS project ){
		infoFile = project.getCacheContainer().getFile( "mapping" );
		readMapping();
	}
	
	private synchronized void readMapping(){
		if( infoFile.exists() && infoFile.isAccessible() ){
			try{
				DataInputStream in = new DataInputStream( infoFile.getContents() );
				 
				int count = in.readInt();
				for( int i = 0; i < count; i++ ){
					CachePath path = new CachePath( in );
					destinations.add( path.destination );
					openPaths.put( path.source, path );
				}
			}
			catch( IOException ex ){
				TinyOSPlugin.warning( ex );
			}
			catch( CoreException ex ){
				TinyOSPlugin.warning( ex );
			}
		}
	}
	
	private synchronized void writeMapping(){
		try{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream( bout );
			
			out.writeInt( openPaths.size() );
			
			for( CachePath path : openPaths.values() ){
				path.write( out );
			}
			
			out.close();
			
			create( infoFile.getParent() );
			if( infoFile.exists() ){
				infoFile.setContents( new ByteArrayInputStream( bout.toByteArray() ), true, false, null );
			}
			else{
				infoFile.create( new ByteArrayInputStream( bout.toByteArray() ), true, null );
			}
		}
		catch( IOException ex ){
			TinyOSPlugin.log( ex );
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex );
		}
	}
	
	private void create( IContainer container ) throws CoreException{
		if( container instanceof IFolder ){
			IFolder folder = (IFolder)container;
			if( !folder.exists() ){
				create( folder.getParent() );
				folder.create( true, true, null );
			}
		}
	}
	
	public synchronized ICachePath convert( Namespace namespace, IPath path ){
		CachePath open = openPaths.get( path );
		if( open == null ){
			open = new CachePath( path, convertPath( namespace, path ));
		}
		return open;
	}
	
	protected abstract IPath convertPath( Namespace namespace, IPath path );
	
	/**
	 * Ensures that the path <code>path</code> is unique compared to all
	 * the other paths that are currently in use. 
	 * @param path a path derived from 
	 * {@link #convertPath(tinyos.yeti.model.standard.streams.IPathConverter.Namespace, IPath) convertPath}
	 * @return a unique version of <code>path</code>
	 */
	private IPath ensureUnique( IPath path ){
		if( !destinations.contains( path ) ){
			return path;
		}
		
		String extension = path.getFileExtension();
		if( extension != null ){
			path = path.removeFileExtension();
		}
		
		int index = 1;
		while( true ){
			IPath check = path.addFileExtension( String.valueOf( index ) );
			if( extension != null ){
				check = check.addFileExtension( extension );
			}
			if( !destinations.contains( check )){
				return check;
			}
			index++;
		}
	}
	
	private class CachePath implements ICachePath{
		private int count = 0;
		private IPath source;
		private IPath destination;
		
		public CachePath( IPath source, IPath destination ){
			this.source = source;
			this.destination = destination;
		}
		
		public CachePath( DataInputStream in ) throws IOException{
			count = in.readInt();
			source = Path.fromPortableString( in.readUTF() );
			destination = Path.fromPortableString( in.readUTF() );
		}
		
		public void write( DataOutputStream out ) throws IOException{
			out.writeInt( count );
			out.writeUTF( source.toPortableString() );
			out.writeUTF( destination.toPortableString() );
		}
		
		public IPath getPath(){
			return destination;
		}
		
		public void open(){
			synchronized( MappingConverter.this ){
				if( count == 0 ){
					destination = ensureUnique( destination );
					openPaths.put( source, this );
					destinations.add( destination );
					if( Debug.DEBUG ){
						Debug.info( "open cache: " + source + " -> " + destination );
					}
					writeMapping();
				}
				count++;
			}
		}
		
		public void close(){
			synchronized( MappingConverter.this ){
				count--;
				if( count == 0 ){
					openPaths.remove( source );
					destinations.remove( destination );
					if( Debug.DEBUG ){
						Debug.info( "close cache: " + source + " -> " + destination );
					}
					writeMapping();
				}
			}
		}
	}
}
