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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.Debug;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCProblemMarker;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.ASTNodeFilterFactory;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.IMessage;
import tinyos.yeti.ep.parser.INesCInitializer;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.INesCParserFactory;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.jobs.IPublicJob;
import tinyos.yeti.model.jobs.InitializeJob;
import tinyos.yeti.model.missing.IMissingResource;
import tinyos.yeti.nesc.FileMultiReader;
import tinyos.yeti.nesc.IMultiReader;

/**
 * The project model holds various values that are associated with a whole
 * tos-project. These informations include:
 * <ul>
 *  <li>all {@link IDeclaration}s </li>
 * </ul>
 * @author Benjamin Sigg
 */
public class ProjectModel {
    /**
     * A factory method for creating a new {@link IDeclarationCollection}
     * which wraps around <code>declaration</code>.
     * @param declarations the declarations which are transformed into a
     * collection.
     * @return the new collection
     */
    public static IDeclarationCollection toCollection( IDeclaration[] declarations ){
        return new ArrayDeclarationCollection( declarations );
    }

    /** the maximum number of files that are cached in the {@link #getAstModel()} */
    public static final int INITIAL_CACHE_SIZE_AST_MODEL = 10;

    /** the maximum number of files whose definitions are cached */
    public static final int INITIAL_CACHE_SIZE_GLOBAL_DEFINITIONS = 500;

    private static final QualifiedName LAST_BUILD = new QualifiedName( TinyOSPlugin.PLUGIN_ID, "last_build" );
    private static final QualifiedName RESOURCE_CHANGED = new QualifiedName( TinyOSPlugin.PLUGIN_ID, "change_build" );

    private ProjectTOS project;
    private Job initializeJob;
    private volatile boolean initialized = false;

    /** all global declarations that were found in the project */
    private Map<IParseFile, Cache> declarations = new HashMap<IParseFile, Cache>();

    /** a model containing only the contents of a few files */
    private IASTModel astModel;

    /** the names of all files that are currently part of {@link #astModel} */
    private LinkedList<ASTContent> astModelContent = new LinkedList<ASTContent>();

    private boolean onGettingNode = false;

    /** any file in this map that has a value higher than 0 will not be removed from the cache */
    private Map<IParseFile, Integer> frozen = new HashMap<IParseFile, Integer>();
    private int fullFreeze = 0;

    private IProjectDefinitionCollector definitionCollection;

    private List<IProjectModelListener> listeners = new ArrayList<IProjectModelListener>();
    private List<IParseFile> changedFiles = new ArrayList<IParseFile>();

    private IFileModel fileModel;

    private BasicDeclarationSet basicDeclarations;

    private boolean startup = true;

    /** thread that has secure access to this model */
    private ReentrantLock secureThread = new ReentrantLock();
    
    /** number of elements that can be in the cache */
    private int cacheSize = INITIAL_CACHE_SIZE_AST_MODEL;

    public ProjectModel( ProjectTOS project ){
        this.project = project;

        IModelConfiguration configuration = TinyOSPlugin.getDefault().getModelConfiguration();

        fileModel = configuration.createFileModel( this );
        definitionCollection = configuration.createDefinitionCollector( this );

        basicDeclarations = new BasicDeclarationSet( this );
    }

    /**
     * Gets the set of basic declarations.
     * @return the set of basic declarations
     */
    public BasicDeclarationSet getBasicDeclarations(){
        return basicDeclarations;
    }

    /**
     * Searches and adds global definitions of typedefs, fields, functions but
     * not interfaces or components to <code>parser</code>, assuming that
     * the file <code>parseFile</code> will be processed by <code>parser</code><br>
     * This method analyzes <code>parseFile</code> to find out which other files
     * are included.
     * @param parseFile the file that will be processed
     * @param reader a reader for <code>parseFile</code>, can be <code>null</code>
     * @param parser the parser to fill with declarations
     * @param monitor used to inform about the state and to cancel this operation
     */
    public void addIncludedDeclarations( IParseFile parseFile, IMultiReader reader, INesCParser parser, IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();

        monitor.beginTask( "Collect C-declarations", 1000 );

        File file = parseFile.toFile();
        if( file != null ){
            List<IDeclaration> declarations = definitionCollection.collect( file, reader, true, new SubProgressMonitor( monitor, 1000 ) );
            if( monitor.isCanceled() ){
                monitor.done();
                return;
            }
            parser.addDeclarations( declarations.toArray( new IDeclaration[ declarations.size() ] ) );
        }

        monitor.done();
    }

    protected IASTModel getAstModel(){
        if( astModel == null )
            astModel = newASTModel();

        return astModel;
    }

    public void dispose() {
        // nothing to do right now
    }

    public ProjectTOS getProject(){
        return project;
    }

    public IFileModel getFileModel(){
        return fileModel;
    }

    public synchronized void addProjectModelListener( IProjectModelListener listener ){
        listeners.add( listener );
    }

    public synchronized void removeProjectModelListener( IProjectModelListener listener ){
        listeners.remove( listener );
    }

    protected synchronized IProjectModelListener[] listeners(){
        return listeners.toArray( new IProjectModelListener[ listeners.size() ] );
    }

    /**
     * Informs this model that a block of continuous changes has finished
     */
    public synchronized void informContinuousFinished(){
        if( initialized ){
            IParseFile[] files = changedFiles.toArray( new IParseFile[ changedFiles.size() ] );
            changedFiles.clear();

            for( IProjectModelListener listener : listeners() )
                listener.changed( files );
        }
        else
            changedFiles.clear();
    }

    /**
     * Gets the {@link IASTModel} that is used as cache to store information
     * about some files. Be warned: the contents of this model can change any
     * time.
     * @return the model
     */
    public IASTModel getCacheModel(){
        return getAstModel();
    }

    /**
     * Gets the collector for definitions and wiring elements. <br>
     * <b>NOTE:</b> this method is not intended to be used by clients.
     * @return the collector
     */
    public IProjectDefinitionCollector getDefinitionCollection(){
        return definitionCollection;
    }

    /**
     * Tries to find a declaration that has the name <code>name</code>
     * and whose intersection of tags with <code>intersection</code> yields
     * a non empty set.
     * @param name the name of the declaration
     * @param kinds the kind of declaration that is searched
     * @return the first declaration that is found and matches the conditions
     */
    public IDeclaration getDeclaration( final String name, final IDeclaration.Kind... kinds ){
        return searchBestDeclaration( getDeclarations( name, kinds ) );
    }    

    /**
     * Gets all declarations whose kind is one of <code>kinds</code> and which
     * has the name <code>name</code>.
     * @param name the name of the declarations
     * @param kinds the filter for the declarations
     * @return the declarations
     */
    public List<IDeclaration> getDeclarations( final String name, final IDeclaration.Kind... kinds ){
        Monitor monitor = enter();
        try{
	        List<IDeclaration> list = new ArrayList<IDeclaration>();
	        for( Cache cache : declarations.values() ){
	            cache.declarations.fillDeclarations( list, name, kinds );
	        }
	
	        basicDeclarations.fillDeclarations( list, name, kinds );
	
	        return list;
        }
        finally{
        	monitor.leave();
        }
    }


