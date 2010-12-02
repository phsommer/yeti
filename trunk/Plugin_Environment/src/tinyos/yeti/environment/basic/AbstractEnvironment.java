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
package tinyos.yeti.environment.basic;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.environment.basic.commands.ICommand;
import tinyos.yeti.environment.basic.commands.ICommandExecuter;
import tinyos.yeti.environment.basic.commands.make.Compile;
import tinyos.yeti.environment.basic.example.IExample;
import tinyos.yeti.environment.basic.example.IExampleManager;
import tinyos.yeti.environment.basic.path.IPathManager;
import tinyos.yeti.environment.basic.path.IPathTranslator;
import tinyos.yeti.environment.basic.path.PathRequest;
import tinyos.yeti.environment.basic.platform.IPlatformManager;
import tinyos.yeti.environment.basic.progress.ICancellation;
import tinyos.yeti.environment.basic.progress.NullCancellation;
import tinyos.yeti.environment.basic.progress.ProgressMonitorCancellation;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IEnvironmentListener;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.ep.ISensorBoard;
import tinyos.yeti.ep.ITest;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.make.MakeExclude;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.nature.MissingNatureException;

/**
 * An abstract environment delegates most of its work to other objects.
 * @author Benjamin Sigg
 */
public abstract class AbstractEnvironment implements IEnvironment, IPathTranslator{
    private String id;
    private String description;
    private String name;
    private int importance = 0;

    private IPathManager pathManager;
    private IExampleManager exampleManager;
    private IPlatformManager platformManager;
    private ICommandExecuter commandExecuter;

    private List<IEnvironmentListener> listeners = new ArrayList<IEnvironmentListener>();
    private List<Runnable> runAfterStartup = new ArrayList<Runnable>();
    private volatile boolean startedUp = false;

    private List<ITest> tests = new ArrayList<ITest>();
    
    public AbstractEnvironment(){
        // nothing to do
    }

    public void addTest( ITest test ){
        if( test == null )
            throw new IllegalArgumentException( "test must not be null" );
        tests.add( test );
    }

    public ITest[] getTests() {
        return tests.toArray( new ITest[ tests.size() ] );
    }

    public void setEnvironmentImportance( int importance ){
        this.importance = importance;
    }

    public int getEnvironmentImportance(){
        return importance;
    }

    public void setEnvironmentDescription( String description ){
        this.description = description;
    }

    public String getEnvironmentDescription(){
        return description;
    }

    public void setEnvironmentID( String id ){
        this.id = id;
    }

    public String getEnvironmentID(){
        return id;
    }

    public void setEnvironmentName( String name ){
        this.name = name;
    }

    public String getEnvironmentName(){
        return name;
    }

    public void addEnvironmentListener( IEnvironmentListener listener ) {
        if( listener == null )
            throw new IllegalArgumentException( "listener must not be null" );

        listeners.add( listener );
    }

    public void removeEnvironmentListener( IEnvironmentListener listener ) {
        listeners.remove( listener );
    }

    protected IEnvironmentListener[] getListeners(){
        return listeners.toArray( new IEnvironmentListener[ listeners.size() ] );
    }

    /**
     * Calls the {@link IEnvironmentListener#reinitialized(IEnvironment)}
     * method of all listeners known to this environment
     * @param delay number of milliseconds to wait until event is fired, 
     * helpful if many events are to be fired at the same time
     */
    public void fireReinitialized( int delay ){
        fireReinitializedJob.schedule( delay );
    }

    private Job fireReinitializedJob = new Job( "Fire Reinitialized" ){
        {
            setPriority( Job.SHORT );
            setSystem( true );
        }
        @Override
        protected IStatus run( IProgressMonitor monitor ) {
            IEnvironmentListener[] listeners = getListeners();
            monitor.beginTask( "Fire", listeners.length );
            for( IEnvironmentListener listener : listeners ){
                listener.reinitialized( AbstractEnvironment.this );
                monitor.worked( 1 );
            }
            monitor.done();
            return Status.OK_STATUS;
        }
    };

    public void runAfterStartup( Runnable run ) {
        synchronized( runAfterStartup ){
            if( startedUp )
                run.run();
            else
                runAfterStartup.add( run );
        }
    }

    /**
     * Informs this environment that all its settings are made and that it
     * now can be used.
     * @see #runAfterStartup(Runnable)
     */
    public void setStartedUp() {
        synchronized( runAfterStartup ){
            this.startedUp = true;
            for( Runnable run : runAfterStartup ){
                run.run();
            }
            runAfterStartup.clear();
        }
    }

    protected void setExampleManager( IExampleManager exampleManager ){
        this.exampleManager = exampleManager;
    }

    protected abstract IExampleManager createExampleManager();

    public IExampleManager getExampleManager(){
        if( exampleManager == null )
            exampleManager = createExampleManager();

        return exampleManager;
    }

    public String[] getExampleApplicationNames(){
        IExampleManager manager = getExampleManager();
        IExample[] examples = manager.getExamples();
        if( examples == null )
            return new String[]{};

        String[] names = new String[ examples.length ];
        for( int i = 0, n = names.length; i<n; i++ )
            names[i] = examples[i].getName();

        return names;
    }

    public File getExampleAppDirectory( String appName ){
        IExampleManager manager = getExampleManager();
        IExample[] examples = manager.getExamples();

        if( examples == null )
            return null;

        for( IExample example : examples ){
            if( example.getName().equals( appName ))
                return example.getDirectory();
        }

        return null;
    }    

    protected void setPlatformManager( IPlatformManager platformManager ){
        this.platformManager = platformManager;
    }

