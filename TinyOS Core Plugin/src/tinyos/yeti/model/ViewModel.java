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

import static tinyos.yeti.ep.parser.ASTNodeFilterFactory.not;
import static tinyos.yeti.ep.parser.ASTNodeFilterFactory.or;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.Debug;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ProjectManager.IProjectLastAccessedChangeListener;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.ASTView;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelConnectionFilter;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.INesCParserFactory;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.jobs.AllReachableFilesJob;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.jobs.MakeTargetJob;
import tinyos.yeti.jobs.PublicJob;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.views.InterfacesView;

/**
 * A view model allows access to all the {@link IASTModelNode}s
 * that are within a project and its libraries.
 * @author Benjamin Sigg
 */
public class ViewModel implements IProjectLastAccessedChangeListener, IProjectModelListener{
    private IASTModel model;

    private ProjectTOS currentProject;

    private InterfacesView view;

    private ParseAllJob parseAllJob;
    private UpdateJob updateJob;

    /** filter used to remove nodes from the cache to prevent memory shortage */
    private IASTModelNodeFilter cacheFilter;
    
    private IASTModelConnectionFilter includeFilter = new IASTModelConnectionFilter(){
        public boolean include( IASTModelNode parent, IASTModelNodeConnection connection ){
            return !connection.isReference() && connection.getTags().contains( Tag.INCLUDED );
        }
    };
    
    public ViewModel( InterfacesView view ){
        this.view = view;
        TinyOSPlugin.getDefault().getProjectManager().addProjectChangeListener( this );
        build( TinyOSPlugin.getDefault().getProjectManager().getlastProject() );

        List<IASTModelNodeFilter> filters = new ArrayList<IASTModelNodeFilter>();

        ASTView[] views = TinyOSPlugin.getDefault().getParserFactory().getViews();
        for( ASTView check : views ){
            IASTModelNodeFilter filter = check.getFilter();
            if( filter != null ){
                filters.add( filter );
            }
        }
        
        IASTModelNodeFilter includeFilter = new IASTModelNodeFilter(){
            public boolean include( IASTModelNode node ){
                return node.getTags().contains( Tag.INCLUDED );
            }
        };
        
        cacheFilter = or( includeFilter, not( or( filters.toArray( new IASTModelNodeFilter[ filters.size() ] ))));
    }

    /**
     * Gets the current {@link IASTModel}.
     * @return the current model
     */
    public IASTModel getASTModel(){
        return model;
    }

    public void projectChanged( IProject project ){
        build( project );
    }

    public void dispose() {
        TinyOSPlugin.getDefault().getProjectManager().removeProjectLastChangeListener(this);
        if( currentProject != null ){
            currentProject.getModel().removeProjectModelListener( this );
            currentProject = null;
        }
    }
    
    public void changed( IParseFile parseFile, boolean continuous ){
        if( !continuous ){
            changed( new IParseFile[]{ parseFile } );
        }
    }
    
    public void changed( IParseFile[] parseFiles ){
        startUpdateJob( parseFiles );
    }
    
    public void initialized(){
        startParseAllJob();
    }

    /**
     * Cleans this model and builds it anew using the files from <code>project</code>.
     * @param project the project to build upon
     */
    public void build( IProject project ){
        if( currentProject != null ){
            currentProject.getModel().removeProjectModelListener( this );
        }

        currentProject = null;

        if( project != null ){
        	try{
	            currentProject = TinyOSPlugin.getDefault().getProjectTOS( project );
	            currentProject.getModel().addProjectModelListener( this );
	            startParseAllJob();
        	}
        	catch( MissingNatureException ex ){
        		// silent
        	}
        }
        else{
            model = null;
            view.refresh( null );
        }
    }

    public ProjectTOS getProjectTOS(){
        return currentProject;
    }

    protected synchronized void startUpdateJob( IParseFile[] files ){
        if( currentProject.getModel().isInitialized() ){
            if( updateJob == null ){
                updateJob = new UpdateJob( currentProject, files );
                if( parseAllJob == null )
                    updateJob.schedule();
            }
            else{
                updateJob.restart( files );
            }
        }
    }
    
    protected synchronized void startParseAllJob(){
        stopParsing();

        if( currentProject == null || currentProject.getModel().isInitialized() ){
            parseAllJob = new ParseAllJob( currentProject );
            parseAllJob.schedule();   
        }
    }

    public synchronized void stopParsing(){
        if( parseAllJob != null ){
            parseAllJob.cancel();
        }
    }

    /**
     * This job parses a subset of all existing files and hence can update
     * this model if only small changes are reported
     * @author Benjamin Sigg
     *
     */
    private class UpdateJob extends CancelingJob{
        private ProjectTOS currentProject;
        private Set<IParseFile> changed;
        
        public UpdateJob( ProjectTOS currentProject, IParseFile[] changedFiles ){
            this( currentProject, changedFiles, new HashSet<IParseFile>() );
        }
        