    /**
     * Tries to find a declaration whose intersection of tags with <code>intersection</code> yields
     * a non empty set.
     * @param kinds the kind of declaration that is searched
     * @return the first declaration that is found and matches the conditions
     */
    public IDeclaration getDeclaration( final IDeclaration.Kind... kinds ){
        return searchBestDeclaration( getDeclarations( kinds ) );
    }

    /**
     * Gets all declarations whose kind is one of <code>kinds</code>.
     * @param kinds the filter for the declarations
     * @return the declarations
     */
    public List<IDeclaration> getDeclarations( final IDeclaration.Kind... kinds ){
       	Monitor monitor = enter();
       	try{
	        List<IDeclaration> list = new ArrayList<IDeclaration>();
	        for( Cache cache : declarations.values() ){
	            cache.declarations.fillDeclarations( list, kinds );
	        }
	
	        basicDeclarations.fillDeclarations( list, kinds );
	
	        return list;
       	}
       	finally{
       		monitor.leave();
       	}
    }

    /**
     * Gets all declarations which are in the file <code>file</code>. This method
     * does not parse the file, it only looks up the declarations in the cache.
     * If the file is not stored in the cache, then <code>null</code> is returned.
     * @param file the file whose declarations are searched
     * @return the declarations or <code>null</code> if <code>file</code> is
     * not in the cache
     */
    public IDeclaration[] getFileDeclarations( File file ){
        return getFileDeclarations( parseFile( file ) );
    }

    /**
     * Gets all declarations which are from a certain file.
     * @param parseFile the file to look for
     * @return all declarations of <code>parseFile</code> or <code>null</code> if the
     * file is not in the cache
     */
    public IDeclaration[] getFileDeclarations( IParseFile parseFile ){
        Monitor monitor = enter();
        try{
	        Cache cache = declarations.get( parseFile );
	        if( cache == null || cache.declarations == null )
	            return null;
	
	        return cache.declarations.toArray();
        }
        finally{
        	monitor.leave();
        }
    }

    /**
     * Gets a declaration which passes <code>filter</code>. If the result is ambiguous,
     * then the declaration which would be parsed before any other declaration
     * is returned.
     * @param filter the filter for declarations
     * @return a declaration or <code>null</code> if non was found
     */
    public IDeclaration getDeclaration( DeclarationFilter filter ){
        return searchBestDeclaration( getDeclarations( filter ) );
    }

    /**
     * Gets all declarations which pass <code>filter</code>.
     * @param filter the condition that each {@link IDeclaration} must meet.
     * @return the declarations
     */
    public List<IDeclaration> getDeclarations( DeclarationFilter filter ){
        Monitor monitor = enter();
        try{
	        List<IDeclaration> list = new ArrayList<IDeclaration>();
	        for( Cache cache : declarations.values() ){
	            cache.declarations.fillDeclarations( list, filter );
	        }
	
	        basicDeclarations.fillDeclarations( list, filter );
	
	        return list;
        }
        finally{
        	monitor.leave();
        }
    }

    /**
     * Searches for the best declaration in <code>declarations</code> using
     * the order in which the original files are parsed.
     * @param declarations the declarations to check
     * @return the best declaration or <code>null</code>
     */
    private IDeclaration searchBestDeclaration( List<IDeclaration> declarations ){
        if( declarations == null )
            return null;

        if( declarations.size() == 0 )
            return null;

        if( declarations.size() == 1 )
            return declarations.get( 0 );

        int index = -1;
        IDeclaration best = null;

        for( IDeclaration check : declarations ){
            if( best == null ){
                best = check;
                IParseFile file = check.getParseFile();
                if( file != null )
                    index = file.getIndex();
            }
            else{
                IParseFile file = check.getParseFile();
                if( file != null ){
                    if( index == -1 || file.getIndex() < index ){
                        index = file.getIndex();
                        best = check;
                    }
                }
            }
        }

        return best;
    }

    /**
     * Tries to find the {@link IASTModelNode} to which <code>connection</code>
     * points.
     * @param connection the connection
     * @return the node to which <code>connection</code> points or <code>null</code>
     */
    public IASTModelNode getNode( IASTModelNodeConnection connection, IProgressMonitor monitor ){
        boolean on = onGettingNode;
        onGettingNode = true;
        try{
            return getNode( connection, !on, monitor );
        }
        finally{
            onGettingNode = on;
        }
    }

    private IASTModelNode getNode( IASTModelNodeConnection connection, boolean fullLoad, IProgressMonitor monitor ){
        Monitor lock = enter();
        try{
            monitor = enter( monitor, "Search Node", 1000 );

            if( connection.isReference() ){
                // first have a quick look in the cache
                IASTModelNode node = getAstModel().getNode( connection );
                if( node != null ){
                    ASTContent content = getCacheContent( node.getParseFile() );
                    if(( content != null && content.isFullLoaded() ) || !fullLoad )
                        return node;
                }

                // the connection might point to another file
                IDeclaration declaration = getDeclaration( connection );
                IASTModelNode result = null;
                if( declaration != null ){
                    return getNode( declaration, fullLoad, new SubProgressMonitor( monitor, 1000 ) );
                }
                if( result == null ){
                	// try loading the file in which the nodes is suposed to be
                	IASTModelPath referencedPath = connection.getReferencedPath();
                	if( referencedPath != null ){
                		try{
                			freeze( referencedPath.getParseFile() );
                			updateModel( referencedPath.getParseFile(), fullLoad, true, new SubProgressMonitor( monitor, 1000 ) );
                			if( monitor.isCanceled() )
                				return null;

                			result = getAstModel().getNode( connection );
                		}
                		finally{
                			melt( referencedPath.getParseFile() );
                		}
                	}
                }

                return result;
            }
            else{
                // the nodes might simply no longer be in the cache
                try{
                    freeze( connection.getParseFile() );
                    updateModel( connection.getParseFile(), fullLoad, true, new SubProgressMonitor( monitor, 1000 ) );
                    if( monitor.isCanceled() )
                        return null;

                    return getAstModel().getNode( connection );
                }
                finally{
                    melt( connection.getParseFile() );
                }
            }
        }
        finally{
            monitor.done();
            lock.leave();
        }
    }

    /**
     * Searches the {@link IASTModelNode} that declares <code>declaration</code>.
     * @param declaration some element
     * @return the node for that element
     */
    public IASTModelNode getNode( IDeclaration declaration, IProgressMonitor monitor ){
        boolean on = onGettingNode;
        onGettingNode = true;
        try{
            return getNode( declaration, !on, monitor );
        }
        finally{
            onGettingNode = on;
        }
    }

    private IASTModelNode getNode( IDeclaration declaration, boolean fullLoad, IProgressMonitor monitor ){
    	if( monitor == null )
    		monitor = new NullProgressMonitor();
    	
    	Monitor lock = enter();
        try{
            freeze( declaration.getParseFile() );
            updateModel( declaration.getParseFile(), fullLoad, true, monitor );

            if( monitor.isCanceled() )
                return null;

            return getAstModel().getNode( declaration.getPath() );
        }
        finally{
            if( declaration != null && declaration.getParseFile() != null )
                melt( declaration.getParseFile() );
            lock.leave();
        }
    }

