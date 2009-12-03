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
package tinyos.yeti.model.jobs;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.Debug;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.INesCInitializer;
import tinyos.yeti.ep.parser.INesCParserFactory;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.jobs.MakeTargetJob;
import tinyos.yeti.jobs.PublicJob;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.model.IFileCache;
import tinyos.yeti.model.IFileModel;
import tinyos.yeti.model.IProjectDefinitionCollector;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nesc.FileMultiReader;

public abstract class InitializeJob extends CancelingJob{
    private int cacheCount = 0;
    private ProjectModel model;
    private boolean startup;
    private boolean forceClear;

    public InitializeJob( ProjectModel model, boolean startup, boolean forceClear ){
        super( "Initialize TinyOS project: " + model.getProject().getProject().getName() );
        this.startup = startup;
        this.forceClear = forceClear;
        this.model = model;
        setPriority(Job.LONG);
    }

    protected abstract void setInitialized( boolean initialized );

    protected abstract IFileModel getFileModel();

    protected abstract IProjectDefinitionCollector getDefinitionCollector();

    protected abstract void put( IParseFile file, IDeclaration[] declarations );

    protected abstract void fireInitialized();

    @Override
    public IStatus run( IProgressMonitor monitor ){
        Debug.enter();
        setInitialized( false );

        monitor.beginTask( "Initialize", 10 );
        IProgressMonitor setupMonitor = new SubProgressMonitor( monitor, 1 );
        setupMonitor.beginTask( "Initialize", 2 );
        
        // make sure cache is not out of sync
        if( startup ){
            monitor.subTask( "Refresh Cache" );
            PublicJob refreshJob = new PublicJob( "Refresh Cache"){
                @Override
                public IStatus run( IProgressMonitor monitor ){
                    IFolder folder = model.getProject().getCacheContainer();
                    if( folder.exists() ){
                        try{
                            folder.refreshLocal( IFolder.DEPTH_INFINITE, monitor );
                            
                            if( !folder.isDerived() ){
                            	folder.accept( new DeriveResourceVisitor() );
                            }
                        }
                        catch ( CoreException e ){
                            TinyOSPlugin.warning( e.getStatus() );
                        }
                    }
                    return Status.OK_STATUS;
                }
            };
            refreshJob.setPriority( getPriority() );
            model.runJob( refreshJob, new SubProgressMonitor( setupMonitor, 1 ) );
        }
        
        // clear caches if new setting (startup == no new setting)        
        if( !startup || forceClear ){
            monitor.subTask( "Clear caches" );
            PublicJob clearJob = new PublicJob( "Clear caches" ){
                @Override
                public IStatus run( IProgressMonitor monitor ){
                    model.deleteProjectCache( true, monitor );
                    return Status.OK_STATUS;
                }
            };
            clearJob.setPriority( getPriority() );
            model.runJob( clearJob, new SubProgressMonitor( setupMonitor, 1 ) );
        }

        // setup
        MakeTargetJob meJob = new MakeTargetJob( model.getProject() );
        meJob.setPriority( getPriority() );
        model.runJob( meJob, new SubProgressMonitor( setupMonitor, 1 ) );
        setupMonitor.done();
        final MakeTarget me = meJob.getTarget();

        if( me == null ){
            cancel();
            monitor.done();
            return Status.CANCEL_STATUS;
        }

        final IFileModel fileModel = getFileModel();
        fileModel.refresh( me, new SubProgressMonitor( monitor, 1 ) );

        // collect all files that will be parsed
        IParseFile[] files = fileModel.getFiles( "nc" );

        int size = files.length;
        List<IParseFile> allFiles = fileModel.getAllFiles();
        
        // size += wiring
        for( IParseFile file : allFiles ){
            if( file.isProjectFile() )
                size++;
        }
        
        int count = 0;

        // The jobs initial arguments are now known
        IProgressMonitor mainMonitor = new SubProgressMonitor( monitor, 8 );
        mainMonitor.beginTask("Initialize", size );

        INesCParserFactory factory = TinyOSPlugin.getDefault().getParserFactory();
        final INesCInitializer initializer = factory.createInitializer( model.getProject().getProject() );

        // initialize each file
        for (int i = 0, n = files.length; i<n; i++) {
            final IParseFile file = files[i];
            mainMonitor.subTask( (++count) + "/" + size + " init " + file.getName() );
            readDeclarations( file, initializer, mainMonitor );
            if( mainMonitor.isCanceled() ){
            	mainMonitor.done();
                return Status.CANCEL_STATUS;
            }
        }

        // wire the project files
        for( final IParseFile file : allFiles ){
            if( file.isProjectFile() ){
            	mainMonitor.subTask( (++count) + "/" + size + " wire " + file.getName() );

                dependencies( file, monitor );
                if( mainMonitor.isCanceled() ){
                	mainMonitor.done();
                    return Status.CANCEL_STATUS;
                }

                mainMonitor.worked( 1 );
            }
        }

        // notify listeners and stop job

        setInitialized( true );

        if( mainMonitor.isCanceled() ){
        	mainMonitor.done();
            return Status.CANCEL_STATUS;
        }

        fireInitialized();

        Debug.leave();
        mainMonitor.done();
        monitor.done();

        return Status.OK_STATUS;
    }

