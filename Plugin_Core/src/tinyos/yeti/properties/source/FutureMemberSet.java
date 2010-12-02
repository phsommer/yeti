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
package tinyos.yeti.properties.source;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.model.NesCPath;

/**
 * Tells what members (only folders) will a project have in the future, and 
 * which of them will be source folders.
 * @author Benjamin Sigg
 */
public class FutureMemberSet{
	private List<IFolder> sourceFolders = new ArrayList<IFolder>();
	private Set<IFolder> allFolders = new HashSet<IFolder>();
	private Map<IFolder, IPath> linkedFolders = new HashMap<IFolder, IPath>();
	
	private NesCPath path;
	
	public FutureMemberSet( NesCPath path ){
		this.path = path;
		
		for( IFolder folder : path.getSourceFolders() ){
			sourceFolders.add( folder );
			if( folder.isLinked() ){
				linkedFolders.put( folder, folder.getLocation() );
			}
		}
		
		try{
			IProject project = path.getProject();
			IResource[] members = project.members( true );
			if( members != null ){
				for( IResource member : members ){
					if( member instanceof IFolder ){
						allFolders.add( (IFolder)member );
					}
				}
			}
		}
		catch( CoreException e ){
			TinyOSPlugin.log( e );
		}
	}
	
	public boolean folderExists( IFolder folder ){
		return allFolders.contains( folder );
	}
	
	public boolean addSourceFolder( IFolder folder ){
		if( sourceFolders.contains( folder ))
			return false;
		sourceFolders.add( folder );
		return true;
	}
	
	public boolean linkSourceFolder( IFolder folder, IPath path ){
		if( sourceFolders.contains( folder ))
			return false;
		sourceFolders.add( folder );
		allFolders.add( folder );
		linkedFolders.put( folder, path );
		return true;
	}
	
	public void removeSourceFolder( IFolder folder ){
		if( linkedFolders.remove( folder ) != null ){
			allFolders.remove( folder );
		}
		sourceFolders.remove( folder );
	}
	
	public void remove( int[] folderIndices ){
		int[] copy = new int[ folderIndices.length ];
		System.arraycopy( folderIndices, 0, copy, 0, copy.length );
		Arrays.sort( copy );
		
		for( int i = copy.length-1; i >= 0; i-- ){
			IFolder folder = sourceFolders.remove( copy[ i ] );
			if( linkedFolders.remove( folder ) != null ){
				allFolders.remove( folder );
			}
		}
	}
	
	/**
	 * Searches for an error in the new set of source folders.
	 * @return an error or <code>null</code>
	 */
	public String getErrorMessage(){
		// check ancestor relation ships
		IPath[] paths = new IPath[ sourceFolders.size() ];
		int index = 0;
		for( IFolder folder : sourceFolders ){
			IPath path = linkedFolders.get( folder );
			if( path == null )
				path = folder.getLocation();
			paths[ index++ ] = path;
		}
		
		for( int i = 0; i < paths.length; i++ ){
			for( int j = 0; j < paths.length; j++ ){
				if( i != j ){
					if( paths[i].isPrefixOf( paths[j] )){
						return "Source folders must not be nested: '" +
							sourceFolders.get(i).getProjectRelativePath().toOSString() + "' and '" +
							sourceFolders.get(j).getProjectRelativePath().toOSString() + "'";
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Searches for a warning in the new set of source folders.
	 * @return a warning or <code>null</code>
	 */
	public String getWarningMessage(){
		if( sourceFolders.isEmpty() )
			return "No folders selected";
		
		for( IFolder folder : sourceFolders ){
			IPath path = linkedFolders.get( folder );
			if( path == null ){
				if( !folder.exists() ){
					return "Source folder does not exist: '" + folder.getProjectRelativePath().toOSString() + "'";
				}
			}
			else{
				File file = path.toFile();
				if( !file.exists() ){
					return "Source folder does not exist: '" + folder.getProjectRelativePath().toOSString() + "'";
				}
				if( !file.isDirectory() ){
					return "Linked source folder does not point to a directory: '" + folder.getProjectRelativePath().toOSString() + "'";
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Transfers all changes made to this set to {@link NesCPath}.
	 */
	public void close(){
		AdaptJob job = new AdaptJob();
		job.schedule();
	}
	
	/**
	 * This job brings {@link NesCPath} and {@link FutureMemberSet} in sync by
	 * updating the {@link NesCPath}.
	 * @author Benjamin Sigg
	 */
	private class AdaptJob extends Job{
		public AdaptJob(){
			super( "Update paths of '" + path.getProject().getName() + "'" );
			setPriority( SHORT );
			setRule( path.getProject() );
		}
		
		@Override
		protected IStatus run( IProgressMonitor monitor ){
			try{
				Set<IFolder> foldersToRemove = linkedFoldersToRemove();
				Set<IFolder> foldersToAdd = linkedFoldersToAdd();
				monitor.beginTask( "Update folders", foldersToAdd.size() + foldersToRemove.size() );
				
				for( IFolder folder : foldersToRemove ){
					monitor.subTask( "Remove '" + folder.getName() + "'" );
					if( !folder.isLinked() ){
						// this should never happen...
						throw new CoreException( new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, "Canceled, folder '" + folder.getName() + "' is not linked and thus must not be deleted." ));
					}
					folder.delete( false, new SubProgressMonitor( monitor, 1 ) );
				}
				
				for( IFolder folder : foldersToAdd ){
					monitor.subTask( "Add '" + folder.getName() + "'" );
					IPath path = linkedFolders.get( folder );
					folder.createLink( path, 0, new SubProgressMonitor( monitor, 1 ) );
				}
				
				path.setSourceFolders( sourceFolders.toArray( new IFolder[ sourceFolders.size() ] ) );
			}
			catch( CoreException ex ){
				return new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, ex.getMessage(), ex );
			}
			
			return Status.OK_STATUS;
		}
		
		private Set<IFolder> linkedFoldersToRemove() throws CoreException{
			Set<IFolder> folders = new HashSet<IFolder>();
			IResource[] members = path.getProject().members( true );
			for( IResource member : members ){
				if( member instanceof IFolder ){
					IFolder folder = (IFolder)member;
					if( folder.isLinked() ){
						IPath path = linkedFolders.get( folder );
						if( path == null || !path.equals( folder.getLocation() )){
							folders.add( folder );
						}
					}
				}
			}
			return folders;
		}
		
		private Set<IFolder> linkedFoldersToAdd() throws CoreException{
			Set<IFolder> folders = new HashSet<IFolder>();
			Set<IFolder> existing = new HashSet<IFolder>();
			IResource[] members = path.getProject().members( true );
			for( IResource member : members ){
				if( member instanceof IFolder ){
					existing.add( (IFolder)member );
				}
			}
			
			for( Map.Entry<IFolder, IPath> entry : linkedFolders.entrySet() ){
				if( !existing.contains( entry.getKey() )){
					folders.add( entry.getKey() );
				}
				else if( entry.getKey().isLinked() && !entry.getKey().getLocation().equals( entry.getValue() )){
					folders.add( entry.getKey() );
				}
			}
			return folders;
		}
	}
}