    /**
     * Searches the {@link IASTModelNode} to which <code>path</code> points.
     * @param path some path
     * @param monitor used to inform the user about the state and to cancel
     * parsing
     * @return the node to which path points
     */
    public IASTModelNode getNode( IASTModelPath path, IProgressMonitor monitor ){
        boolean on = onGettingNode;
        onGettingNode = true;
        try{
            return getNode( path, !on, monitor );
        }
        finally{
            onGettingNode = on;
        }
    }

    private IASTModelNode getNode( IASTModelPath path, boolean fullLoad, IProgressMonitor monitor ){
        Monitor lock = enter();

        try{
            freeze( path.getParseFile() );
            updateModel( path.getParseFile(), fullLoad, true, monitor );
            if( monitor.isCanceled() )
                return null;
            return getAstModel().getNode( path );
        }
        finally{
            if( path != null && path.getParseFile() != null )
                melt( path.getParseFile() );
            
            lock.leave();
        }
    }

    /**
     * Tries to find the declaration to which <code>connection</code> points.
     * @param connection the connection that points to some element
     * @return the declaration of that element
     */
    public IDeclaration getDeclaration( IASTModelNodeConnection connection ){
    	Monitor monitor = enter();
    	try{
	        final String identifier = connection.getIdentifier();
	        final TagSet tags = connection.getTags().keySet();
	
	        return getDeclaration( new DeclarationFilter(){
	            public boolean include( IDeclaration declaration ){
	                if( !declaration.getName().equals( identifier ))
	                    return false;
	
	                TagSet dtags = declaration.getTags();
	                return dtags != null && dtags.contains( tags );
	            }
	        });
    	}
    	finally{
    		monitor.leave();
    	}
    }

    /**
     * Searches all the references to which <code>parseFile</code> points. This
     * method has to run in a secure thread.
     * @param parseFile the file whose references are searched
     * @param monitor to report progress or abort the operation
     * @return the references, might be <code>null</code>
     */
    public IASTReference[] getReferences( IParseFile parseFile, IProgressMonitor monitor ){
    	if( monitor == null )
    		monitor = new NullProgressMonitor();
    	
    	Monitor lock = enter();
    	try{
	    	if( fileModel.getReferencesCache().canReadCache( parseFile ) ){
	    		return fileModel.getReferencesCache().readCache( parseFile, monitor );
	    	}
	    	
	    	monitor.beginTask( "References of '" + parseFile.getName() + "'", 7 );
	    	
	    	File file = parseFile.toFile();
	    	if( file == null || !file.canRead() || !file.exists() )
	    		return null;
	    	
	    	IMultiReader reader = new FileMultiReader( file );
	    	INesCParser parser = newParser( parseFile, reader, new SubProgressMonitor( monitor, 1 ) );
	    	if( monitor.isCanceled() ){
	    		monitor.done();
	    		return null;
	    	}
	    	
	    	parser.setCreateReferences( true );
	    	parser.parse( reader, new SubProgressMonitor( monitor, 5 ) );
	    	if( monitor.isCanceled() ){
	    		monitor.done();
	    		return null;
	    	}
	    	
	    	IASTReference[] result = parser.getReferences();
	    	fileModel.getReferencesCache().writeCache( parseFile, result, new SubProgressMonitor( monitor, 1 ) );
	    	
	    	monitor.done();
	    	return result;
    	}
    	catch( CoreException ex ){
    		TinyOSPlugin.log( ex );
    		return null;
    	}
    	catch( IOException ex ){
    		TinyOSPlugin.log( ex );
    		return null;
    	}
    	finally{
    		lock.leave();
    	}
    }
    
    /**
     * Creates a new empty {@link IASTModel}.
     * @return the new and empty model
     */
    public IASTModel newASTModel(){
        return TinyOSPlugin.getDefault().getParserFactory().createModel( project.getProject() );
    }

    public INesCParserFactory getParserFactory(){
    	TinyOSPlugin plugin = TinyOSPlugin.getDefault();
        if( plugin == null )
            throw new IllegalStateException( "shutdown" );
        
        return plugin.getParserFactory();
    }
    
    /**
     * This methods creates a new parser and prepares the parser such that it
     * can be directly used to parse the file <code>file</code>. All features
     * of the parser created by this method are disabled and have to be activated
     * by the client. However, all declarations and typedefs are set up (except
     * the declarations that are in <code>file</code>).
     * @param file the name of the file that will be parsed
     * @param reader a reader for <code>file</code>, can be <code>null</code>
     * @param monitor used to inform about the state and to cancel this operation
     * @return the new parser
     * @throws IOException if the file can't be opened
     */
    public INesCParser newParser( IParseFile file, IMultiReader reader, IProgressMonitor monitor ) throws IOException{
        if( monitor == null )
            monitor = new NullProgressMonitor();

        Monitor lock = enter();
        try{
	        monitor.beginTask( "Create Parser for '" + file.getName() + "'", 1500 );
	
	        INesCParserFactory factory = getParserFactory();
	        INesCParser parser = factory.createParser( project.getProject() );
	        parser.setParseFile( file );
	        getBasicDeclarations().addBasics( parser, file, new SubProgressMonitor( monitor, 500 ) );
	
	        if( file.getPath().endsWith( ".nc" )){
	            addIncludedDeclarations( file, reader, parser, new SubProgressMonitor( monitor, 1000 ));
	        }
	        else{
	            updateInclusionPaths( file, reader, new SubProgressMonitor( monitor, 1000 ) );
	        }
	
	        if( monitor.isCanceled() ){
	            monitor.done();
	            return null;
	        }
	
	        monitor.done();
	        return parser;
	    }
        finally{
        	lock.leave();
        }
    }

    public void updateInclusionPaths( IParseFile file, IMultiReader reader, IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();

        monitor.beginTask( "Update inclusion paths", 1000 );

        definitionCollection.updateInclusions( file, reader, new SubProgressMonitor( monitor, 1000 ) );

        monitor.done();
    }

    /**
     * Adds the cached declarations of the whole project and the used
     * libraries to <code>parser</code>.
     * @param file the file that should be excluded from adding
     * @param parser the parser whose {@link INesCParser#addDeclarations(IDeclaration[])}
     * method will be used
     */
    public void addDeclarations( IParseFile file, INesCParser parser ){
        for( Map.Entry<IParseFile, Cache> entry : declarations.entrySet() ){
            if( !entry.getKey().equals( file ))
                parser.addDeclarations( entry.getValue().declarations.toArray() );
        }
    }

    /**
     * Returns whether this model is already initialized or still awaiting
     * the execution of its initializing job.
     * @return <code>true</code> if this model is ready to use
     */
    public boolean isInitialized(){
        return initialized;
    }

