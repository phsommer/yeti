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
package tinyos.yeti.make;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;

import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.targets.IMakeTargetMorpheable;
import tinyos.yeti.make.targets.MakeTargetSkeleton;
import tinyos.yeti.utility.PlatformObserver;

/**
 * Holds for each {@link IProject} an {@link ProjectTargets}, hence can tell
 * for each project which {@link IMakeTarget}s are available.
 */
public class MakeTargetManager implements IResourceChangeListener{
    private ListenerList listeners = new ListenerList( ListenerList.IDENTITY );
    private Map<IProject, ProjectTargets> projectMap = new HashMap<IProject, ProjectTargets>();

    public static String TARGETS_EXT = "targets_nc";

    public MakeTargetManager(){
    	// ignore
    }
    
    /**
     * Connects this manager with all the available environments. This manager
     * will add a listener to each {@link IPlatform} 
     * @param environments
     */
    public void connect( Collection<IEnvironment> environments ){ 
        for( IEnvironment environment : environments ){
            new PlatformObserver( environment ){
            	@Override
            	public void makeIncludesChanged( IPlatform platform ){
            		update( platform );
            	}
            };
        }
    }

    /**
     * Searches all {@link MakeTarget}s which include <code>platform</code>
     * and updates their paths.
     * @param platform the platform whose make includes changed
     */
    private void update( IPlatform platform ){
        // search all make targets which refer to platform and mark them as changed
        for( ProjectTargets targets : projectMap.values() ){
            if( targets != null ){
                IMakeTargetMorpheable[] targetList = targets.getSelectableTargets();
                if( targetList != null ){
                    for( IMakeTargetMorpheable morphable : targetList ){
                    	MakeTarget target = morphable.toMakeTarget();
                    	
                        if( target.isUsingPlatformIncludes() && target.getPlatform() == platform ){
                            MakeTargetEvent event = new MakeTargetEvent( this, MakeTargetEvent.TARGET_CHANGED, target );
                            notifyListeners( event );
                        }
                    }
                }
            }
        }
    }

    public void notifyListeners( MakeTargetEvent event ){
        Object[] list = listeners.getListeners();
        for( int i = 0; i < list.length; i++ ){
            (( IMakeTargetListener )list[i] ).targetChanged( event );
        }
    }

    public void addListener( IMakeTargetListener listener ){
        listeners.add( listener );
    }

    public void removeListener( IMakeTargetListener listener ){
        listeners.remove( listener );
    }

    public Collection<MakeTarget> convert( String content, IProject project ){
    	return loadProjectTargets( project ).convert( content );
    }
    
    /**
     * Adds <code>target</code> to the project specified in <code>target</code>
     * itself. This method will rename <code>target</code> if necessary
     * and will change the default-state of <code>target</code> to <code>false</code>.
     * @param target the target to add
     */
    public void pasteTarget( MakeTarget target ){
    	// check the name
    	MakeTarget[] targets = getTargets( target.getProject() );
    	Set<String> names = new HashSet<String>();
    	for( MakeTarget check : targets ){
    		names.add( check.getName() );
    	}
    	
    	String name = target.getName();
    	if( name == null || name.equals( "" ))
    		name = "?";
    	
    	int index = 2;
    	if( name.endsWith( ")" )){
    		int offset = name.lastIndexOf( "(" );
    		if( offset >= 0 ){
    			String number = name.substring( offset+1, name.length()-1 );
    			try{
    				index = Integer.parseInt( number );
    				name = name.substring( 0, offset );
    				if( name.endsWith( " " ))
    					name = name.substring( 0, name.length()-1 );
    				if( name.equals( "" ))
    					name = "?";
    			}
    			catch( NumberFormatException ex ){
    				// ignore
    			}
    		}
    	}
    	
    	String finalName = name;
    	
    	if( names.contains( name )){
    		while( names.contains( finalName = name + " (" + index + ")" ))
    			index++;
    	}
    	
    	target.setName( finalName );
    	addTarget( target );
    }
    
    public boolean addTarget( MakeTarget target ){
    	return getProjectTargets( target.getProject() ).addStandardTarget( target );
    }

    protected void deleteTargets( IProject project ){
        IPath targetFilePath = TinyOSPlugin.getDefault().getStateLocation()
        	.append( project.getName() ).addFileExtension( TARGETS_EXT );
        File targetFile = targetFilePath.toFile();
        if( targetFile.exists() ){
            targetFile.delete();
        }
        projectMap.remove( project );

        // remove preference key
        if( TinyOSPlugin.getDefault().getPreferenceStore().contains(
                project.getName() + ProjectTargets.MAKE_TARGET_KEY ) ){
            TinyOSPlugin.getDefault().getPreferenceStore().setValue(
                    project.getName() + ProjectTargets.MAKE_TARGET_KEY, "" );
        }
    }

    public boolean removeTarget( MakeTarget target ){
    	return getProjectTargets( target.getProject() ).removeStandardTarget( target );
    }

    private ProjectTargets readTargets( IProject project ){
        ProjectTargets projectTargets = new ProjectTargets( this, project );
        projectMap.put( project, projectTargets );
        return projectTargets;
    }

    public IProjectMakeTargets getProjectTargets( IProject project ){
    	return loadProjectTargets( project );
    }
    