        public UpdateJob( ProjectTOS currentProject, IParseFile[] changedFiles, Set<IParseFile> changed ){
            super( "Analyze files: " + currentProject.getProject().getName() );
            setPriority(Job.DECORATE);
            this.currentProject = currentProject;
            this.changed = changed;
            for( IParseFile file : changedFiles )
                changed.add( file );
        }
        
        public void restart( IParseFile[] changedFiles ){
            cancel();
            updateJob = new UpdateJob( currentProject, changedFiles, changed );
            if( parseAllJob == null )
                updateJob.schedule();
        }
        
        @Override
        public IStatus run( IProgressMonitor monitor ){
            if( currentProject == null ){
                monitor.done();
                return new Status( Status.ERROR, TinyOSPlugin.PLUGIN_ID, "no project" );
            }

            monitor.beginTask( "Parse and Analyze", 10 );
            IProgressMonitor runMonitor = new SubProgressMonitor( monitor, 3 );
            runMonitor.beginTask( "Parse and Analyze", 3 );
            
            // collect all files which *might* be affected
            MakeTargetJob meJob = new MakeTargetJob( currentProject );
            meJob.setPriority( getPriority() );
            currentProject.getModel().runJob( meJob, new SubProgressMonitor( runMonitor, 1 ) );
            final MakeTarget me = meJob.getTarget();

            if( me == null ){
                cancel();
                monitor.done();
                return Status.CANCEL_STATUS;
            }

            if( monitor.isCanceled() ){
                monitor.done();
                return Status.CANCEL_STATUS;
            }

            
            AllReachableFilesJob reachNC = new AllReachableFilesJob( currentProject, me, "nc" );
            reachNC.setPriority( getPriority() );
            currentProject.getModel().runJob( reachNC, new SubProgressMonitor( runMonitor, 1 ) );

            if( monitor.isCanceled() ){
                monitor.done();
                return Status.CANCEL_STATUS;
            }

            AllReachableFilesJob reachH = new AllReachableFilesJob( currentProject, me, "h" );
            reachH.setPriority( getPriority() );
            currentProject.getModel().runJob( reachH, new SubProgressMonitor( runMonitor, 1 ) );

            if( monitor.isCanceled() ){
                monitor.done();
                return Status.CANCEL_STATUS;
            }

            File[] fnc = reachNC.getFiles();
            File[] fh = reachH.getFiles();

            if( fnc == null )
                fnc = new File[]{};

            if( fh == null )
                fh = new File[]{};

            // now filter out those files which *are* affected
            List<File> files = new ArrayList<File>();
            final ProjectModel projectModel = currentProject.getModel();

            for( File[] array : new File[][]{ fnc, fh }){
                for( File file : array ){
                    if( monitor.isCanceled() ){
                        monitor.done();
                        return Status.CANCEL_STATUS;
                    }
                    
                    IParseFile parseFile = projectModel.parseFile( file );
                    if( changed.contains( parseFile ))
                        files.add( file );
                    else{
                        for( IParseFile change : changed ){
                            if( projectModel.isIncluded( parseFile, change )){
                                files.add( file );
                                break;
                            }
                        }
                    }
                }
            }
            
            int length = files.size();

            runMonitor.done();
            IProgressMonitor mainMonitor = new SubProgressMonitor( monitor, 7 );
            mainMonitor.beginTask("Parse and analyze", 2*length);

            final INesCParserFactory factory = TinyOSPlugin.getDefault().getParserFactory();

            try{
                int i = 0;
                for( final File file : files ){
                    i++;
                    mainMonitor.subTask( i + "/" + length + " " + file.getName()  );

                    if( mainMonitor.isCanceled() )
                        break;

                    PublicJob job = new PublicJob( "parse " + file.getName() ){
                        @Override
                        public IStatus run( IProgressMonitor monitor ){
                            monitor.beginTask( "parse", 2 );
                            IParseFile parseFile = projectModel.parseFile( file );
                            
                            model.removeNodes( parseFile );
                            model.markForLaterRemoving();
                            
                            INesCParser parser = factory.createParser( currentProject.getProject() );
                            parser.setParseFile( parseFile );
                            parser.setCreateMessages( false );
                            parser.setFollowIncludes( true );
                            parser.setASTModel( model );
                            parser.setResolveFullModel( true );
                            currentProject.getModel().getBasicDeclarations().addBasics( parser, parseFile, new SubProgressMonitor( monitor, 1 ) );

                            Debug.info( "size before parsing: " + model.getSize() );
                            
                            try{
                                parser.parse( new FileMultiReader( file ), new SubProgressMonitor( monitor, 1 ) );
                            }
                            catch ( IOException e ){
                                e.printStackTrace();
                            }

                            Debug.info( "size after parsing: " + model.getSize() );
                            model.remove( cacheFilter, includeFilter );
                            Debug.info( "size after clean up: " + model.getSize() );
                            
                            monitor.done();
                            return Status.OK_STATUS;
                        }
                    };
                    job.setPriority( getPriority() );
                    job.setSystem( true );
                    currentProject.getModel().runJob( job, new SubProgressMonitor( mainMonitor, 1 ) );

                    mainMonitor.worked( 1 );
                }
            }
            finally{
                synchronized( ViewModel.this ){
                    if( updateJob == this ){
                        updateJob = null;
                    }
                }
                view.refresh( currentProject.getModel() );
            }
            return Status.OK_STATUS;
        }
    }
    