    protected abstract IPlatformManager createPlatformManager();

    public IPlatformManager getPlatformManager(){
        if( platformManager == null )
            platformManager = createPlatformManager();

        return platformManager;
    }

    public IPlatform[] getPlatforms(){
        return getPlatformManager().getPlatforms();
    }

    protected void setCommandExecuter( ICommandExecuter commandExecuter ){
        this.commandExecuter = commandExecuter;
    }

    public ICommandExecuter getCommandExecuter(){
        if( commandExecuter == null )
            commandExecuter = createCommandExecuter();

        return commandExecuter;
    }

    protected abstract ICommandExecuter createCommandExecuter();

    public void executeMake( OutputStream info, OutputStream out, OutputStream error,
            File directory, ProjectTOS project, IMakeTarget target,
            IProgressMonitor progress ){

        try{
            progress.beginTask( "Make", IProgressMonitor.UNKNOWN );
            Compile compile = new Compile( directory, project, target, this );
            execute( compile, progress, info, out, error );
        }
        catch( CoreException ex ){
            TinyOSPlugin.log( ex.getStatus() );
        }
        finally{
            progress.done();
        }
    }

    public <R> R execute( ICommand<R> command ){
        try{
            return getCommandExecuter().execute( command );
        }
        catch ( InterruptedException e ){
            TinyOSAbstractEnvironmentPlugin.log(
                    new Status( Status.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, e.getMessage(), e ) );
        }
        catch ( IOException e ){
            TinyOSAbstractEnvironmentPlugin.log(
                    new Status( Status.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, e.getMessage(), e ) );
        }

        return null;
    }

    public <R> R execute( ICommand<R> command, IProgressMonitor monitor, OutputStream info, OutputStream out, OutputStream error ){
        try{
            return getCommandExecuter().execute( command, monitor, info, out, error );
        }
        catch ( InterruptedException e ){
            TinyOSAbstractEnvironmentPlugin.log(
                    new Status( Status.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, e.getMessage(), e ) );
        }
        catch ( IOException e ){
            TinyOSAbstractEnvironmentPlugin.log(
                    new Status( Status.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, e.getMessage(), e ) );
        }

        return null;
    }

    protected void setPathManager( IPathManager pathManager ){
        this.pathManager = pathManager;
    }

    /**
     * Creates a new {@link IPathManager} that will be used by this environment.
     * @return the new path manager
     */
    protected abstract IPathManager createPathManager();

    /**
     * Gets the path manager of this environment. If not yet created, then
     * it will be created using {@link #createPathManager()}.
     * @return the path manager
     */
    public IPathManager getPathManager(){
        if( pathManager == null )
            pathManager = createPathManager();

        return pathManager;
    }

    public File modelToSystem( String path ) {
        return getPathManager().modelToSystem( path );
    }

    public String systemToModel( File file ) {
        return getPathManager().systemToModel( file );
    }

    public File[] getAllReachableFiles( IProject p, 
            MakeInclude[] directives, MakeExclude[] excludes,
            ISensorBoard[] boards, String platformName, boolean nostdinc,
            String[] fileExtensions, Set<SearchFlag> flags, IProgressMonitor monitor ){

        if( monitor == null )
            monitor = new NullProgressMonitor();

        monitor.beginTask( "Search all files", IProgressMonitor.UNKNOWN );
        ICancellation cancellation = new ProgressMonitorCancellation( monitor );

        IPathManager manager = getPathManager();
        PathRequest request = new PathRequest();

        try{
	        ProjectTOS project = TinyOSPlugin.getDefault().getProjectTOS( p );
	
	        request.setProject( project );
	        request.setDirectives( directives );
	        request.setExcludes( excludes );
	        request.setBoards( boards );
	        request.setPlatformName( platformName );
	        request.setNostdinc( nostdinc );
	        request.setFlags( flags );
	        if( fileExtensions != null )
	            request.setFileExtensions( fileExtensions );
	
	        File[] result = manager.getAllReachableFiles( request, cancellation );
	        monitor.done();
	        return result;
        }
        catch( MissingNatureException ex ){
        	// silent
        	return new File[]{};
        }
    }

    public File locate( String fileName, IProject p, 
            MakeInclude[] directives, MakeExclude[] excludes,
            ISensorBoard[] boards, String platform, boolean nostdinc,
            boolean systemFile, IProgressMonitor monitor ){

        if( monitor == null )
            monitor = new NullProgressMonitor();

        monitor.beginTask( "Locate file", IProgressMonitor.UNKNOWN );
        ICancellation cancellation = new ProgressMonitorCancellation( monitor );

        IPathManager manager = getPathManager();
        PathRequest request = new PathRequest();

        try{
	        ProjectTOS project = TinyOSPlugin.getDefault().getProjectTOS( p );
	
	        request.setProject( project );
	        request.setDirectives( directives );
	        request.setExcludes( excludes );
	        request.setBoards( boards );
	        request.setPlatformName( platform );
	        request.setNostdinc( nostdinc );
	        request.setSystemFiles( systemFile );
	        request.setFlags( Collections.<IEnvironment.SearchFlag>emptySet() );
	
	        File result = manager.locate( fileName, request, cancellation );
	        monitor.done();
	        return result;
        }
        catch( MissingNatureException ex ){
        	// silent
        	return null;
        }
    }

    public File[] getStandardInclusionFiles(){
        IPathManager manager = getPathManager();
        return manager.getStandardInclusionFiles( new NullCancellation() );
    }

    public String getSuitablePath( File file ){
        return systemToModel( file );
    }
}
