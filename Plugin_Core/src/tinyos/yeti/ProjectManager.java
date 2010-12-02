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
package tinyos.yeti;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IEnvironmentListener;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.model.NesCPath;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.utility.PlatformObserver;

public class ProjectManager {
    private ListenerList lastAccessedListeners = new ListenerList( ListenerList.IDENTITY );

    private Map<String, ProjectTOS> projects;
    private IProject lastProject = null;
    
    private ReinitializeJob reinitialize = new ReinitializeJob();
    
    private Map<IProject, NesCPath> paths = new Hashtable<IProject, NesCPath>();
    
    public ProjectManager() {
        projects = new Hashtable<String, ProjectTOS>();
    }

    public void connect( Collection<IEnvironment> environments ){
        for( IEnvironment environment : environments ){
            environment.addEnvironmentListener( new IEnvironmentListener(){
                public void reinitialized( IEnvironment environment ) {
                	// reinitialize projects using environment
                    reinitialize.add( environment );
                }
            });
            new PlatformObserver( environment ){
            	@Override
            	public void pathsChanged( IPlatform platform ) {
            		// reinitialize projects using platform
            		reinitialize.add( platform );
            	}
            };
        }
    }
    
    public void replaceCaches(){
    	Job replaceJob = new Job( "Clean open TinyOS projects" ){
			@Override
			protected IStatus run( IProgressMonitor monitor ){
				monitor.beginTask( "Clean open TinyOS projects", projects.size() );
				
				for( ProjectTOS project : projects.values() ){
					monitor.subTask( project.getProject().getName() );
					project.getModel().replaceCache( new SubProgressMonitor( monitor, 1 ) );
				}
				
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		replaceJob.setPriority( Job.LONG );
		replaceJob.setRule( ResourcesPlugin.getWorkspace().getRoot() );
		replaceJob.schedule();
    }
    
    public ProjectTOS getProjectTOS( IProject project, boolean initialize ) throws MissingNatureException{
        ProjectTOS p = projects.get(project.getName());

        if( p == null ){
        	try{
	        	if( !project.hasNature( TinyOSCore.OLD_NATURE_ID ) && !project.hasNature( TinyOSCore.NATURE_ID ) ){
	        		throw new MissingNatureException( project, "Not a tinyos project" );
	        	}
        	}
        	catch( CoreException ex ){
        		TinyOSPlugin.log( ex );
        	}
        	
            p = new ProjectTOS( this );
            projects.put(project.getName(),p);
            p.init( project, initialize );
        }
        
        if (!project.equals(lastProject)) {
            lastProject = project;
            notifyLastAccessedListeners();
        } 
        
        return p;
    }
    
    public IProject getlastProject() {
        return lastProject;
    }
    
    public NesCPath getPaths( IProject project ){
    	NesCPath path = paths.get( project );
    	if( path == null ){
    		synchronized( paths ){
    			path = paths.get( project );
    			if( path == null ){
    				path = new NesCPath( project );
    				paths.put( project, path );
    			}
			}
    	}
    	return path;
    }

    public void addProjectChangeListener( IProjectLastAccessedChangeListener iplacl) {
        lastAccessedListeners.add(iplacl);
    }

    public void removeProjectLastChangeListener( IProjectLastAccessedChangeListener iplacl) {
        lastAccessedListeners.remove(iplacl);
    }
    
    protected void notifyLastAccessedListeners() {
        Object[] list = lastAccessedListeners.getListeners();
        for (int i = 0; i < list.length; i++) {
            ((IProjectLastAccessedChangeListener)list[i]).projectChanged(lastProject);
        }
    }

    public interface IProjectLastAccessedChangeListener {
        void projectChanged(IProject p);
    }
    
    private class ReinitializeJob extends Job{
        private Set<IEnvironment> environments = new HashSet<IEnvironment>();
        private Set<IPlatform> platforms = new HashSet<IPlatform>();
        
        public ReinitializeJob(){
            super( "Reinitialize" );
            setPriority( BUILD );
            setSystem( true );
        }
        
        public void add( IEnvironment environment ){
            synchronized( environments ){
                environments.add( environment );
            }
            schedule( 500 );
        }
        
        public void add( IPlatform platform ){
        	synchronized ( platforms ) {
				platforms.add( platform );
			}
        	schedule( 500 );
        }
        
        @Override
        protected IStatus run( IProgressMonitor monitor ) {
            monitor.beginTask( "Reinitialize Projects", projects.size() );
            
            Set<IEnvironment> environments = new HashSet<IEnvironment>();
            Set<IPlatform> platforms = new HashSet<IPlatform>();
            
            synchronized( this.environments ){
                environments.addAll( this.environments );
                this.environments.clear();
            }
            
            synchronized( this.platforms ){
            	platforms.addAll( this.platforms );
            	this.platforms.clear();
            }
            
            for( ProjectTOS project : projects.values() ){
                if( project.getProject().isAccessible() && project.isInitializeable() ){
                	if( environments.contains( project.getEnvironment() )){
                        project.initialize();
                    }
                    else{
                    	MakeTarget target = project.getMakeTarget();
                    	if( target != null ){
                    		if( platforms.contains( target.getPlatform() )){
                    			project.initialize();
                    		}
                    	}
                    }
                }
                monitor.worked( 1 );
            }
            
            monitor.done();
            return Status.OK_STATUS;
        }
    }
}
