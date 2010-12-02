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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
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
import tinyos.yeti.ep.parser.IMessage;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.jobs.PublicJob;
import tinyos.yeti.jobs.ResourceJob;
import tinyos.yeti.marker.ProblemMarkerSupport;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.FileMultiReader;

/**
 * @deprecated replaced by {@link TinyOSProjectBuilder2}
 */
@Deprecated
public class TinyOsProjectBuilder extends IncrementalProjectBuilder {
    private Job collectMessagesJob;

    public TinyOsProjectBuilder() {
        super();
    }

    @Override
    protected void clean( IProgressMonitor monitor ){
        Debug.enter();
        try{
	        ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( getProject() );
	
	        if( tos.getModel().secureThread() ){
	            tos.clearCache( false, monitor );
	            try{
	                getProject().accept(new CleanVisitor());
	            }
	            catch ( CoreException e ){
	                e.printStackTrace();
	            }
	        }
	        else{
	            PublicJob job = new PublicJob( "clean" ){
	                @Override
	                public IStatus run( IProgressMonitor monitor ){
	                    clean( monitor );
	                    return Status.OK_STATUS;
	                }
	            };
	
	            job.setSystem( true );
	            job.setPriority( Job.SHORT );
	            tos.getModel().runJob( job, monitor );
	        }
        }
        catch( MissingNatureException ex ){
        	TinyOSPlugin.log( ex );
        }
        Debug.leave();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
    	try{
	        ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( getProject() );
	        if( tos.getModel().isInitialized() ){
	            if( monitor == null )
	                monitor = new NullProgressMonitor();
	
	            if( kind == IncrementalProjectBuilder.FULL_BUILD ){
	                fullBuild(monitor);
	            }
	            else {
	                IResourceDelta delta = getDelta(getProject());
	                if (delta == null) {
	                    fullBuild(monitor);
	                }
	                else {
	                    incrementalBuild(monitor,delta);
	                }
	            }
	        }
    	}
    	catch( MissingNatureException ex ){
    		TinyOSPlugin.log( ex );
    	}
        return null;
    }

    protected void fullBuild(final IProgressMonitor monitor) throws MissingNatureException{
        Debug.enter();

        ProjectTOS project = TinyOSPlugin.getDefault().getProjectTOS( getProject() );

        if( project.getModel().secureThread() ){
            Job job = new Job( "Build full: " + project.getProject().getName() ){
                @Override
                protected IStatus run( IProgressMonitor monitor ){
                	try{
                		fullBuild( monitor );
                		return Status.OK_STATUS;
                	}
                	catch( MissingNatureException ex ){
                		TinyOSPlugin.log( ex );
                		return Status.CANCEL_STATUS;
                	}
                }
            };
            job.setPriority( Job.BUILD );
            job.schedule();
        }
        else{
            try{
                ProjectResourceCollector resources = new ProjectResourceCollector();
                project.getProject().accept( resources );

                int size = resources.resources.size();
                int count = 0;

                monitor.beginTask( "Build", size );

                for( IResource resource : resources.resources ){
                    if( monitor.isCanceled() || !project.getModel().isInitialized()){
                        monitor.done();
                        project.getModel().informContinuousFinished();
                        return;
                    }

                    monitor.subTask( (++count) + "/" + size + " " + resource.getName() );
                    collectDeclarations( project, resource );
                    monitor.worked( 1 );
                }

                project.getModel().informContinuousFinished();
                
                collectMessages( project, null );
            }
            catch( CoreException ex ){
                ex.printStackTrace();
            }
        }
        monitor.done();
        Debug.leave();
    }