    /**
     * (Re-)initializes this project.
     * @param forceClear if set to <code>true</code> then the cache
     * gets deleted in any case. Otherwise the cache gets only deleted if
     * this is not the first initialization.
     */
    public void startInitialize( final boolean forceClear ){
        Runnable initialize = new Runnable() {
            public void run() {
                if( project.isInitializeable() ){
                    stopInitialize( false );

                    if( project.getProject().isAccessible() && !project.isOnStop() ){
                        initializeJob = new ModelInitializeJob( startup, forceClear );
                        startup = false;
                        initializeJob.schedule( 100 );
                    }
                }
            }
        };

        IEnvironment environment = project.getEnvironment();
        if( environment == null ){
            initialize.run();
        }
        else{
            environment.runAfterStartup( initialize );
        }
    }

    public void ensureInitialize(){
        if( initializeJob == null )
            startInitialize( false );
    }

    public void stopInitialize( boolean wait ){
        Job initializeJob = this.initializeJob;

        if( initializeJob != null ){
            initializeJob.cancel();

            if( wait ){
                while( initializeJob.getState() != Job.NONE ){
                    try{
                        initializeJob.join();
                    }
                    catch ( InterruptedException e ){
                        // ignore
                    }
                }
            }
        }
    }


    /**
     * Makes a search for a file with the given name. Note that this is a
     * resource intensive search through several folders and should not be
     * performed often.
     * @param name the name of the file
     * @param monitor to report progress
     * @return its parse file or <code>null</code>
     */
    public IParseFile parseFile( String name, IProgressMonitor monitor ){
    	if( monitor == null )
    		monitor = new NullProgressMonitor();
    	
        monitor.beginTask( "Locate file '" + name + "'", 1000 );
        TinyOSPlugin plugin = TinyOSPlugin.getDefault();
        if( plugin == null ){
            monitor.done();
            return null;
        }

        File file = plugin.locate( name, false, new SubProgressMonitor( monitor, 1000 ) );
        monitor.done();

        if( file == null || monitor.isCanceled() )
            return null;

        return parseFile( file );
    }

    /**
     * Creates the key that identifies <code>file</code>.
     * @param file some file
     * @return the key for <code>file</code>
     */
    public IParseFile parseFile( File file ){
        return fileModel.parseFile( file );
    }

    /**
     * Creates the key that identifies the file or directory <code>resource</code>.
     * @param resource some file or directory
     * @return the key for <code>resource</code>
     */
    public IParseFile parseFile( IResource resource ){
        IPath path = resource.getLocation();
        if( path == null )
            return null;

        File file = path.toFile();
        if( file == null )
            return null;

        return parseFile( file );
    }

    /**
     * Tries to find the resource which would be translated into <code>parseFile</code>.
     * @param parseFile some file to find
     * @return the resource behind that file
     */
    public IFile resource( IParseFile parseFile ){
        if( !parseFile.isProjectFile() )
            return null;

        File file = parseFile.toFile();
        IPath projectPath = project.getProject().getLocation();
        if( projectPath == null )
            return null;

        IPath filePath = Path.fromOSString( file.getAbsolutePath() );
        if( !projectPath.isPrefixOf( filePath ))
            return null;

        filePath = filePath.removeFirstSegments( projectPath.segmentCount() );
        return project.getProject().getFile( filePath );
    }
    
    /**
     * Creates or reads an {@link IASTModel} which contains the nodes of <code>parseFile</code>.
     * @param parseFile the file which might be parsed or whose cache is read
     * @param fullLoad whether to force the model to contain all nodes or allow 
     * to local nodes to be missing
     * @param monitor to report progress
     * @return the model or <code>null</code> if <code>monitor</code> is canceled
     */
    public IASTModel getModel( IParseFile parseFile, boolean fullLoad, IProgressMonitor monitor ){
    	if( monitor == null )
    		monitor = new NullProgressMonitor();
    	
    	Monitor lock = enter();
    	try{
    		ensureInCache( parseFile, fullLoad, monitor );
    		
	    	if( monitor.isCanceled() ){
	    		monitor.done();
	    		return null;
	    	}
	    	
	    	IASTModelNode[] nodes = getAstModel().getNodes( ASTNodeFilterFactory.origin( parseFile ) );
	    	IASTModel model = newASTModel();
	    	if( nodes != null ){
	    		model.addNodes( nodes );
	    	}
	    	
	    	return model;
    	}
    	finally{
    		melt( parseFile );
    		lock.leave();
    	}
    }

    /**
     * This method ensures that the current version of <code>parseFile</code>
     * is in the {@link #getCacheModel() cache}.
     * @param parseFile the file that needs to be in the cache
     * @param fullLoad whether the file must be fully loaded
     * @param monitor to report progress or abort the operation
     */
    public void ensureInCache( IParseFile parseFile, boolean fullLoad, IProgressMonitor monitor ){
    	if( monitor == null )
    		monitor = new NullProgressMonitor();
    	
    	Monitor lock = enter();
    	try{
    		monitor.beginTask( "Ensure Model", 1 );
    		freeze( parseFile );
    	
    		boolean loaded = false;
    		
	    	// check loaded nodes
	    	ASTContent content = getCacheContent( parseFile );
	    	if( content != null ){
	    		if( content.isFullLoaded() || !fullLoad ){
	    			loaded = true;
	    		}
	    	}
	    	
	    	if( !loaded ){
	    		updateModel( parseFile, fullLoad, true, new SubProgressMonitor( monitor, 1 ));
	    	}
    	
	    	monitor.done();
    	}
    	finally{
    		melt( parseFile );
    		lock.leave();
    	}
    }
    
    /**
     * Updates the {@link #getCacheModel() cache} such that the file 
     * <code>parseFile</code> is included. If it is already there, then it will
     * not be parsed again.<br>
     * Clients should call this method only if they intend to use the result 
     * of the call soon, they might {@link #freeze(IParseFile)} and {@link #melt(IParseFile)}
     * the file before updating the model, and after using it. If a client only 
     * works within one file, then using the {@link #newParser(IParseFile, IMultiReader, IProgressMonitor) parser} with
     * a fresh and empty {@link #newASTModel() model} is often better. Ambiguous
     * references are less likely to occur with a new model.
     * @param monitor used to inform the user about the state and to cancel
     * parsing
     * @param parseFile the file that should be in the model
     * @param allowCacheRead if set, then this model might read the contents 
     * of the file from a backup cache
     * @param fullLoad whether to make a full load of the file or not
     */
    private void updateModel( IParseFile parseFile, boolean fullLoad, boolean allowCacheRead, IProgressMonitor monitor ){
        Monitor lock = enter();
    	monitor = enter( monitor, "Update Model: " + parseFile.getPath(), 2500 );
    	ASTContent content = getCacheContent( parseFile );

        if( content != null ){
            if( content.isFullLoaded() || !fullLoad ){
                monitor.done();
                lock.leave();
                return;
            }
        }

        try{
            fullFreeze();

            boolean loadedFromCache = false;
            IASTModelFileCache cache = fileModel.getASTModelCache();

            if( allowCacheRead ){
                if( cache != null && cache.canReadCache( parseFile )){
                    if( !fullLoad || cache.isFullyLoaded( parseFile )){
                        try{
                            SubASTModel subModel = cache.readCache( parseFile, new SubProgressMonitor( monitor, 500 ) );
                            if( monitor.isCanceled() ){
                                monitor.done();
                                return;
                            }
                            loadedFromCache = true;
                            prepareForModelUpdate( parseFile, subModel.isFullyLoaded() );
                            getAstModel().addNodes( subModel.getNodes() );
                        }
                        catch( IOException e ){
                            TinyOSPlugin.warning( e );
                        }
                        catch ( CoreException e ){
                            TinyOSPlugin.warning( e.getStatus() );
                        }
                    }
                }
            }

            if( !loadedFromCache ){
                prepareForModelUpdate( parseFile, fullLoad );

                File file = parseFile.toFile();
                if( file != null && file.exists() && file.canRead() ){
                    try {
                        INesCParser parser = newParser( parseFile, null, new SubProgressMonitor( monitor, 500 ) );
                        if( monitor.isCanceled() )
                            return;

                        parser.setASTModel( getAstModel() );
                        parser.setFollowIncludes( true );
                        parser.setResolveFullModel( fullLoad );

                        parser.parse( new FileMultiReader( file ), new SubProgressMonitor( monitor, 1000 ) );
                    }
                    catch( IOException e ) {
                        TinyOSPlugin.warning( e );
                    }
                }

                if( cache != null ){
                    IASTModelNode[] nodes = getAstModel().getNodes( ASTNodeFilterFactory.origin( parseFile ) );
                    if( nodes == null )
                        nodes = new IASTModelNode[]{};

                    try{
                        cache.writeCache( parseFile, new SubASTModel( fullLoad, nodes ), new SubProgressMonitor( monitor, 500 ));
                    }
                    catch ( IOException e ){
                        // can't do anything
                        e.printStackTrace();
                    }
                    catch ( CoreException e ){
                        TinyOSPlugin.warning( e.getStatus() );
                    }
                }
            }
        }
        finally{
            fullMelt();
            monitor.done();
            lock.leave();
        }
    }

