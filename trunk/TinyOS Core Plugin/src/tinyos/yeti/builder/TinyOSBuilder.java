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
package tinyos.yeti.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.Debug;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.jobs.ProgressMonitorCheckingClose;
import tinyos.yeti.jobs.ResourceJob;
import tinyos.yeti.model.IProjectModelListener;
import tinyos.yeti.model.ProjectModel;

/**
 * The builder for a {@link ProjectTOS}. Each builder knows which files to 
 * build and can be interrupted when additional files are to be built.
 * @author Benjamin Sigg
 */
public class TinyOSBuilder{
    private final int MAX_REBUILD_CYCLES = 5;
    
    /** The set of resources that are unchanged but need to be built */
    private Set<IResource> build;
    /** The set of resources that were changed */
    private Set<IResource> changed;
    /** The set of resources which seem to be removed (note: the resources might already be added again) */
    private Set<IResource> removed;
    
    /** whether this builder will do a full build */
    private boolean requireFullBuild = false;
    
    /** the main source of information for this builder */
    private ProjectModel model;
    
    /** the job that is executing the build, might be <code>null</code> */
    private volatile Job buildJob;
    
    public TinyOSBuilder( ProjectModel model ){
        this.model = model;
        
        model.addProjectModelListener( new IProjectModelListener(){
            public void changed( IParseFile parseFile, boolean continuous ){
                // ignore
            }
            public void changed( IParseFile[] parseFiles ){
                // ignore
            }
            public void initialized(){
                doCheckBuild( 0 );
            }
        });
    }
    