    private void incrementalBuild( IProgressMonitor monitor, IResourceDelta delta ) throws MissingNatureException{
        Debug.enter();

        DeltaResourcCollector collector = new DeltaResourcCollector();
        try {
            delta.accept( collector );
        }
        catch (CoreException e) {
            e.printStackTrace();
        }

        final ProjectTOS project = TinyOSPlugin.getDefault().getProjectTOS( getProject() );

        int count = 0;
        int size = collector.changed.size();

        monitor.beginTask( "Build", size );

        IParseFile[] files = new IParseFile[ size ];
        IParseFile[] allFiles = new IParseFile[ size + collector.removed.size() ];
        
        int index = 0;
        int allIndex = 0;
        
        ProjectModel model = project.getModel();
        
        for( File file : collector.removed ){
            IParseFile parseFile = model.parseFile( file );
            // TODO should not be a NullProgressMonitor
            project.getModel().clearCache( parseFile, true, new NullProgressMonitor() );
            allFiles[ allIndex++ ] = parseFile;
        }
        
        for( File file : collector.changed ){
            files[ index ] = model.parseFile( file );
            allFiles[ allIndex++ ] = files[ index ];

            index++;
        }

        for( File file : collector.changed ){
            if( !project.getModel().isInitialized() || monitor.isCanceled() ){
                monitor.done();
                project.getModel().informContinuousFinished();
                return;
            }

            monitor.subTask( (++count) + "/" + size + " " + file.getName() );
            collectDeclarations( project, file );
            monitor.worked( 1 );
        }
        
        project.getModel().informContinuousFinished();

        collectMessages( project, allFiles );

        monitor.done();
        Debug.leave();
    }

    /**
     * Collects all declarations of the file <code>resource</code>. This method
     * may start a new job to collect the declarations and will wait until the job
     * is executed.
     * @param project the project to which <code>resource</code> belongs
     * @param resource the resource to check
     */
    private void collectDeclarations( final ProjectTOS project, IResource resource ){
        File file = toFile( resource );
        if( file != null )
            collectDeclarations( project, file );
    }

    private void collectDeclarations( final ProjectTOS project, final File file ){
        Debug.enter();
        Debug.info( file.getName() );

        if( project.getModel().secureThread() ){
            Debug.info( file );
            try {
                project.getModel().update( file, true, null );
            }
            catch( IOException e ) {
                e.printStackTrace();
            }
        }
        else{
            PublicJob job = new PublicJob( "Build " + file.getName() ){
                @Override
                public IStatus run( IProgressMonitor monitor ){
                    Debug.enter();
                    Debug.info( file );
                    if( file.exists() && file.canRead() ){
                        try {
                            project.getModel().update( file, true, null );
                        }
                        catch( IOException e ) {
                            e.printStackTrace();
                        }
                    }
                    Debug.leave();
                    return Status.OK_STATUS;
                }
            };

            job.setPriority( Job.BUILD );
            job.setSystem( true );
            project.getModel().runJob( job, null );
        }

        Debug.leave();
    }

    /**
     * Collects all messages of the files of <code>project</code>. This method
     * runs asynchronous and will return immediately.
     * @param project the project whose messages should be updated
     * @param changes a list of files that were changed, <code>null</code> if all
     * files need to be updated
     */
    private void collectMessages( final ProjectTOS project, final IParseFile[] changes ){
        Debug.enter();

        synchronized( this ){
            if( collectMessagesJob != null ){
                collectMessagesJob.cancel();
                collectMessagesJob = null;
            }
        }
        collectMessagesJob = new Job( "Check for errors and warnings: " + project.getProject().getName() ){
            @Override
            protected IStatus run( IProgressMonitor monitor ){
                try{
                    ProjectResourceCollector resources = new ProjectResourceCollector();
                    project.getProject().accept( resources );

                    int count = 0;
                    int size = resources.resources.size();
                    monitor.beginTask( "Parse and check", size );

                    for( IResource resource : resources.resources ){
                        monitor.subTask( (++count) + "/" + size + " " + resource.getName() );
                        
                        if( !project.getModel().isInitialized() )
                            cancel();
                        
                        if( monitor.isCanceled() ){
                            monitor.done();
                            return Status.CANCEL_STATUS;
                        }

                        collectMessages( project, resource, changes );
                        monitor.worked( 1 );
                    }
                }
                catch ( CoreException e ){
                    e.printStackTrace();
                }

                synchronized( TinyOsProjectBuilder.this ){
                    collectMessagesJob = null;
                }

                monitor.done();
                return Status.OK_STATUS;
            }
        };

        collectMessagesJob.setPriority( Job.BUILD );
        collectMessagesJob.schedule();

        Debug.leave();
    }