    private ASTContent getCacheContent( IParseFile parseFile ){
        for( ASTContent content : astModelContent ){
            if( content.getParseFile().equals( parseFile ))
                return content;
        }
        return null;
    }

    /**
     * Tells whether <code>parseFile</code> includes <code>includedParseFile</code>
     * directly or indirectly. This method will be only valid after a full
     * build of the whole project.
     * @param parseFile some file
     * @param includedParseFile the file that may be included
     * @return <code>true</code> if <code>parseFile</code> includes <code>includedParseFile</code>
     */
    public boolean isIncluded( IParseFile parseFile, IParseFile includedParseFile ){
        return definitionCollection.includes( parseFile, includedParseFile );
    }

    /**
     * Opens a reader to read the file <code>parseFile</code>. Clients must
     * call {@link Reader#close()} when they are finished reading.
     * @param parseFile the file to read
     * @return the reader
     * @throws IOException if the file can't be opened
     */
    public Reader openReader( String parseFile ) throws IOException{
        File file = new File( parseFile );
        return new BufferedReader( new FileReader( file ));
    }

    /**
     * Ensures that this model knows all accessible files
     * @param monitor to report progress
     */
    public void updateFileModel( IProgressMonitor monitor ){
        Monitor lock = enter();
        try{
        	fileModel.refresh( project.getMakeTarget(), monitor );
        }
        finally{
        	lock.leave();
        }
    }

    /**
     * Parses <code>file</code> again and stores all the declarations that were found.
     * @param file the file to parse
     * @param monitor used to inform the user about the state and to cancel
     * parsing
     * @throws IOException if the file can't be read
     */
    public void update( File file, IProgressMonitor monitor ) throws IOException{
        update( file, false, monitor );
    }

    /**
     * Parses <code>file</code> again and stores all the declarations that were found.
     * @param file the file to parse
     * @param continuous if set, then listeners are informed that there are
     * more changes to expect, {@link #informContinuousFinished()} should be
     * called after all changes were applied
     * @param monitor used to inform the user about the state and to cancel
     * parsing
     * @throws IOException if the file can't be read
     */
    public void update( File file, boolean continuous, IProgressMonitor monitor ) throws IOException{
        Monitor lock = enter();
    	try{
    		FileMultiReader reader = new FileMultiReader( file );
    		update( parseFile( file ), reader, continuous, monitor );
    	}
    	finally{
    		lock.leave();
    	}
    }

    /**
     * Parses the contents of <code>reader</code> again and stores all the
     * declarations that were found. This method assumes that the file
     * belongs to a project.
     * @param filename the name of the file that gets parsed
     * @param reader the reader that contains the contents of the file
     * @param monitor used to inform the user about the state and to cancel
     * parsing
     * @throws IOException if the file can't be read
     */
    public void update( IParseFile filename, IMultiReader reader, IProgressMonitor monitor ) throws IOException{
        update( filename, reader, false, monitor );
    }

    /**
     * Parses the contents of <code>reader</code> again and stores all the
     * declarations that were found. This method assumes that the file
     * belongs to a project.
     * @param filename the name of the file that gets parsed
     * @param reader the reader that contains the contents of the file
     * @param continuous if set, then listeners will be informed that more changes
     * are to be expected, and {@link #informContinuousFinished()} should
     * be called
     * @param monitor used to inform the user about the state and to cancel
     * parsing
     * @throws IOException if the file can't be read
     */
    public void update( IParseFile filename, IMultiReader reader, boolean continuous, IProgressMonitor monitor ) throws IOException{
        monitor = enter( monitor, "Update File: '" + filename.getName() + "'", 2000 );
        Monitor lock = enter();
        try{
            Debug.info( "update file: " + filename.getPath() );

            INesCParser parser = newParser( filename, reader, new SubProgressMonitor( monitor, 1000 ) );
            if( monitor.isCanceled() )
                return;

            parser.setFollowIncludes( true );
            parser.setCreateDeclarations( true );

            parser.setResolveFullModel( false );
            prepareForModelUpdate( filename, false );

            parser.setASTModel( getAstModel() );

            declarations.remove( filename );

            parser.parse( reader, new SubProgressMonitor( monitor, 1000 ) );
            if( monitor.isCanceled() )
                return;

            IDeclaration[] result = parser.getDeclarations();
            if( result != null ){
                declarations.put( filename, new Cache( filename, result ));
            }

            if( continuous && initialized )
                changedFiles.add( filename );

            for( IProjectModelListener listener : listeners() )
                listener.changed( filename, continuous );
        }
        finally{
            monitor.done();
            lock.leave();
        }
    }

    public void buildMark( IParseFile parseFile, boolean changed ){
        IFile file = resource( parseFile );
        if( file != null ){
            buildMark( file, changed );
        }
    }

    /**
     * Ensures that <code>resource</code> is no longer marked as being built.
     * As a result every query to {@link #checkBuild(IResource, IProgressMonitor)} will
     * answer that <code>resource</code> needs to be built.
     * @param resource the resource to mark
     * @param changed whether the resource was changed
     */
    public void buildMark( IResource resource, boolean changed ){
        try{
            if( Debug.DEBUG ){
                Debug.info( "buildMark '" + resource.getName() + "', changed = " + changed );
            }

            resource.setPersistentProperty( LAST_BUILD, null );
            if( changed ){
                resource.setPersistentProperty( RESOURCE_CHANGED, "true" );
            }
        }
        catch ( CoreException e ){
            TinyOSPlugin.warning( e.getStatus() );
        }
    }

