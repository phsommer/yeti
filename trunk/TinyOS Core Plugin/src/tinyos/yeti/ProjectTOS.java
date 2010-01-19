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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.builder.TinyOSBuilder;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.make.IMakeTargetListener;
import tinyos.yeti.make.IProjectMakeTargets;
import tinyos.yeti.make.MakeExclude;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.MakeTargetEvent;
import tinyos.yeti.make.MultiMakeExclude;
import tinyos.yeti.make.targets.IMakeTargetMorpheable;
import tinyos.yeti.model.INesCPathListener;
import tinyos.yeti.model.IProjectCache;
import tinyos.yeti.model.NesCPath;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nesc.IMultiReader;
import tinyos.yeti.utility.ProjectTOSUtility;

public class ProjectTOS implements IAdaptable{
    public static final String MAKEFILE_NAME = "TinyOS_Plugin_Makefile";
    private static final QualifiedName CACHE_STRATEGY = new QualifiedName( TinyOSPlugin.PLUGIN_ID, "project_cache_strategy" );

    private ProjectModel model;
    private IProject project;
    private ProjectManager projects;
    
    private TinyOSBuilder builder;

    private volatile boolean initializeable = true;

    private volatile boolean onStop = false;

    private MakeTarget makeTarget;
        
    private IMakeTargetListener targetListener = new IMakeTargetListener(){
    	public void targetChanged( MakeTargetEvent event ){
    		if( event.getType() == MakeTargetEvent.PROJECT_REMOVED ){
    			return;
    		}
    		
    		if( event.getProject() == null || event.getProject() == ProjectTOS.this.project ){
    			IProjectMakeTargets targets = getMakeTargets();
    			IMakeTargetMorpheable morph = targets.getSelectedTarget();
    			MakeTarget target = morph == null ? null : morph.toMakeTarget();
    			
    			if( target != null  ){
    				if( makeTarget == null ){
    					makeTarget = new MakeTarget( ProjectTOS.this.project, "null", null );
    					makeTarget.copyFull( target );
    					model.startInitialize( true );
    				}
    				else{
    					if( makeTarget.copyTriggersUpdate( target )){
    						makeTarget.copyFull( target );
    						model.startInitialize( true );
    					}
    					else{
    						makeTarget.copyFull( target );
    					}
    				}
    			}
    		}
    	}
    };

    private IResourceChangeListener resourceListener = new IResourceChangeListener(){
    	public void resourceChanged( IResourceChangeEvent event ){
    		if( event.getResource() == getProject() ){
    			// this project gets closed or deleted, stop anything
    			try{
    				onStop = true;
    				model.stopInitialize( true );
    				getBuilder().cancelBuild( true );
    			}
    			finally{
    				onStop = false;
    			}
    			if( event.getType() == IResourceChangeEvent.PRE_DELETE ){
    				release();
    			}
    		}
    	}
    };
    
    public ProjectTOS( ProjectManager projects ){
    	this.projects = projects;
    }
    
    @SuppressWarnings("unchecked")
	public Object getAdapter( Class adapter ){
	    if( adapter.equals( IProject.class ))
	    	return getProject();
	    if( adapter.equals( IResource.class ))
	    	return getProject();
	    if( adapter.equals( ProjectModel.class ))
	    	return getModel();
	    if( adapter.equals( MakeTarget.class ))
	    	return getMakeTarget();
	    
	    return project.getAdapter( adapter );
    }
    
