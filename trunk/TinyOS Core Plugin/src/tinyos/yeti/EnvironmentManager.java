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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.properties.ProjectPropertyPage;

/**
 * Keeps a list of all available {@link IEnvironment}s and tells for each
 * project which environment it should use.  
 * @author Benjamin Sigg
 */
public class EnvironmentManager{
	private Collection<IEnvironment> environments;
	private TinyOSPlugin plugin;
    
	private List<Listener> listeners = new ArrayList<Listener>();
	
	private final Object ENVIRONMENT_INITIALIZED = new Object();
	
	/**
	 * An observer for an {@link EnvironmentManager}.
	 * @author Benjamin Sigg
	 */
	public static interface Listener{
		/**
		 * Called if the environment of a project changed, this method
		 * can be called from any thread and should execute fast.
		 * @param project the project whose environment changed
		 */
		public void environmentChanged( IProject project );
		
		/**
		 * Called once all {@link IEnvironment}s have been initialized.
		 */
		public void initialized();
	}
	
    public EnvironmentManager( TinyOSPlugin plugin ){
    	this.plugin = plugin;
    	
        // make sure the other plugins are fired up
        Job startup = new Job( "Start TinyOS" ){
        	@Override
        	protected IStatus run( IProgressMonitor monitor ){
        		monitor.beginTask( "Start", IProgressMonitor.UNKNOWN );
        		loadEnvironments();
        		monitor.done();
        		return Status.OK_STATUS;
        	}
        };
        startup.setSystem( true );
        startup.schedule();
    }
    
    public static EnvironmentManager getDefault(){
    	return TinyOSPlugin.getDefault().getEnvironments();
    }
    
    public void addListener( Listener listener ){
    	synchronized( listeners ){
    		listeners.add( listener );
		}
    }
    
    /**
     * Adds a listener to this {@link EnvironmentManager}, the listeners
     * method {@link Listener#initialized()} will be called exactly one: either
     * directly from this method if the environments are already initialized,
     * or later once they get initialized.
     * @param listener a listener whose initialized-method will be called in
     * any case
     */
    public void addInitializingListener( Listener listener ){
    	addListener( listener );
    	if( environments != null ){
    		synchronized( ENVIRONMENT_INITIALIZED ){
				if( environments == null ){
		    		listener.initialized();		
				}
			}
    	}
    }
    
    public void removeListener( Listener listener ){
    	synchronized( listeners ){
    		listeners.remove( listener );
		}
    }
    
    private Listener[] listeners(){
    	synchronized( listeners ){
    		return listeners.toArray( new Listener[ listeners.size() ] );
    	}
    }
    
    public IEnvironment getEnvironment( IProject p ){
    	try{
    		String key = p.getPersistentProperty( new QualifiedName( "", ProjectPropertyPage.ENV_PROPERTY ) );
    		for( IEnvironment environment : getEnvironments() ){
    			if( environment.getEnvironmentID().equals( key )){
    				return environment;
    			}
    		}
    	}
    	catch ( CoreException e ){
    		TinyOSPlugin.log( e );
    	}

    	return getDefaultEnvironment();
    }
    
    public IEnvironment getEnvironment( String environmentId ){
    	for( IEnvironment environment : getEnvironments() ){
    		if( environment.getEnvironmentID().equals( environmentId )){
    			return environment;
    		}
    	}
    	return null;
    }
    
    public void setEnvironment( IProject project, IEnvironment environment ) throws CoreException {
    	if( getEnvironment( project ) != environment ){
    		try{
	    		ProjectTOS tos = plugin.getProjectTOS( project );
	    		
	    		project.setPersistentProperty( new QualifiedName( "", ProjectPropertyPage.ENV_PROPERTY ), environment.getEnvironmentID() );
		    	for( Listener listener : listeners() ){
		    		listener.environmentChanged( project );
		    	}
		    	
		    	tos.initialize( true );
    		}
    		catch( MissingNatureException ex ){
    			TinyOSPlugin.log( ex );
    		}
    	}
    }

    public IEnvironment getDefaultEnvironment(){
        int highest = -1;
        IEnvironment best = null;
        
        for( IEnvironment env : getEnvironments() ){
            if( env.getEnvironmentImportance() > highest ){
                highest = env.getEnvironmentImportance();
                best = env;
            }
        }

        return best;
    }
    
    public Collection<IEnvironment> getEnvironments(){
    	if( environments == null ){
    		loadEnvironments();
    	}
    	return environments;
    }

    public IEnvironment[] getEnvironmentsArray(){
    	return environments.toArray( new IEnvironment[ environments.size() ] );
    }
    
    private synchronized void loadEnvironments(){
    	if( environments == null ){
    		IExtensionRegistry reg = Platform.getExtensionRegistry();
    		IExtensionPoint ep = reg.getExtensionPoint( TinyOSPlugin.PLUGIN_ID + ".Environments" );
    		IExtension[] extensions = ep.getExtensions();

    		List<IEnvironment> list = new ArrayList<IEnvironment>();
    		
    		for(IExtension extension: Arrays.asList(extensions)){
    			IConfigurationElement[] configurationElements = extension.getConfigurationElements();
    			for(IConfigurationElement configurationElement: Arrays.asList(configurationElements)){
    				try{
    					list.add( ( IEnvironment )configurationElement.createExecutableExtension( "class" ) );
    				}
    				catch ( CoreException e ){
    					TinyOSPlugin.log( e );
    				}
    			}
    		}
    		
    		environments = Collections.unmodifiableList( Arrays.asList( list.toArray( new IEnvironment[ list.size() ] ) ) );

    		plugin.getTargetManager().connect( environments );
    		plugin.getProjectManager().connect( environments );
    		
    		synchronized( ENVIRONMENT_INITIALIZED ){
	    		for( Listener listener : listeners() ){
	    			listener.initialized();
	    		}
    		}
    	}
    }

}