    /**
     * Checks whether <code>resource</code> has been marked as changed. A resource
     * which is marked will be built and might trigger other resources to be built
     * as well.
     * @param resource the resource to check
     * @return <code>true</code> if <code>resource</code> is marked as changed
     */
    public boolean checkChange( IResource resource ){
        try{
            if( Debug.DEBUG ){
                Debug.info( "checkChange '" + resource.getName() + "': " + resource.getPersistentProperty( RESOURCE_CHANGED ) );
            }

            return "true".equals( resource.getPersistentProperty( RESOURCE_CHANGED ) );
        }
        catch ( CoreException e ){
            TinyOSPlugin.warning( e.getStatus() );
            return false;
        }
    }

    /**
     * Tries to find out whether <code>resource</code> is in a legal state
     * or should be built again.
     * @param resource the resource to check
     * @param monitor to cancel the operation
     * @return <code>true</code> if <code>resource</code> should be built again,
     * <code>false</code> if everything is just fine with <code>resource</code>
     */
    public boolean checkBuild( IResource resource, IProgressMonitor monitor ){
        monitor.beginTask( "Check '" + resource.getName() + "'", 1000 );

        try{
            if( !resource.exists() ){
                if( Debug.DEBUG ){
                    Debug.info( "checkBuild: '" + resource.getName() + "': false, resource does not exist" );
                }
                return false; // if the resource is not there, then it can't be built
            }

            // check the timestamp of the last built
            String lastBuildString = resource.getPersistentProperty( LAST_BUILD );
            if( lastBuildString == null ){
                if( Debug.DEBUG ){
                    Debug.info( "checkBuild: '" + resource.getName() + "': true, no last build" );
                }
                return true;
            }
            long lastBuildValue = Long.valueOf( lastBuildString );
            if( lastBuildValue != resource.getModificationStamp() ){
                if( Debug.DEBUG ){
                    Debug.info( "checkBuild: '" + resource.getName() + "': true, modification stamp does not match" );
                }
                return true;
            }

            // check whether a missing resource was found
            IParseFile file = parseFile( resource );
            if( file == null ){
                if( Debug.DEBUG ){
                    Debug.info( "checkBuild: '" + resource.getName() + "': false, file of resource does not exist" );
                }

                return false; // the resource does not exist... no hope to build it
            }

            IFileCache<IMissingResource[]> cache = fileModel.getMissingFileCache();
            if( !cache.canReadCache( file )){
                if( Debug.DEBUG ){
                    Debug.info( "checkBuild: '" + resource.getName() + "': true, missing file cache cannot be read" );
                }

                return true;
            }

            IMissingResource[] missing = cache.readCache( file, new SubProgressMonitor( monitor, 200 ) );

            if( monitor.isCanceled() ){
                if( Debug.DEBUG ){
                    Debug.info( "checkBuild: '" + resource.getName() + "': false, canceled" );
                }

                return false;
            }

            IProgressMonitor subMonitor = new SubProgressMonitor( monitor, missing.length );
            for( IMissingResource check : missing ){
                boolean available = check.checkAvailable( project, file, new SubProgressMonitor( subMonitor, 1 ) );
                if( available ){
                    if( Debug.DEBUG ){
                        Debug.info( "checkBuild: '" + resource.getName() + "': true, missing resource has become available" );
                    }

                    return true;
                }
            }
        }
        catch( IOException e ){
            if( Debug.DEBUG ){
                Debug.info( "checkBuild: '" + resource.getName() + "': true, io exception " + e.getMessage() );
            }

            TinyOSPlugin.warning( e );
            return true;
        }
        catch( CoreException e ){
            if( Debug.DEBUG ){
                Debug.info( "checkBuild: '" + resource.getName() + "': true, core exception " + e.getMessage() );
            }

            TinyOSPlugin.warning( e.getStatus() );
            return true;
        }
        finally{
            monitor.done();
        }

        if( Debug.DEBUG ){
            Debug.info( "checkBuild: '" + resource.getName() + "': false, all tests passed" );
        }
        return false;
    }

    /**
     * Called by the build system to inform the model that a file was changed.
     * The file needs to be reinitialized in order to guarantee that the global
     * index becomes valid again.
     * @param resource the changed file
     * @param monitor used to inform the user of progress and to cancel this operation
     * @throws IOException if the file can't be accessed
     */
    public void buildInit( IResource resource, IProgressMonitor monitor ) throws IOException{
        if( monitor == null )
            monitor = new NullProgressMonitor();

        Monitor lock = enter();
        try{
            monitor.beginTask( "Init", 1000 );

            IPath location = resource.getLocation();
            if( location == null )
                return;

            File file = location.toFile();
            IParseFile filename = parseFile( file );

            Debug.info( "init file: " + filename.getPath() );

            deleteCache( filename, true, new SubProgressMonitor( monitor, 200 ) );

            INesCInitializer initializer = TinyOSPlugin.getDefault().getParserFactory().createInitializer( project.getProject() );
            IMultiReader reader = new FileMultiReader( file );

            IMacro[] macros = getBasicDeclarations().listBasicMacros();
            if( macros != null ){
                for( IMacro macro : macros ){
                    initializer.addMacro( macro );
                }
            }
            
            IDeclaration[] result = initializer.analyze( filename, reader, new SubProgressMonitor( monitor, 600 ) );
            if( monitor.isCanceled() )
                return;

            declarations.remove( filename );

            if( result != null ){
                declarations.put( filename, new Cache( filename, result ));
            }

            if( monitor.isCanceled() )
                return;

            TinyOSPlugin plugin = TinyOSPlugin.getDefault();
            if( plugin != null ){
                try{
                    IFileCache<IDeclaration[]> cache = fileModel.getInitCache();
                    if( cache != null ){
                        cache.writeCache( filename, result, new SubProgressMonitor( monitor, 200 ) );
                    }
                }
                catch ( CoreException e ){
                    // the cache was not written, but that should be
                    // obvious when loading the cache the next time
                    TinyOSPlugin.warning( e.getStatus() );
                }
            }
        }
        finally{
            monitor.done();
            lock.leave();
        }
    }

