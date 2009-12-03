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
package tinyos.yeti.environment.basic.checker;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.EnvironmentManager;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IEnvironmentListener;
import tinyos.yeti.ep.IProjectChecker;
import tinyos.yeti.ep.IProjectCheckerCallback;
import tinyos.yeti.model.ProjectChecker;

/**
 * An abstract implementation of {@link IProjectChecker}. This class calls
 * {@link #check(IProjectCheckerCallback)} for any project that uses
 * this environment. Recheck is called if the environment of a project
 * changes.
 * @author Benjamin Sigg
 */
public abstract class AbstractEnvironmentChecker implements IProjectChecker{
	private ProjectChecker checker;
	
	public void connect( ProjectChecker checker ){
		this.checker = checker;
		init();
	}
	
	private void init(){
		EnvironmentManager.getDefault().addInitializingListener( new EnvironmentManager.Listener(){
			public void environmentChanged( IProject project ){
				checker.recheck( project );	
			}
			public void initialized(){
				connect();	
			}
		});
	}
		
	private void connect(){
		getEnvironment().addEnvironmentListener( new IEnvironmentListener(){
			public void reinitialized( final IEnvironment environment ){
				// check all projects with this environment
				Job recheck = new Job( "Check all" ){
					@Override
					protected IStatus run( IProgressMonitor monitor ){
						monitor.beginTask( "Check", 1 );
						
						IProject[] projects = TinyOSPlugin.getDefault().getProjects().getProjects();
						EnvironmentManager environments = EnvironmentManager.getDefault();
						for( IProject project : projects ){
							if( project.isOpen() ){
								if( environments.getEnvironment( project ) == environment ){
									checker.recheck( project );
								}
							}
						}
						
						monitor.done();
						return Status.OK_STATUS;
					}
				};
				recheck.setSystem( true );
				recheck.setPriority( Job.SHORT );
				recheck.setRule( TinyOSPlugin.getWorkspace().getRoot() );
				recheck.schedule();
			}
		});
	}
	
	protected abstract IEnvironment getEnvironment();
	
	protected abstract void check( IProjectCheckerCallback callback ) throws CoreException;
	
	public void checkProject( IProject project, IProgressMonitor monitor, IProjectCheckerCallback callback ) throws CoreException{
		IEnvironment environment = getEnvironment();
		
	    if( EnvironmentManager.getDefault().getEnvironment( project ) == environment ){
	    	// report errors of this environment
	    	check( callback );
	    }
	}
	
	protected boolean checkDir( String name, String path, IProjectCheckerCallback callback ) throws CoreException{
		File file = getEnvironment().modelToSystem( path );
		if( file == null ){
			callback.reportError( getEnvironment().getEnvironmentName() + ", '" + name + "': directory does not exist: '" + path + "'" );
			return false;
		}
		return checkDir( name, file, callback );
	}
	
	protected boolean checkDir( String name, File file, IProjectCheckerCallback callback ) throws CoreException{
		if( !file.exists() ){
			callback.reportError( getEnvironment().getEnvironmentName() + ", '" + name + "': directory does not exist: '" + file.getPath() + "'" );
			return false;
		}
		else if( !file.isDirectory() ){
			callback.reportError( getEnvironment().getEnvironmentName() + ", '" + name + "': not a directory: '" + file.getPath() + "'" );
			return false;
		}
		else if( !file.isAbsolute() ){
			callback.reportWarning( getEnvironment().getEnvironmentName() + ", '" + name + "': not absolute path: '" + file.getPath() + "'" );
			return false;
		}
		return true;
	}
	
	protected boolean checkFile( String name, String path, IProjectCheckerCallback callback ) throws CoreException{
		File file = getEnvironment().modelToSystem( path );
		if( file == null ){
			callback.reportError( getEnvironment().getEnvironmentName() + ", '" + name + "': file does not exist: '" + path + "'" );
			return false;
		}
		
		return checkFile( name, file, callback );
	}
	
	protected boolean checkFile( String name, File file, IProjectCheckerCallback callback ) throws CoreException{
		if( !file.exists() ){
			callback.reportError( getEnvironment().getEnvironmentName() + ", '" + name + "': file does not exist: '" + file.getPath() + "'" );
			return false;
		}
		else if( !file.isFile() ){
			callback.reportError( getEnvironment().getEnvironmentName() + ", '" + name + "': not a file: '" + file.getPath() + "'" );
			return false;
		}
		else if( !file.isAbsolute() ){
			callback.reportWarning( getEnvironment().getEnvironmentName() + ", '" + name + "': not absolute path: '" + file.getPath() + "'" );
			return false;
		}
		return true;
	}
}