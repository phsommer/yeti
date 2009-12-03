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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.xml.sax.SAXException;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IProjectCheckerCallback;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

/**
 * Manages paths related to an {@link IProject}, the content of
 * this object gets persistently stored in ".nescpath".
 * @author Benjamin Sigg
 */
public class NesCPath{
	private IProject project;
	private IFolder[] sourceFolders;
	
	private List<INesCPathListener> listeners = new ArrayList<INesCPathListener>();
	
	public NesCPath( IProject project ){
		this.project = project;
		read();
		if( sourceFolders == null ){
			// legacy folder
			sourceFolders = new IFolder[]{ project.getFolder( "src" ) };
		}
	}
	
	public IProject getProject(){
		return project;
	}
	
	/**
	 * Checks all the paths of this {@link NesCPath} and reports
	 * errors.
	 * @param callback to report errors
	 */
	public void check( IProjectCheckerCallback callback ) throws CoreException{
		for( IFolder folder : sourceFolders ){
			if( !folder.exists() ){
				callback.reportError( "Source folder '" + folder.getProjectRelativePath().toString() + "' does not exist" );
			}
		}
	}
	
	public void addListener( INesCPathListener listener ){
		listeners.add( listener );
	}
	
	public void removeListener( INesCPathListener listener ){
		listeners.remove( listener );
	}
	
	protected INesCPathListener[] listeners(){
		return listeners.toArray( new INesCPathListener[ listeners.size() ] );
	}
	
	public IFolder[] getSourceFolders(){
		return sourceFolders;
	}
	
	public void setSourceFolders( IFolder[] sourceFolders ){
		if( !Arrays.equals( this.sourceFolders, sourceFolders )){
			this.sourceFolders = sourceFolders;
			write();
			
			for( INesCPathListener listener : listeners() ){
				listener.sourceFoldersChanged( this );
			}
		}
	}
	
	private void read( XReadStack xml ){
		List<IFolder> sourceFolders = new ArrayList<IFolder>();
		
		if( xml.search( "nescpath" )){
			if( xml.search( "source-folders" )){
				while( xml.go( "source" )){
					IPath path = Path.fromPortableString( xml.getText() );
					sourceFolders.add( project.getFolder( path ) );
					xml.pop();
				}
				xml.pop();
			}
			xml.pop();
		}
		
		this.sourceFolders = sourceFolders.toArray( new IFolder[ sourceFolders.size() ] );
	}
	
	private void write( XWriteStack xml ){
		xml.push( "nescpath" );
		
		xml.push( "source-folders" );
		if( sourceFolders != null ){
			for( IFolder folder : sourceFolders ){
				xml.push( "source" );
				xml.setText( folder.getProjectRelativePath().toPortableString() );
				xml.pop();
			}
		}
		xml.pop();
		
		xml.pop();
	}
	
	public void read(){
		try{
			IFile file = project.getFile( ".nescpath" );
			if( file.exists() ){
				InputStream in = file.getContents();
				XReadStack xml = new XReadStack( in );
				in.close();
				read( xml );
			}
		}
		catch( CoreException e ){
			TinyOSPlugin.log( e );
		}
		catch( SAXException e ){
			TinyOSPlugin.log( e );
		}
		catch( IOException e ){
			TinyOSPlugin.log( e );
		}
	}
	
	public void write(){
		try{
			IFile file = project.getFile( ".nescpath" );
			
			XWriteStack xml = new XWriteStack();
			write( xml );
			
			if( !file.exists() )
				file.create( xml.toInputStream(), IFile.FORCE, null );
			else
				file.setContents( xml.toInputStream(), IFile.FORCE, null );
		}
		catch( CoreException e ){
			TinyOSPlugin.log( e );
		}
	}
}