    /**
     * Collects all messages from <code>resource</code>. This method starts
     * a new job and waits until the job is finished to perform its work.
     * @param project the owner of <code>resource</code>
     * @param resource the resource to check
     * @param changes the files that were changed, can be <code>null</code> to indicate
     * that all files have changed
     */
    private void collectMessages( final ProjectTOS project, IResource resource, final IParseFile[] changes ) {
        Debug.enter();

        ResourceJob job = new ResourceJob( "Check " + resource.getName(), resource ){
            @Override
            public IStatus run( IProgressMonitor monitor ){
                final File file = toFile( getResouce() );
                Debug.info( file );
                if( file != null ){
                    try{
                        monitor.beginTask( "Check", 2000 );
                        
                        boolean update = false;
                        IParseFile parseFile = project.getModel().parseFile( file );

                        if( changes != null ){
                            ProjectModel model = project.getModel();
                            for( IParseFile change : changes ){
                                if( model.isIncluded( parseFile, change )){
                                    update = true;
                                    break;
                                }
                            }
                        }
                        else{
                            update = true;
                        }

                        if( update ){
                            INesCParser parser = project.newParser( parseFile, null, new SubProgressMonitor( monitor, 1000 ) );
                            if( monitor.isCanceled() ){
                                monitor.done();
                                return Status.CANCEL_STATUS;
                            }
                            parser.setCreateMessages( true );
                            parser.setFollowIncludes( true );
                            parser.parse( new FileMultiReader( file ), new SubProgressMonitor( monitor, 1000 ) );
                            if( monitor.isCanceled() ){
                                monitor.done();
                                return Status.CANCEL_STATUS;
                            }

                            IMessage[] messages = parser.getMessages();
                            if( messages != null ){
                                ProblemMarkerSupport.synchronizeMessages( getResouce(), parseFile, messages );
                            }
                        }
                    }
                    catch( IOException ex ){
                        ex.printStackTrace();
                    }
                    finally{
                        monitor.done();
                    }
                }
                
                monitor.done();
                return Status.OK_STATUS;
            }
        };

        job.setPriority( Job.BUILD );
        job.setSystem( true );
        project.getModel().runJob( job, null );

        Debug.leave();
    }

    private File toFile( IResource resource ){
        File file = toInexistantFile( resource );
        if( file != null && file.exists() && file.isFile() && file.canRead() ){
            return file;
        }

        return null;
    }
    
    private File toInexistantFile( IResource resource ){
        if( resource == null )
            return null;

        String ext = resource.getFileExtension();
        if( "nc".equals( ext ) || "h".equals( ext )){
            IPath path = resource.getLocation();
            if( path == null )
                return null;

            return path.toFile();
        }

        return null;        
    }

    private static class CleanVisitor implements IResourceVisitor {
        public boolean visit(IResource res) {
            try {
                res.deleteMarkers(IMarker.PROBLEM,true,IResource.DEPTH_INFINITE);
            }
            catch (CoreException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    private static class ProjectResourceCollector implements IResourceVisitor{
        public List<IResource> resources = new ArrayList<IResource>();

        public boolean visit( IResource resource ) throws CoreException {
            String ext = resource.getFileExtension();
            if( "nc".equals( ext ) || "h".equals( ext )){
                resources.add( resource );
            }

            return true;
        }
    }

    private class DeltaResourcCollector implements IResourceDeltaVisitor {
        public List<File> changed = new ArrayList<File>();
        public List<File> removed = new ArrayList<File>();

        public boolean visit(IResourceDelta delta) throws CoreException {
            switch( delta.getKind() ){
                case IResourceDelta.ADDED:
                case IResourceDelta.ADDED_PHANTOM:
                case IResourceDelta.CHANGED:
                case IResourceDelta.REPLACED:
                    File file = toFile( delta.getResource() );
                    if( file != null ){
                        changed.add( file );
                    }
                    break;
                case IResourceDelta.REMOVED:
                case IResourceDelta.REMOVED_PHANTOM:
                    file = toInexistantFile( delta.getResource() );
                    if( file != null ){
                        removed.add( file );
                    }
                    break;
            }

            return true;
        }
    }

    /*
    private class MarkVisitor implements IResourceVisitor {
        private ProjectTOS project;

        public MarkVisitor( ProjectTOS project ){
            this.project = project;
        }

        public boolean visit(IResource res) {
            collectDeclarations( project, res );
            return true;
        }
    }

    private class MessageVisitor implements IResourceVisitor{
        private ProjectTOS project;

        public MessageVisitor( ProjectTOS project ){
            this.project = project;
        }

        public boolean visit( IResource resource ){
            collectMessages( project, resource );
            return true;
        }
    }*/
}