    public void init( IProject project, boolean initialize ) {
    	this.project = project;
    	
    	if( project.isOpen() ){
    		ProjectTOSUtility.updateNature( project );
    	}
    	else{
    		ResourcesPlugin.getWorkspace().addResourceChangeListener( new PendingUpdate() );
    	}
    	
        model = new ProjectModel( this );
        builder = new TinyOSBuilder( model );

        IMakeTargetMorpheable morph = TinyOSPlugin.getDefault().getTargetManager().getSelectedTarget( project );
        if( morph != null ){
        	makeTarget = new MakeTarget( this.project, "null", null );
        	makeTarget.copyFull( morph.toMakeTarget() );
        }

        TinyOSPlugin.getDefault().addMakeTargetListener( targetListener );

        initializeable = initialize;

        if( initialize ){
            model.startInitialize( false );
        }
        
        project.getWorkspace().addResourceChangeListener( resourceListener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE );
        
        getPaths().addListener( new INesCPathListener(){
        	public void sourceFoldersChanged( NesCPath path ){
        		model.startInitialize( true );
        	}
        });
    }
    
    public String getCacheStrategy(){
    	try{
    		return project.getPersistentProperty( CACHE_STRATEGY );
		}
		catch( CoreException e ){
			TinyOSPlugin.log( e );
			return null;
		}
    }
    
    /**
     * Stores the identifier <code>strategy</code> used for resolving the 
     * current {@link IProjectCache}.
     * @param strategy the strategy
     */
    public void setCacheStrategy( String strategy ){
    	try{
			project.setPersistentProperty( CACHE_STRATEGY, strategy );
		}
		catch( CoreException e ){
			TinyOSPlugin.log( e );
		}
    }
    
    private void release(){
    	TinyOSPlugin.getDefault().removeMakeTargetListener( targetListener );
    	project.getWorkspace().removeResourceChangeListener( resourceListener );
    }
    
    /**
     * Whether this model can start initialization.
     * @return <code>true</code> if initialization is enabled
     */
    public boolean isInitializeable(){
        return initializeable;
    }

    public void initialize(){
    	initialize( false );
    }
    
    /**
     * Initializes this project, overrides {@link #isInitializeable()}. If this
     * is not the first time this method is called, then the cache gets deleted
     * and the project gets rebuild (assuming auto-build is active).
     * @param forceClear if set to <code>true</code> then the cache
     * gets cleared, otherwise it gets only cleared if this is not the first
     * time initialization is performed
     */
    public void initialize( boolean forceClear ){
        initializeable = true;
        TinyOSPlugin.getDefault().getTargetManager().refresh( project );
        model.startInitialize( forceClear );
    }

    /**
     * Ensures that this project gets initialized sometimes in the future.
     */
    public void ensureInitialize(){
    	TinyOSPlugin.getDefault().getTargetManager().refresh( project );
        model.ensureInitialize();
    }
    
    public boolean isOnStop(){
        return onStop;
    }

    /**
     * Gets the project this {@link ProjectTOS} is built upon.
     * @return the real project
     */
    public IProject getProject() {
        return project;
    }

    /**
     * Gets the build paths of this project.
     * @return the build paths
     */
    public NesCPath getPaths(){
    	return projects.getPaths( project );
	}
    
    /**
     * Gets the containers in which source is stored
     * @return the source containers
     */
    public IFolder[] getSourceContainers(){
    	return getPaths().getSourceFolders();
    }
    
    /**
     * Gets the source folder that was used before multiple source
     * folders could be used.
     * @return the legacy source folder, should only be used to read
     * and convert old settings
     */
    public IFolder getLegacySourceContainer(){
    	return project.getFolder( "src" );
    }
    
    /**
     * Searches the container in which <code>resource</code> is
     * stored.
     * @param resource some resource
     * @return one of its ancestors or <code>null</code>
     */
    public IContainer getSourceContainer( IResource resource ){
    	IContainer[] roots = getSourceContainers();
    	while( resource != null ){
    		for( IContainer check : roots ){
    			if( check.equals( resource )){
	    			return check;
	    		}
    			resource = resource.getParent();
    		}
    	}
    	return null;
    }
    