    /**
     * Starts a full build of all resources.
     */
    public void doFullBuild(){
        ProjectResourceCollector collector = new ProjectResourceCollector();
        try{
            model.getProject().acceptSourceFiles( collector );
        }
        catch ( CoreException e ){
            TinyOSPlugin.warning( e.getStatus() );
        }
        
        for( IResource resource : collector.resources ){
            model.buildMark( resource, true );
        }
        
        Job startFullBuilder = new CancelingJob( "Start full build" ){
            @Override
            public IStatus run( IProgressMonitor monitor ){
                monitor.beginTask( "Start", 1 );
                cancelBuild( true );
                
                synchronized( TinyOSBuilder.this ){
                    requireFullBuild = true;
                    changed = null;
                    removed = null;
                    build = null;
                }
                
                build( 0 );
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        startFullBuilder.setPriority( Job.SHORT );
        startFullBuilder.schedule();
    }
    
    /**
     * Starts an automatic build of <code>resources</code>. An automatic build
     * can never trigger another build. An automatic build is normally triggered
     * by a manual build after checking the projects consistency.
     * @param build the resources that just need to be built
     * @param changed the resources which are changed and need to be built
     * @param autoTriggered the number of the current sweep
     */
    public void doAutoBuild( List<IResource> build, List<IResource> changed, int autoTriggered ){
        doBuild( build, changed, null, autoTriggered );
    }
    
    /**
     * Starts an incremental build.
     * @param changed the resources which seem to be changed
     * @param removed the resources which seem to be removed
     */
    public void doIncrementalBuild( final List<IResource> changed, final List<IResource> removed ){
        doBuild( null, changed, removed, 0 );
    }
    
    /**
     * Marks <code>resource</code> as not built if the model is not yet
     * initialized and hence will build the resource when the model is finished
     * initializing.
     * @param resource the resource to build after initialization
     */
    public void doBuildAfterInitialization( IResource resource ){
        if( !model.isInitialized() ){
            model.buildMark( resource, false );
        }
    }
    
    /**
     * Marks the resource <code>resource</code> as needed built.
     * @param resource the resource to build
     * @param changed whether the resource has changed or not
     */
    public void doBuild( IResource resource, boolean changed ){
        List<IResource> list = new ArrayList<IResource>( 1 );
        list.add( resource );
        
        if( changed ){
            doBuild( list, null, null, 0 );
        }
        else{
            doBuild( null, list, null, 0 );
        }
    }
    
    private void doBuild( final List<IResource> build, final List<IResource> changed, final List<IResource> removed, final int autoTriggered ){
        if( build != null ){
            for( IResource resource : build ){
                model.buildMark( resource, false );
            }
        }
        if( changed != null ){
            for( IResource resource : changed ){
                model.buildMark( resource, true );
            }
        }
        
        Job startBuild = new CancelingJob( "Start incremental build" ){
            @Override
            public IStatus run( IProgressMonitor monitor ){
                monitor.beginTask( "Start", 1 );
                cancelBuild( true );
                
                synchronized( TinyOSBuilder.this ){
                    if( !requireFullBuild ){
                        if( TinyOSBuilder.this.changed == null )
                            TinyOSBuilder.this.changed = new HashSet<IResource>();

                        if( changed != null ){
                            addAllValid( changed, TinyOSBuilder.this.changed );
                        }

                        if( TinyOSBuilder.this.removed == null )
                            TinyOSBuilder.this.removed = new HashSet<IResource>();

                        if( removed != null ){
                            addAllValid( removed, TinyOSBuilder.this.removed);
                        }
                        
                        if( TinyOSBuilder.this.build == null )
                            TinyOSBuilder.this.build = new HashSet<IResource>();
                        
                        if( build != null ){
                            addAllValid( build, TinyOSBuilder.this.build );
                        }
                    }
                }
                
                build( autoTriggered );
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        startBuild.setPriority( Job.SHORT );
        startBuild.setSystem( true );
        startBuild.schedule();
    }
    
    private void addAllValid( List<IResource> source, Set<IResource> destination ){
    	IContainer[] folders = model.getProject().getSourceContainers();
    	IPath[] folderPaths = new IPath[ folders.length ];
    	for( int i = 0; i < folders.length; i++ ){
    		folderPaths[i] = folders[i].getFullPath();
    	}
	        
        for( IResource resource : source ){
            IPath resourcePath = resource.getFullPath();
            
            for( IPath folderPath : folderPaths ){
            	if( folderPath.isPrefixOf( resourcePath )){
            		destination.add( resource );
            		break;
            	}
            }
        }
    }
    
    public void doCheckBuild( int autoTriggered ){
        cancelBuild( true );
        buildJob = new BuildCheckJob( model.getProject(), autoTriggered ){
            @Override
            public IStatus run( IProgressMonitor monitor ){
                if( this != buildJob ){
                    return Status.CANCEL_STATUS;
                }
                
                IStatus status = super.run( monitor );
                
                synchronized( TinyOSBuilder.this ){
                    if( buildJob == this )
                        buildJob = null;
                }
                
                return status;
            }
        };
        buildJob.setPriority( Job.BUILD );
        buildJob.schedule();
    }
    
    /**
     * Cancels the current build job
     * @param wait if set, then this method waits until the job is canceled
     */
    public void cancelBuild( boolean wait ){
        Job buildJob = this.buildJob;
        
        if( buildJob != null ){
            Debug.info( "cancel build job" );
            
            buildJob.cancel();
            
            if( wait ){
                while( buildJob.getState() != Job.NONE ){
                    try{
                        buildJob.join();
                    }
                    catch ( InterruptedException e ){
                        // ignore this kind of exception
                    }
                }
            }
        }
    }
    
    /**
     * Starts a new build job if, and only if, the model is initialized.
     * @param autoTriggered whether the job is triggered automatically
     */
    private void build( final int autoTriggered ){
        if( model.isInitialized() && !model.getProject().isOnStop() ){
            Debug.info( "start build job" );
            synchronized( this ){
                buildJob = new Job( "Build '" + model.getProject().getProject().getName() + "'" ){
                    @Override
                    protected IStatus run( IProgressMonitor monitor ){
                        build( this, autoTriggered, monitor );
                        return Status.OK_STATUS;
                    }
                };

                buildJob.setPriority( Job.BUILD );
                buildJob.schedule();
            }
        }
    }
    
    private void build( Job current, int autoTriggered, IProgressMonitor monitor ){
        if( requireFullBuild ){
            fullBuild( current, monitor );
        }
        else if( changed != null && removed != null && build != null ){
            List<IResource> buildResources;
            List<IResource> changedResources;
            List<IResource> removedResources;
            
            synchronized( this ){
                buildResources = new ArrayList<IResource>( build.size() );
                changedResources = new ArrayList<IResource>( changed.size() );
                removedResources = new ArrayList<IResource>( removed.size() );
                
                for( IResource resource : build ){
                    if( resource.exists() )
                        buildResources.add( resource );
                }
                
                for( IResource resource : changed ){
                    if( resource.exists() )
                        changedResources.add( resource );
                }
                for( IResource resource : removed ){
                    if( !resource.exists() )
                        removedResources.add( resource );
                }
            }
            
            monitor.beginTask( "Build", 1000 );
            
            deleteCache( buildResources, changedResources, removedResources, new SubProgressMonitor( monitor, 50 ) );
            build( current, autoTriggered, buildResources, changedResources, removedResources, new SubProgressMonitor( monitor, 950 ) );
            
            monitor.done();
        }
    }
    

    public void fullBuild( Job current, IProgressMonitor monitor ){
    	if( monitor == null )
    		monitor = new NullProgressMonitor();
    	
    	monitor.beginTask( "Build", 2 );
    	
        try{
        	ProjectResourceCollector resources = new ProjectResourceCollector();
            ProjectTOS project = model.getProject();
            project.acceptSourceFiles( resources );
            final ProjectModel model = project.getModel();
            if( model.secureThread() ){
                model.clearCache( false, new SubProgressMonitor( monitor, 1 ) );
            }
            else{
                CancelingJob job = new CancelingJob( "clear cache" ){
                    @Override
                    public IStatus run( IProgressMonitor monitor ) {
                        model.clearCache( false, null );
                        return Status.OK_STATUS;
                    }
                };
                job.setSystem( true );
                job.setPriority( Job.BUILD );
                model.runJob( job, new SubProgressMonitor( monitor, 1 ) );
            }

            build( current, 0, null, resources.resources, null, new SubProgressMonitor( monitor, 1 ) );
        }
        catch( CoreException ex ){
            TinyOSPlugin.warning( ex.getStatus() );
        }

        monitor.done();
    }

    /**
     * Deletes all caches that contain resources of <code>changed</code>
     * or <code>removed</code>.
     * @param build resources to build
     * @param changed the changed resources
     * @param removed the removed resources
     */
    private void deleteCache( final Collection<IResource> build, final Collection<IResource> changed, final Collection<IResource> removed, IProgressMonitor monitor ){
        if( model.secureThread() ){
            monitor.beginTask( "Delete", removed.size() + changed.size() );
            
            for( IResource resource : removed ){
                model.clearCache( model.parseFile( resource ), true, new SubProgressMonitor( monitor, 1 ) );
                if( monitor.isCanceled() ){
                    monitor.done();
                    return;
                }
            }

            for( IResource resource : changed ){
                model.clearCache( model.parseFile( resource ), true, new SubProgressMonitor( monitor, 1 ) );
                if( monitor.isCanceled() ){
                    monitor.done();
                    return;
                }
            }
            
            for( IResource resource : build ){
                model.clearCache( model.parseFile( resource ), true, new SubProgressMonitor( monitor, 1 ) );
                if( monitor.isCanceled() ){
                    monitor.done();
                    return;
                }
            }
            
            monitor.done();
        }
        else{
            CancelingJob job = new CancelingJob( "clear cache" ){
                @Override
                public IStatus run( IProgressMonitor monitor ) {
                    deleteCache( build, changed, removed, monitor );
                    return Status.OK_STATUS;
                }
            };
            job.setSystem( true );
            job.setPriority( Job.BUILD );
            model.runJob( job, monitor );
        }
    }

    /**
     * The main building operation.
     * @param current the build job which called this method, must equal
     * {@link #buildJob}, otherwise this operation cancels itself.
     * @param autoTriggered whether this job was triggered automatically or not
     * @param unchangedButToBuild the files that are unchanged but need to be built
     * @param changedAndToBuild the files that should be built anew
     * @param changedButNotToBuild the files that have changed but do not 
     * to be built again.
     * @param monitor for interaction 
     */
    private void build( Job current, final int autoTriggered, final Collection<IResource> unchangedButToBuild, final Collection<IResource> changedAndToBuild, final Collection<IResource> changedButNotToBuild, IProgressMonitor monitor ){           
        if( model.secureThread() ){
            monitor.beginTask( "Start asynchronous build", 1 );
            buildJob = new CancelingJob( "Build" ){
                @Override
                public IStatus run(IProgressMonitor monitor) {
                    build( this, autoTriggered, unchangedButToBuild, changedAndToBuild, changedButNotToBuild, monitor );
                    return Status.OK_STATUS;
                }
            };
            buildJob.setPriority( Job.BUILD );
            buildJob.schedule();
            monitor.done();
            return;
        }
        
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor = new ProgressMonitorCheckingClose( monitor );
        
        synchronized( this ){
            // make sure that the current job is the right one
            if( buildJob == null ){
                buildJob = current;
            }
            else if( buildJob != current ){
                monitor.done();
                return;
            }
        }

        // find the files which must be checked again
        Set<IResource> checkFiles = new HashSet<IResource>();
        if( changedAndToBuild != null )
            checkFiles.addAll( changedAndToBuild );
        
        if( unchangedButToBuild != null )
            checkFiles.addAll( unchangedButToBuild );
        
        if( changedButNotToBuild != null ){
            ProjectResourceCollector collector = new ProjectResourceCollector();
            try {
                model.getProject().acceptSourceFiles( collector );

                List<IParseFile> changedNames = new ArrayList<IParseFile>();
                for( IResource resource : changedAndToBuild ){
                    changedNames.add( model.parseFile( resource ));
                }
                for( IResource resource : changedButNotToBuild ){
                    changedNames.add( model.parseFile( resource ));
                }

                for( IResource resource : collector.resources ){
                    IParseFile name = model.parseFile( resource );
                    for( IParseFile changed : changedNames ){
                        if( model.isIncluded( name, changed )){
                            checkFiles.add( resource );
                            break;
                        }
                    }
                }
            }
            catch( CoreException e ) {
                TinyOSPlugin.warning( e.getStatus() );
            }
        }

        // store the files that need checking as changed. If the builder
        // is canceled, then these files get not lost 
        synchronized( TinyOSBuilder.this ){
            if( !requireFullBuild ){
                if( build == null )
                    build = new HashSet<IResource>( checkFiles );
                else
                    build.addAll( checkFiles );
            }
        }
        
        if( monitor.isCanceled() ){
            monitor.done();
            return;
        }
        
        // start the "real" work
        int filesSize = checkFiles.size();
        int prepareCount = 0;
        int buildCount = 0;
        monitor.beginTask( "Build", 8 * filesSize + 1 );

        BuildPrepareJob prepare = new BuildPrepareJob( model.getProject() );
        model.runJob( prepare, new SubProgressMonitor( monitor, 1 ) );

        for( IResource file : checkFiles ){
            if( monitor.isCanceled() || !model.isInitialized() || current != buildJob ){
                monitor.done();
                model.informContinuousFinished();
                return;
            }

            monitor.subTask( "(" + (++prepareCount) + "/" + filesSize + ", " + (buildCount) + "/" + filesSize + ")[prepare] '" + file.getName() + "'" );
            BuildInitJob job = new BuildInitJob( file, model.getProject() );
            model.runJob( job, new SubProgressMonitor( monitor, 1 ) );
            monitor.worked( 1 );
        }

        
        for( IResource file : checkFiles ){
            if( monitor.isCanceled() || !model.isInitialized() || current != buildJob ){
                monitor.done();
                model.informContinuousFinished();
                return;
            }

            monitor.subTask( "(" + (prepareCount) + "/" + filesSize + ", " + (++buildCount) + "/" + filesSize + ")[build] '" + file.getName() + "'" );
            BuildUpdateJob job = new BuildUpdateJob(
                    file,
                    model.getProject(),
                    new SubProgressMonitor( monitor, 5, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL ){
                        @Override
                        public void setTaskName( String name ){
                            // ignore new labels
                            // TODO is there any need to override more methods?
                        }
                    });
            model.runJob( job, new SubProgressMonitor( monitor, 1 ) );
        }

        synchronized( this ){
            requireFullBuild = false;
            changed = null;
            removed = null;
            
            if( buildJob == current ){
                buildJob = null;
            }
        }
        
        model.informContinuousFinished();
        monitor.done();
        
        if( !monitor.isCanceled() ){
            if( autoTriggered < MAX_REBUILD_CYCLES ){
                doCheckBuild( autoTriggered+1 );
            }
        }
    }
    
    /**
     * Prepares the {@link ProjectModel} for the next build.
     */
    private static class BuildPrepareJob extends CancelingJob{
        private ProjectTOS project;
        
        public BuildPrepareJob( ProjectTOS project ){
            super( "prepare" );
            this.project = project;
            setPriority( Job.BUILD );
            setSystem( true );
        }

        @Override
        public IStatus run( IProgressMonitor monitor ){
            project.getModel().updateFileModel( monitor );
            return Status.OK_STATUS;
        }
    }

    /**
     * Initializes one resource, meaning: collects the top level declarations
     * of that resource.
     */
    private static class BuildInitJob extends ResourceJob{
        private ProjectTOS project;

        public BuildInitJob( IResource resource, ProjectTOS project ) {
            super( "init", resource );
            this.project = project;
            setPriority( Job.BUILD );
            setSystem( true );
        }

        @Override
        public IStatus run( IProgressMonitor monitor ) {
            try {
                monitor = new ProgressMonitorCheckingClose( monitor );
                project.getModel().buildInit( getResouce(), monitor );
            }
            catch( IOException e ) {
                TinyOSPlugin.warning( e );
            }
            return Status.OK_STATUS;
        }
    }

    /**
     * Updates a resource, meaning: completely parses the resource again.
     */
    private class BuildUpdateJob extends ResourceJob{
        private ProjectTOS project;
        private IProgressMonitor baseMonitor;
        
        public BuildUpdateJob( IResource resource, ProjectTOS project, IProgressMonitor baseMonitor ) {
            super( "update", resource );
            this.project = project;
            setPriority( Job.BUILD );
            setSystem( true );
            this.baseMonitor = baseMonitor;
        }

        @Override
        public IStatus run( IProgressMonitor monitor ) {
            monitor.beginTask( "update", IProgressMonitor.UNKNOWN );
            try {
                IResource resource = getResouce();
                monitor = new ProgressMonitorCheckingClose( monitor );
                project.getModel().buildUpdate( resource, baseMonitor );
                
                synchronized( TinyOSBuilder.this ){
                    if( changed != null )
                        changed.remove( resource );
                    
                    if( removed != null )
                        removed.remove( resource );
                    
                    if( build != null )
                        build.remove( resource );
                }
            }
            catch( IOException e ) {
                TinyOSPlugin.warning( e );
            }
            monitor.done();
            return Status.OK_STATUS;
        }
    }
}
