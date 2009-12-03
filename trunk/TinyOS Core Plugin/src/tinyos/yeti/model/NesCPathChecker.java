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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IProjectChecker;
import tinyos.yeti.ep.IProjectCheckerCallback;
import tinyos.yeti.utility.TinyOSProjectsListener;

/**
 * Checks the {@link NesCPath} of projects and may  
 * @author Benjamin Sigg
 *
 */
public class NesCPathChecker implements IProjectChecker, INesCPathListener{
	private ProjectChecker checker;
	private Set<NesCPath> observed = new HashSet<NesCPath>();
	

	public void connect( ProjectChecker checker ){
		this.checker = checker;
		init();
	}
	
	private void init(){
		TinyOSPlugin.getDefault().getProjects().addListener( new TinyOSProjectsListener(){
			public void projectAdded( IProject project ){
				NesCPath path = TinyOSPlugin.getDefault().getPaths( project );
				link( path );
			}
			public void projectRemoved( IProject project ){
				NesCPath path = TinyOSPlugin.getDefault().getPaths( project );
				unlink( path );
			}
		});
		
		TinyOSPlugin.getWorkspace().addResourceChangeListener( new IResourceChangeListener(){
			public void resourceChanged( IResourceChangeEvent event ){
				IResourceDelta delta = event.getDelta();
				if( delta == null )
					return;
				
				try{
					final Set<IProject> projects = new HashSet<IProject>();
					
					delta.accept( new IResourceDeltaVisitor(){
						public boolean visit( IResourceDelta delta ) throws CoreException{
							IResource resource = delta.getResource();
							if( resource instanceof IFolder ){
								projects.add( resource.getProject() );
							}
							
							return true;
						}
					});
					
					for( IProject project : projects ){
						checker.recheck( project );
					}
				}
				catch( CoreException ex ){
					TinyOSPlugin.log( ex );
				}
			}
		});
	}
	
	private void link( NesCPath path ){
		synchronized( observed ){
			if( observed.add( path )){
				path.addListener( this );
			}
		}
	}
	
	private void unlink( NesCPath path ){
		synchronized( observed ){
			observed.remove( path );
			path.removeListener( this );
		}
	}
	
	public void sourceFoldersChanged( NesCPath path ){
		checker.recheck( path.getProject() );
	}
	
	public void checkProject( IProject project, IProgressMonitor monitor, IProjectCheckerCallback callback ) throws CoreException{
		monitor.beginTask( "Check paths", 1 );
		NesCPath paths = TinyOSPlugin.getDefault().getPaths( project );
		if( paths == null ){
			monitor.done();
			return;
		}
		
		paths.check( callback );
		monitor.done();
	}
}