    /**
     * Searches the container in which the resource with <code>path</code>
     * is stored.
     * @param path a project relative path
     * @return a container which is a parent of <code>path</code>
     */
    public IContainer getSourceContainer( IPath path ){
    	IContainer[] roots = getSourceContainers();
    	for( IContainer root : roots ){
    		IPath rootPath = root.getProjectRelativePath();
    		if( rootPath.isPrefixOf( path )){
    			return root;
    		}
    	}
    	return null;
    }
    
    /**
     * Searches the source file which represents <code>file</code>.
     * @param file some file
     * @return the source file it represents
     */
    public IFile getSource( File file ){
		IPath path = new Path( file.getAbsolutePath() );
		
		for( IContainer root : getSourceContainers() ){
			IPath rootPath = root.getLocation();
			if( rootPath.isPrefixOf( path )){
				IFile resource = root.getFile( path.removeFirstSegments( rootPath.segmentCount() ) );
				return resource;
			}
		}
		
		return null;
    }
    
    public static IFolder getSourceContainer( IProject project ){
    	return project.getFolder( "src" );
    }

    /**
     * Gets the container in which cached intern files are stored.
     * @return the container
     */
    public IFolder getInternBinaryContainer(){
        return getCacheContainer().getFolder( "intern" );
    }

    /**
     * Gets the file which represents the make file
     * @return the make file, may not exist
     */
    public IFile getMakefile(){
        return project.getFile( MAKEFILE_NAME );
    }

    /**
     * Gets the makefile, creates a new makefile if the old is missing.
     * @return the make file
     * @throws CoreException if no file could be created
     */
    public File ensureMakefilePath() throws CoreException{
        IFile file = getMakefile();
        if( !file.exists() ){
            ProjectTOSUtility.createMakefile( this, null );
        }
        IPath path = file.getLocation();
        if( path == null )
            throw new CoreException( new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, "Could not create makefile" ));