    /**
     * This job can parse all files that are in the system and so fill this model
     * @author Benjamin Sigg
     */
    private class ParseAllJob extends CancelingJob{
        private ProjectTOS currentProject;

        public ParseAllJob( ProjectTOS currentProject ){
            super( "Analyze files: " + currentProject.getProject().getName() );
            setPriority(Job.DECORATE);
            this.currentProject = currentProject;
        }

        @Override
        public IStatus run( IProgressMonitor monitor ){
            if( currentProject == null ){
                monitor.done();
                return new Status( Status.ERROR, TinyOSPlugin.PLUGIN_ID, "no project" );
            }

            monitor.beginTask( "Parse All", 7 );
            SubProgressMonitor runMonitor = new SubProgressMonitor( monitor, 3 );
            runMonitor.beginTask( "Parse All", 3 );
            
            MakeTargetJob meJob = new MakeTargetJob( currentProject );
            meJob.setPriority( getPriority() );
            currentProject.getModel().runJob( meJob, runMonitor );
            final MakeTarget me = meJob.getTarget();

            if( me == null ){
                cancel();
                monitor.done();
                return Status.CANCEL_STATUS;
            }

            AllReachableFilesJob reachNC = new AllReachableFilesJob( currentProject, me, "nc" );
            reachNC.setPriority( getPriority() );
            currentProject.getModel().runJob( reachNC, runMonitor );
            
            if( monitor.isCanceled() ){
                monitor.done();
                return Status.CANCEL_STATUS;
            }

            AllReachableFilesJob reachH = new AllReachableFilesJob( currentProject, me, "h" );
            reachH.setPriority( getPriority() );
            currentProject.getModel().runJob( reachH, runMonitor );

            if( monitor.isCanceled() ){
                monitor.done();
                return Status.CANCEL_STATUS;
            }

            File[] fnc = reachNC.getFiles();
            File[] fh = reachH.getFiles();

            if( fnc == null )
                fnc = new File[]{};

            if( fh == null )
                fh = new File[]{};

            int length = fnc.length + fh.length;

            runMonitor.done();
            IProgressMonitor mainMonitor = new SubProgressMonitor( monitor, 7 );
            mainMonitor.beginTask("Parse and analyze", 2*length);

            final INesCParserFactory factory = TinyOSPlugin.getDefault().getParserFactory();
            final IASTModel model = factory.createModel( currentProject.getProject() );

            try{
                for (int i = 0; i < length; i++) {
                    final File file = i < fnc.length ? fnc[i] : fh[ i - fnc.length ];
                    mainMonitor.subTask( (i+1) + "/" + length + " " + file.getName()  );

                    if( mainMonitor.isCanceled() )
                        break;

                    PublicJob job = new PublicJob( "parse " + file.getName() ){
                        @Override
                        public IStatus run( IProgressMonitor monitor ){
                            monitor.beginTask( "parse", 2 );
                            model.markForLaterRemoving();
                            
                            INesCParser parser = factory.createParser( currentProject.getProject() );
                            IParseFile parseFile = currentProject.getModel().parseFile( file );
                            parser.setParseFile( parseFile );
                            parser.setCreateMessages( false );
                            parser.setFollowIncludes( true );
                            parser.setASTModel( model );
                            parser.setResolveFullModel( true );
                            currentProject.getModel().getBasicDeclarations().addBasics( parser, parseFile, new SubProgressMonitor( monitor, 1 ) );

                            Debug.info( "size before parsing: " + model.getSize() );
                            
                            try{
                                parser.parse( new FileMultiReader( file ), new SubProgressMonitor( monitor, 1 ) );
                            }
                            catch ( IOException e ){
                                e.printStackTrace();
                            }

                            Debug.info( "size after parsing: " + model.getSize() );
                            model.remove( cacheFilter, includeFilter );
                            Debug.info( "size after clean up: " + model.getSize() );
                            
                            monitor.done();
                            return Status.OK_STATUS;
                        }
                    };
                    job.setPriority( getPriority() );
                    job.setSystem( true );
                    currentProject.getModel().runJob( job, new SubProgressMonitor( mainMonitor, 1 ) );

                    mainMonitor.worked( 1 );
                }
                
                mainMonitor.done();
                monitor.done();
            }
            finally{
                synchronized( ViewModel.this ){
                    if( parseAllJob == this ){
                        parseAllJob = null;
                        if( updateJob != null )
                            updateJob.schedule();
                    }
                    ViewModel.this.model = model;
                }
                view.refresh( currentProject.getModel() );
            }
            return Status.OK_STATUS;
        }
    }
}