    /**
     * Called by the build system after all changed files were reported through
     * {@link #buildInit(IResource, IProgressMonitor)} to actually parse and analyze the
     * resource.
     * @param resource the changed file
     * @param monitor progress monitor used to inform the user about changes
     * or to cancel parsing
     * @throws IOException if the file can't be accessed
     */
    public void buildUpdate( IResource resource, IProgressMonitor monitor ) throws IOException{
        if( Debug.DEBUG ){
            Debug.info( "buildUpdate (1) '" + resource.getName() + "'" );
        }
        monitor = enter( monitor, "Build", 2000 );
        Monitor lock = enter();
        try{
        	// setup parser
            IPath location = resource.getLocation();
            if( location == null ){
                if( Debug.DEBUG ){
                    Debug.info( "buildUpdate (e) '" + resource.getName() + "': missing location" );
                }

                return;
            }

            File file = location.toFile();
            IParseFile filename = parseFile( file );
            IMultiReader reader = new FileMultiReader( file );

            Debug.info( "build file: " + filename.getPath() );

            INesCParser parser = newParser( filename, reader, new SubProgressMonitor( monitor, 900 ) );
            if( monitor.isCanceled() )
                return;

            MissingResourceRecorder missing = new MissingResourceRecorder();

            parser.setFollowIncludes( true );
            parser.setCreateDeclarations( true );
            parser.setCreateMessages( true );
            parser.setMissingResourceRecorder( missing );
            parser.setCreateReferences( true );

            declarations.remove( filename );

            // parse
            
            parser.parse( reader, new SubProgressMonitor( monitor, 1000 ) );
            if( monitor.isCanceled() )
                return;

            // read and store results
            
            IDeclaration[] result = parser.getDeclarations();
            if( result != null ){
                declarations.put( filename, new Cache( filename, result ));
            }
            try{
				fileModel.getInclusionCache().writeCache( filename, result, new SubProgressMonitor( monitor, 50 ) );
			}
			catch( CoreException e ){
				TinyOSPlugin.warning( e.getStatus() );
			}

            IMissingResource[] missingResources = missing.getMissingResources();
            try{
                fileModel.getMissingFileCache().writeCache( filename, missingResources, new SubProgressMonitor( monitor, 50 ) );
            }
            catch ( CoreException e ){
                TinyOSPlugin.warning( e.getStatus() );
            }

            IMessage[] messages = parser.getMessages();
            try {
                if( messages == null )
                    messages = new IMessage[]{};

                NesCProblemMarker.synchronizeMessages( resource, filename, messages );

                if( Debug.DEBUG ){
                    Debug.info( "buildUpdate (2) '" + resource.getName() + "', " + resource.getModificationStamp() );
                }
                resource.setPersistentProperty( LAST_BUILD, String.valueOf( resource.getModificationStamp() ));
                resource.setPersistentProperty( RESOURCE_CHANGED, "false" );
            }
            catch( CoreException e ) {
                TinyOSPlugin.warning( e.getStatus() );
            }

            IASTReference[] references = parser.getReferences();
            try{
				fileModel.getReferencesCache().writeCache( filename, references, new SubProgressMonitor( monitor, 50 ) );
			}
			catch( CoreException e ){
				TinyOSPlugin.warning( e );
			}
            
            if( initialized )
                changedFiles.add( filename );

            for( IProjectModelListener listener : listeners() )
                listener.changed( filename, true );
        }
        finally{
            monitor.done();
            lock.leave();
        }
    }

    /**
     * Makes all preparations such that {@link #getAstModel()} can be used
     * in the parser.
     * @param filename the name of the file that gets parsed
     * @param fullLoad whether a full load will be performed
     */
    private void prepareForModelUpdate( IParseFile filename, boolean fullLoad ){
        removeFromModel( filename );

        Debug.info( "add to cache: " + filename );
        astModelContent.add( new ASTContent( filename, fullLoad ));
    }

    /**
     * Removes all nodes from the file <code>filename</code> out of the
     * {@link #getAstModel()}
     * @param filename the name of the file
     */
    private void removeFromModel( IParseFile filename ){
    	Monitor monitor = enter();
    	try{
	        ASTContent content = getCacheContent( filename );
	        if( content != null ){
	            astModelContent.remove( content );
	            getAstModel().removeNodes( filename );
	        }
	
	        shrinkCache();
    	}
    	finally{
    		monitor.leave();
    	}
    }

    private IProgressMonitor enter( IProgressMonitor monitor, String task, int ticks ){
        if( monitor == null )
            monitor = new NullProgressMonitor();

        monitor.beginTask( task, ticks );
        monitor.setTaskName( task );
        return monitor;
    }

    /**
     * Freezes the ast model of <code>parseFile</code>, that will prevent
     * the ast of <code>parseFile</code> from unloading, but not from updates.<br>
     * Note: it is important that for every call to <code>freeze</code>, exactly
     * one call to {@link #melt(IParseFile)} is made as well.
     * @param parseFile the name of the file whose model should be frozen
     * @see #melt(IParseFile)
     */
    public void freeze( IParseFile parseFile ){
    	Monitor monitor = enter();
    	try{
	        Integer value = frozen.get( parseFile );
	        if( value != null )
	            frozen.put( parseFile, value+1 );
	        else{
	            frozen.put( parseFile, 1 );
	        }
    	}
    	finally{
    		monitor.leave();
    	}
    }

    /**
     * Frees an ast model to be deleted when necessary.
     * @param parseFile the file whose model gets freed
     * @see #freeze(IParseFile)
     */
    public void melt( IParseFile parseFile ){
        Monitor monitor = enter();
        try{
	        Integer value = frozen.get( parseFile );
	        if( value != null ){
	            if( value.intValue() == 1 ){
	                frozen.remove( parseFile );
	                shrinkCache();
	            }
	            else{
	                frozen.put( parseFile, value-1 );
	            }
	        }
        }
        finally{
        	monitor.leave();
        }
    }

    private void fullFreeze(){
        fullFreeze++;
    }

    private void fullMelt(){
        if( fullFreeze > 0 ){
            fullFreeze--;
            shrinkCache();
        }
    }

    private void shrinkCache(){
        if( fullFreeze == 0 ){
            Iterator<ASTContent> name = astModelContent.iterator();

            while( name.hasNext() && astModelContent.size() > cacheSize ){
                ASTContent next = name.next();
                Integer check = frozen.get( next.getParseFile() );
                if( check == null || check <= 0 ){
                    name.remove();
                    getAstModel().removeNodes( next.getParseFile() );
                }
            }
        }
    }

    /**
     * Deletes the cache of this model, {@link #freeze(IParseFile)} will be ignored.
     * @param full if <code>true</code>, then the full cache will be deleted,
     * otherwise only the files from the project will be deleted
     * @param monitor used for output, can be <code>null</code>
     * @see #secureThread()
     */
    public void deleteProjectCache( final boolean full, IProgressMonitor monitor ){
        Monitor lock = enter();
        try{
	        if( monitor == null )
	            monitor = new NullProgressMonitor();
	
	        monitor.beginTask( "clean", 40 );
	
	        getAstModel().clear();
	        astModelContent.clear();
	
	        monitor.worked( 10 );
	
	        fileModel.clear( full, new SubProgressMonitor( monitor, 10 ) );
	
	        if( full ){
	            declarations.clear();
	            basicDeclarations = new BasicDeclarationSet( this );
	        }
	        else{
	            Iterator<Cache> iter = declarations.values().iterator();
	            while( iter.hasNext() ){
	                if( iter.next().file.isProjectFile() )
	                    iter.remove();
	            }
	        }
	
	        monitor.worked( 10 );
	
	        definitionCollection.deleteCache( new SubProgressMonitor( monitor, 10 ) );
	
	        monitor.done();
        }
        finally{
        	lock.leave();
        }
    }

    public void deleteCache( IParseFile parseFile, boolean continuous, IProgressMonitor monitor ){
        declarations.remove( parseFile );
        definitionCollection.deleteCache( parseFile, monitor );
        removeFromModel( parseFile );

        if( continuous && initialized )
            changedFiles.add( parseFile );

        for( IProjectModelListener listener : listeners() )
            listener.changed( parseFile, continuous );
    }