        return path.toFile();
    }

    /**
     * Gets the container in which cached extern files are stored.
     * @return the container
     */
    public IFolder getExternBinaryContainer(){
        return getCacheContainer().getFolder( "extern" );
    }

    public IFolder getBuildContainer(){
        return project.getFolder( "build" );
    }

    public IFolder getCacheContainer(){
        return project.getFolder( ".cache" );
    }

    /**
     * Gets the builder that should be used to update the resources of this
     * project.
     * @return the builder, never <code>null</code>
     */
    public TinyOSBuilder getBuilder(){
        return builder;
    }
    
    public void clearCache( boolean full, IProgressMonitor monitor ){
        model.clearCache( full, monitor );
    }

    /**
     * The project model holds all declarations and some ast-elements that
     * are needed in the whole project.
     * @return the model, never <code>null</code>
     */
    public ProjectModel getModel(){
        return model;
    }

    /**
     * Creates a new parser that can be used to process <code>file</code>.
     * @param file the name of the file that will get parsed
     * @param reader the reader for <code>file</code> or <code>null</code>
     * @param monitor to inform about state or to cancel the operation
     * @return the new parser
     * @see #newASTModel()
     */
    public INesCParser newParser( IParseFile file, IMultiReader reader, IProgressMonitor monitor ) throws IOException{
        return model.newParser( file, reader, monitor );
    }

    /**
     * Calls {@link IResourceVisitor#visit(IResource)} for those resources
     * which are in the source folders, and not in an excluded folder.
     * @param visitor the used to visit resources
     * @throws CoreException if a resource cannot be visited
     */
    public void acceptSourceFiles( final IResourceVisitor visitor ) throws CoreException{
        final MakeTarget target = getMakeTarget();
        final MakeExclude[] excludes = target == null ? null : target.getExcludes();

        for( IContainer source : getSourceContainers() ){
        	if( source.isAccessible() ){
		        if( excludes == null || excludes.length == 0 ){
		            source.accept( visitor );
		        }
		        else{
		            source.accept( new IResourceVisitor(){
		            	private MultiMakeExclude exclude = target.getExclude();
		            	
		                public boolean visit( IResource resource ) throws CoreException{
		                    if( resource instanceof IFolder ){
		                        IPath location = resource.getLocation();
		                        if( location == null )
		                            return visitor.visit( resource );
		
		                        if( exclude.shouldExclude( location.toFile() ))
		                            return true;
		
		                        return visitor.visit( resource );
		                    }
		                    else{
		                        IPath location = resource.getParent().getLocation();
		                        if( location == null )
		                            return visitor.visit( resource );
		
		                        if( exclude.shouldExclude( location.toFile() ))
		                            return true;
		
		                        return visitor.visit( resource );
		                    }
		                }
		            });
		        }
        	}
        }
    }

    /**
     * Creates a new empty {@link IASTModel} that can be used together
     * with the current parser.
     * @return the new model
     */
    public IASTModel newASTModel(){
        return model.newASTModel();
    }

    /**
     * Gets the default maketarget of this project.
     * @return the maketarget or <code>null</code>
     */
    public MakeTarget getMakeTarget(){
    	if( makeTarget != null )
            return makeTarget;

        TinyOSPlugin plugin = TinyOSPlugin.getDefault();
        if( plugin == null )
            return null;

        IMakeTargetMorpheable morph = plugin.getTargetManager().getSelectedTarget( project );
        if( morph == null )
        	return null;
        
        return morph.toMakeTarget();
    }

    /**
     * Gets all the {@link MakeTarget}s that are associated with this project.
     * @return all the {@link MakeTarget}s.
     */
    public IProjectMakeTargets getMakeTargets(){
    	return TinyOSPlugin.getDefault().getTargetManager().getProjectTargets( getProject() );
    }
    
    public IEnvironment getEnvironment(){
        return TinyOSPlugin.getDefault().getEnvironments().getEnvironment( project );
    }

    /**
     * Gets all the directories which contain source files.
     * @param target the target for which the files are used
     * @return the name of the source directories
     */
    public String[] getSourceIncludes( final MakeTarget target ){
        final List<String> result = new ArrayList<String>();

        final IEnvironment environment = getEnvironment();
        
        try{
            acceptSourceFiles( new IResourceVisitor(){
            	private MultiMakeExclude exclude = target.getExclude();
                public boolean visit( IResource resource ) throws CoreException{
                	if( resource instanceof IFolder ){
                        IFolder folder = (IFolder)resource;

                        if( resource.isAccessible() && !folder.isTeamPrivateMember() ){
                            File file = resource.getLocation().toFile();
                            String modelPath = environment.systemToModel( file );
                            if( modelPath != null ){
                                boolean include = target == null || !exclude.shouldExclude( modelPath );

                                if( include ){
                                    result.add( modelPath );
                                }
                            }
                        }
                    }
                    return true;
                }
            });
        }
        catch ( CoreException e ){
            TinyOSPlugin.warning( e.getStatus() );
        }

        return result.toArray( new String[ result.size() ] );
    }

    public File locateFile( String name, boolean systemFile, IProgressMonitor monitor ){
        TinyOSPlugin plugin = TinyOSPlugin.getDefault();
        if( plugin == null )
            return null;

        return plugin.locate( name, systemFile, monitor, project );
    }
    
    private class PendingUpdate implements IResourceChangeListener, IResourceDeltaVisitor{
    	public void resourceChanged( IResourceChangeEvent event ){
    		IResourceDelta delta = event.getDelta();
    		if( delta != null ){
    			try{
					delta.accept( this );
				}
				catch( CoreException e ){
					TinyOSPlugin.log( e );
				}
    		}
    	}
    	
    	public boolean visit( IResourceDelta delta ) throws CoreException{
    		if( delta.getResource() == project ){
    			if( project.isOpen() ){
    				ProjectTOSUtility.updateNature( project );
    				ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );
    			}
    		}
    		return true;
    	}
    }
}