    private ProjectTargets loadProjectTargets( IProject project ){
    	ProjectTargets result = projectMap.get( project );
    	if( result == null ){
    		result = readTargets( project );
    	}
    	return result;
    }
    
    public MakeTarget[] getTargets( IProject project ){
        ProjectTargets projectTargets = projectMap.get( project );
        if( projectTargets == null ){
            projectTargets = readTargets( project );
        }
        return projectTargets.getStandardTargets();
    }
    
    public void refresh( IProject project ){
        readTargets( project );
        notifyListeners( new MakeTargetEvent( this,
                MakeTargetEvent.PROJECT_REFRESH, project ) );
    }

    public void startUp(){
        IProject project[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for( int i = 0; i < project.length; i++ ){
            try{
                if( !project[i].isAccessible() )
                    continue;
                if( project[i].hasNature( TinyOSCore.NATURE_ID ) ){
                    projectMap.put( project[i], null );
                }
            }catch ( CoreException e ){
                e.printStackTrace();
            }

        }
        ResourcesPlugin.getWorkspace().addResourceChangeListener( this );
    }

    public void shutdown(){
        ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );
    }

    public void resourceChanged( IResourceChangeEvent event ){
        IResourceDelta delta = event.getDelta();
        if( delta != null ){
            try{
                delta.accept( new MakeTargetVisitor() );
            }
            catch ( CoreException e ){
                TinyOSPlugin.log( e );
            }
        }

    }

    private class MakeTargetVisitor implements IResourceDeltaVisitor{
        public boolean visit( IResourceDelta delta ){
            // System.out.println("visit - "+delta);
            if( delta == null ){
                return false;
            }

            IResource resource = delta.getResource();

            if( resource.getType() == IResource.PROJECT ){
                IProject project = ( IProject )resource;
                int deltaKind = delta.getKind();

                if( (deltaKind & ( IResourceDelta.REMOVED | IResourceDelta.REPLACED )) != 0 ){
                    if( projectMap.containsKey( project ) ){
                        deleteTargets( project );
                        projectMap.remove( project );
                        notifyListeners( new MakeTargetEvent(
                                MakeTargetManager.this,
                                MakeTargetEvent.PROJECT_REMOVED, project ) );
                    }
                }

                if( (deltaKind & ( IResourceDelta.ADDED | IResourceDelta.REPLACED )) != 0 ){
                    if( !projectMap.containsKey( project ) && isTinyOSProject( project ) ){
                        projectMap.put( project, null );
                        notifyListeners( new MakeTargetEvent(
                                MakeTargetManager.this,
                                MakeTargetEvent.PROJECT_ADDED, project ) );
                    }
                }


                if( ( deltaKind & IResourceDelta.CHANGED ) != 0 ){
                    if( project.isOpen() ){
                        if( !projectMap.containsKey( project ) && isTinyOSProject( project ) ){
                            projectMap.put( project, null );
                            notifyListeners( new MakeTargetEvent(
                                    MakeTargetManager.this,
                                    MakeTargetEvent.PROJECT_ADDED, project ) );
                        }
                    }
                    else{
                        if( projectMap.containsKey( project )){
                            projectMap.remove( project );
                            notifyListeners( new MakeTargetEvent(
                                    MakeTargetManager.this,
                                    MakeTargetEvent.PROJECT_REMOVED, project ) );
                        }
                    }
                }
                return false;
            }
            return resource instanceof IWorkspaceRoot;
        }
    }
    
    private boolean isTinyOSProject( IProject project ){
    	try{
    		return project.hasNature( TinyOSCore.NATURE_ID ) || project.hasNature( TinyOSCore.OLD_NATURE_ID );
    	}
    	catch( CoreException ex ){
    		TinyOSPlugin.warning( ex );
    		return false;
    	}
    }

    public IProject[] getTargetBuilderProjects(){
        return projectMap.keySet().toArray( new IProject[ projectMap.size() ] );
    }

    public void updateTarget( MakeTarget target ) {
        ProjectTargets projectTargets = projectMap.get( target.getProject() );
        if( projectTargets == null ){
            return; // target has not been added to manager.
        }
        projectTargets.informStandardTargetChanged( target );
    }
    
    public void updateDefaults( MakeTargetSkeleton defaults ) {
        ProjectTargets projectTargets = projectMap.get( defaults.getProject() );
        if( projectTargets != null ){
        	projectTargets.informDefaultsChanged();
        }
    }

    public IMakeTarget findTarget( IProject project, String name ) {
        ProjectTargets projectTargets = projectMap.get( project );
        if( projectTargets == null ){
            projectTargets = readTargets( project );
        }
        return projectTargets.findStandardTarget( name );
    }

    public void setSelectedTarget( MakeTarget target ) {
        getProjectTargets( target.getProject() ).setSelectedTarget( target );
    }

    public IMakeTargetMorpheable getSelectedTarget( IProject project ){
    	if( project == null )
            return null;
    	
    	return getProjectTargets( project ).getSelectedTarget();
    }
    
    public void setDefaults( MakeTargetSkeleton target ){
    	getProjectTargets( target.getProject() ).setDefaults( target );
    }

    public MakeTargetSkeleton getDefaults( IProject project ){
    	if( project == null )
    		return null;

    	return getProjectTargets( project ).getDefaults();
    }
}