    /**
     * Gets the number of files whose model can be loaded 
     * @return the maximal number of loaded files
     */
    public int getCacheSize(){
        return cacheSize;
    }

    /**
     * Sets the number of files whose model can be loaded
     * @param cacheSize the maximal number of loaded files
     */
    public void setCacheSize( int cacheSize ){
        this.cacheSize = cacheSize;
    }

    /**
     * Gets the files which are currently in the cache.
     * @return the files
     */
    public IParseFile[] getCachedFiles(){
        Monitor monitor = enter();
        try{
	        IParseFile[] files = new IParseFile[ astModelContent.size() ];
	        int index = 0;
	
	        for( ASTContent content : astModelContent ){
	            files[ index++ ] = content.getParseFile();
	        }
	
	        return files;
        }
        finally{
        	monitor.leave();
        }
    }

    /**
     * Tells which files are currently fully loaded in the cache, this method
     * should be used together with {@link #getCachedFiles()}.
     * @return tells for each file wether it is fully loaded or not
     */
    public boolean[] areCachedFilesFullLoaded(){
        Monitor monitor = enter();
        try{
	        boolean[] files = new boolean[ astModelContent.size() ];
	        int index = 0;
	
	        for( ASTContent content : astModelContent ){
	            files[ index++ ] = content.isFullLoaded();
	        }
	
	        return files;
        }
        finally{
        	monitor.leave();
        }
    }

    /**
     * Executes <code>job</code> such that {@link #secureThread()} returns <code>true</code>
     * from within <code>job</code>. This method blocks until <code>job</code>
     * has finished its execution. This method ensures that no other thread can
     * modify this model until <code>job</code> finished its work.
     * 
     * @param job the job to run
     * @param monitor to report progress
     * @return the  status of the job after it has run
     */
    public <J extends Job & IPublicJob> IStatus runJob( final J job, IProgressMonitor monitor ){
    	if( secureThread() ){
    		try{
    			secureThread.lock();
    			return job.run( monitor );
    		}
    		finally{
    			secureThread.unlock();
    		}
        }
        else{
        	boolean locked = false;
    		try{
    			if( monitor == null )
    				monitor = new NullProgressMonitor();
    			
    			monitor.beginTask( "Run Secure", 10 );
    			try{
    				Job.getJobManager().beginRule( project.getProject(), new SubProgressMonitor( monitor,1 ) );
    			}
    			catch( IllegalArgumentException ex ){
    				/*
    				 *  Very nice: this method is called from a thread that
    				 *  already has a lock on a file or folder of this project.
    				 *  
    				 *  Since UIJobs with the project-scheduling-rule can introduce
    				 *  unnecessary deadlocks, they must run without scheduling rule.
    				 *  
    				 *  The idea would be that the lock is acquired in this method,
    				 *  preventing deadlock situations by always following the same
    				 *  order when locking (first the ui, then the project).
    				 *  
    				 *  Unfortunately SWT does not play along. ui-Jobs 
    				 *  (or classes that behave like them) can get mixed into
    				 *  each other and the locking mechanism fails. 
    				 *  
    				 *  While not happening very often, it happens *too* often
    				 *  to just cancel the whole operation.
    				 *  
    				 *  Hence this method features a fallback-locking mechanism
    				 *  involving a traditional Lock.
    				 *  
    				 *  Most of the operations in this plugin call this method to
    				 *  prevent any race conditions, but external plugins might
    				 *  interfear.
    				 */
    				TinyOSPlugin.log( new Status( IStatus.WARNING, TinyOSPlugin.PLUGIN_ID, 
    						"Unable to acquire lock '" + project.getProject() + "', falling back to unsecure locking mechanism and continue operation." ) );
    			}
    			
    			secureThread.lock();
    			locked = true;
    			
    			IStatus result = job.run( new SubProgressMonitor( monitor, 9 ) );
    			monitor.done();
    			return result;
    		}
    		finally{
    			if( locked ){
    				secureThread.unlock();
    			}
    			
    			Job.getJobManager().endRule( project.getProject() );
    		}
    	}
    }
    public boolean secureThread(){
    	if( secureThread.isHeldByCurrentThread() )
    		return true;
    	return threadOwnsProject();
    }

    private boolean secureThreadLocked(){
    	return secureThread.isHeldByCurrentThread();
    }
    
    private boolean threadOwnsProject(){
    	Job job = Job.getJobManager().currentJob();
        if( job == null )
            return false;

        if( job.getRule() == null )
            return false;

        if( !job.getRule().contains( project.getProject() ))
            return false;

        return true;    	
    }
    
    private Monitor enter(){
    	Monitor monitor = new Monitor();
    	monitor.enter();
    	return monitor;
    }
    
    private class Monitor{
    	private boolean secureThreadLocked;
    	
    	public void enter(){
    		secureThreadLocked = secureThreadLocked();
    		if( !secureThreadLocked ){
    			if( threadOwnsProject() ){
    				secureThread.lock();
    			}
    			else{
    				throw new IllegalStateException( 
    						"this method must be accessed through a job whose rule includes the project, or which runs in a secure environment" );	
    			}
    		}
    	}
    	
    	public void leave(){
    		if( !secureThreadLocked ){
    			secureThread.unlock();
    		}
    	}
    }

    private class ModelInitializeJob extends InitializeJob{
        public ModelInitializeJob( boolean startup, boolean forceClear ){
            super( ProjectModel.this, startup, forceClear );
        }

        @Override
        protected void fireInitialized(){
            for( IProjectModelListener listener : listeners() )
                listener.initialized();
        }

        @Override
        protected IFileModel getFileModel(){
            return fileModel;
        }

        @Override
        protected IProjectDefinitionCollector getDefinitionCollector(){
            return definitionCollection;
        }

        @Override
        protected void put( IParseFile file, IDeclaration[] declarations ){
            ProjectModel.this.declarations.put( file, new Cache( file, declarations ) );
        }

        @Override
        protected void setInitialized( boolean initialized ){
            synchronized( ProjectModel.this ){
                ProjectModel.this.initialized = initialized;
            }
        }
    }

    public static interface DeclarationFilter{
        public boolean include( IDeclaration declaration );
    }

    private static class Cache{
        /** declarations found in the file */
        public IDeclarationCollection declarations;
        /** whether the file belongs to a project */
        public IParseFile file;

        public Cache( IParseFile file, IDeclaration[] declarations ){
            this.file = file;
            this.declarations = toCollection( declarations );
        }
    }

    /**
     * Describes one entry in the {@link IASTModel}-cache of a {@link ProjectModel}.
     */
    private static class ASTContent{
        private IParseFile parseFile;
        private boolean fullLoaded = false;

        public ASTContent( IParseFile parseFile, boolean fullLoad ){
            if( parseFile == null )
                throw new IllegalArgumentException( "parse file must not be null" );
            this.parseFile = parseFile;
            this.fullLoaded = fullLoad;
        }

        public IParseFile getParseFile(){
            return parseFile;
        }

        public boolean isFullLoaded(){
            return fullLoaded;
        }
    }
}
