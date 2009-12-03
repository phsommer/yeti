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
package tinyos.yeti.environment.winXP;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.utility.preferences.OldPluginPreferenceStore;

/**
 * The activator class controls the plug-in life cycle
 */
public class TinyOSWinXPEnvironmentWrapper extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "tinyos.yeti.environment.winXP";

	// The shared instance
	private static TinyOSWinXPEnvironmentWrapper plugin;
	
	private IPreferenceStore preferenceStore;
	
	/**
	 * The constructor
	 */
	public TinyOSWinXPEnvironmentWrapper() {
	}

    @Override
    public IPreferenceStore getPreferenceStore(){
    	if( preferenceStore == null ){
    		preferenceStore =  new OldPluginPreferenceStore( super.getPreferenceStore(), "TinyOsWinXPEnvironmentWrapper" );
    	}
    	
    	return preferenceStore;
    }
    
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start( BundleContext context ) throws Exception {
		super.start(context);
		plugin = this;
		
		// force TinyOSPlugin to be started as well
		TinyOSPlugin.getDefault();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop( BundleContext context ) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static TinyOSWinXPEnvironmentWrapper getDefault() {
		return plugin;
	}


    public static void warning( Exception e ){
        TinyOSWinXPEnvironmentWrapper plugin = getDefault();
        if( plugin != null )
            plugin.getLog().log( new Status( IStatus.WARNING, PLUGIN_ID, 0, e.getMessage(), e ) );
    }

    public static void warning( IStatus status ){
        TinyOSWinXPEnvironmentWrapper plugin = getDefault();
        if( plugin != null )
            plugin.getLog().log( new Status( IStatus.WARNING, status.getPlugin(), status.getCode(), status.getMessage(), status.getException() ) );
    }
}
