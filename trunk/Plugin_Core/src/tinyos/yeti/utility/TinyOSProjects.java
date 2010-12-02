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
package tinyos.yeti.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;

/**
 * Keeps a list of all projects that have nature {@link TinyOSCore#NATURE_ID}
 * and updates the list if necessary.
 * @author Benjamin Sigg
 *
 */
public class TinyOSProjects implements IResourceChangeListener, IResourceDeltaVisitor{
	private Set<IProject> openTinyosProjects = Collections.synchronizedSet( new HashSet<IProject>() );
	private List<TinyOSProjectsListener> listeners = new ArrayList<TinyOSProjectsListener>();
	
	public TinyOSProjects(){
		ResourcesPlugin.getWorkspace().addResourceChangeListener( this );
		try{
			for( IProject project : currentProjects() ){
				openTinyosProjects.add( project );
			}
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex );
		}
	}

	public void addListener( TinyOSProjectsListener listener ){
		if( listener == null )
			throw new IllegalArgumentException( "listener must not be null" );
		synchronized( listeners ){
			listeners.add( listener );
		}
	}
	
	public void removeListener( TinyOSProjectsListener listener ){
		synchronized( listeners ){
			listeners.remove( listener );
		}
	}
	
	private TinyOSProjectsListener[] listeners(){
		synchronized( listeners ){
			return listeners.toArray( new TinyOSProjectsListener[ listeners.size() ] );
		}
	}
	
	public void dispose(){
		ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );
		openTinyosProjects.clear();
	}
	
	public void resourceChanged( IResourceChangeEvent event ){
		try{
			IResourceDelta delta = event.getDelta();
			if( delta != null ){
				delta.accept( this );
			}
		}
		catch( CoreException e ){
			TinyOSPlugin.log( e );
		}
	}
	
	public boolean visit( IResourceDelta delta ) throws CoreException{
		IResource resource = delta.getResource();
		if( resource instanceof IProject ){
			IProject project = (IProject)resource;
			switch( delta.getKind() ){
				case IResourceDelta.ADDED:
				case IResourceDelta.ADDED_PHANTOM:
					if( project.isOpen() && project.hasNature( TinyOSCore.NATURE_ID )){
						added( project );
					}
					break;
					
				case IResourceDelta.REMOVED:
				case IResourceDelta.REMOVED_PHANTOM:
					if( project.isOpen() && project.hasNature( TinyOSCore.NATURE_ID )){
						removed( project );
					}
					break;
				case IResourceDelta.CHANGED:
					if( project.isOpen() && project.hasNature( TinyOSCore.NATURE_ID )){
						if( openTinyosProjects.add( project )){
							added( project );
						}
					}
					else{
						if( openTinyosProjects.remove( project )){
							removed( project );
						}
					}
					break;
			}
			return false;
		}
		if( resource instanceof IFile || resource instanceof IFolder )
			return false;
		
		return true;
	}
	
	private void added( IProject project ){
		for( TinyOSProjectsListener listener : listeners() ){
			listener.projectAdded( project );
		}
	}
	
	private void removed( IProject project ){
		for( TinyOSProjectsListener listener : listeners() ){
			listener.projectRemoved( project );
		}
	}
	
	public IProject[] getProjects(){
		return openTinyosProjects.toArray( new IProject[ openTinyosProjects.size() ] );
	}
	
	private IProject[] currentProjects() throws CoreException{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		List<IProject> result = new ArrayList<IProject>();
		for( IProject project : projects ){
			if( project.isOpen() && project.hasNature( TinyOSCore.NATURE_ID )){
				result.add( project );
			}
		}
		return result.toArray( new IProject[ result.size() ] );
	}
}