    private void readDeclarations( final IParseFile file, final INesCInitializer initializer, IProgressMonitor monitor ){
        final IFileModel fileModel = getFileModel();

        final boolean[] done = new boolean[]{ false };

        for( int i = 0; i < 2 && !done[0]; i++ ){
            if( monitor.isCanceled() ){
                monitor.done();
                return;
            }

            CancelingJob job = null;

            if( i == 0 ){
                // try read from cache
                final IFileCache<IDeclaration[]> cache = fileModel.getInitCache();
                if( cache != null && cache.canReadCache( file )){
                    job = new CancelingJob( file.getName() ){
                        @Override
                        public IStatus run( IProgressMonitor monitor ){
                            try{
                                IDeclaration[] declarations = cache.readCache( file, monitor );
                                if( monitor.isCanceled() )
                                    return Status.CANCEL_STATUS;
                                if( declarations != null ){
                                    put( file, declarations );
                                }
                                monitor.done();
                                done[0] = true;
                                return Status.OK_STATUS;
                            }
                            catch( CoreException ex ){
                                monitor.done();
                                return ex.getStatus();
                            }
                            catch( IOException ex ){
                                monitor.done();
                                return new Status( Status.WARNING, TinyOSPlugin.PLUGIN_ID, ex.getLocalizedMessage(), ex );
                            }
                        }
                    };
                }
            }

            if( i == 1 ){
                // could not read cache, need to parse the file
                job = new CancelingJob( file.getName() ){
                    @Override
                    public IStatus run( IProgressMonitor monitor ){
                        monitor.beginTask( file.getPath(), 1000 );

                        if( monitor.isCanceled() ){
                            monitor.done();
                            return Status.CANCEL_STATUS;
                        }

                        Debug.info( "initialize: " + file.getPath() );

                        try{
                            IDeclaration[] result = initializer.analyze( 
                                    file, 
                                    new FileMultiReader( file.toFile() ),
                                    new SubProgressMonitor( monitor, 800 ));

                            if( monitor.isCanceled() ){
                                monitor.done();
                                return Status.CANCEL_STATUS;
                            }

                            if( result != null ){
                                put( file, result );
                                cacheCount += result.length;
                            }

                            // store the files if possible
                            IFileCache<IDeclaration[]> cache = fileModel.getInitCache();
                            if( cache != null ){
                                cache.writeCache( file, result, new SubProgressMonitor( monitor, 200 ) );
                            }
                            else{
                                monitor.worked( 200 );
                            }
                        }
                        catch( CoreException ex ){
                            TinyOSPlugin.getDefault().getLog().log( ex.getStatus() );
                            ex.printStackTrace();
                        }
                        catch( IOException e ){
                            e.printStackTrace();
                        }

                        Debug.info( "declarations: " + cacheCount );

                        monitor.done();
                        return Status.OK_STATUS;
                    }
                };
            }

            if( job != null ){
                job.setSystem( true );
                job.setPriority( getPriority() );
                model.runJob( job, new SubProgressMonitor( monitor, 0 ) );
            }
        }

        monitor.worked( 1 );        
    }

    private void dependencies( final IParseFile file, IProgressMonitor monitor ){
        final IFileModel fileModel = getFileModel();
        final boolean[] done = new boolean[]{ false };

        for( int i = 0; i < 2 && !done[0]; i++ ){
            CancelingJob job = null;
            if( i == 0 ){
                // let's have a look in the cache, maybe we don't need to
                // parse the whole file
                final IFileCache<Set<IParseFile>> cache = fileModel.getDependencyCache();
                if( cache != null && cache.canReadCache( file )){
                    job = new CancelingJob( file.getName() ){
                        @Override
                        public IStatus run( IProgressMonitor monitor ){
                            try{
                                monitor.beginTask( "Read dependency cache", 1000 );
                                Set<IParseFile> dependencies = cache.readCache( file, new SubProgressMonitor( monitor, 800 ) );
                                if( monitor.isCanceled() ){
                                    monitor.done();
                                    return Status.OK_STATUS;
                                }
                                getDefinitionCollector().updateInclusions( file, dependencies );
                                done[0] = true;
                                monitor.done();
                                return Status.OK_STATUS;
                            }
                            catch( CoreException ex ){
                                monitor.done();
                                return ex.getStatus();
                            }
                            catch( IOException ex ){
                                monitor.done();
                                return new Status( Status.ERROR, TinyOSPlugin.PLUGIN_ID, ex.getLocalizedMessage(), ex );
                            }

                        }
                    };
                }
            }

            if( i == 1 ){
                // there was nothing in the cache,
                // mark the file for rebuild
                model.buildMark( file, false );
                /*
                job = new CancelingJob( file.getName() ){
                    @Override
                    protected IStatus run( IProgressMonitor monitor ) {
                        getDefinitionCollector().updateInclusions( file, null, monitor );
                        return Status.OK_STATUS;
                    }
                };
                */
            }

            if( job != null ){
                job.setSystem( true );
                job.setPriority( getPriority() );
                model.runJob( job, new SubProgressMonitor( monitor, 0 ) );       
            }
        }
    }
    
    private static class DeriveResourceVisitor implements IResourceVisitor{
		public boolean visit( IResource resource ) throws CoreException{
			resource.setDerived( true );
			return true;
		}
    }
}
