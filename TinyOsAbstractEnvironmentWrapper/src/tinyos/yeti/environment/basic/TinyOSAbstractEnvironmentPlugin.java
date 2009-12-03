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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import tinyos.yeti.environment.basic.platform.MMCUConverter;

/**
 * The activator class controls the plug-in life cycle
 */
public class TinyOSAbstractEnvironmentPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "tinyos.yeti.environment.basic";

    // The shared instance
    private static TinyOSAbstractEnvironmentPlugin plugin;

    /**
     * The constructor
     */
    public TinyOSAbstractEnvironmentPlugin() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static TinyOSAbstractEnvironmentPlugin getDefault() {
        return plugin;
    }
    
    public static void log( IStatus status ){
    	TinyOSAbstractEnvironmentPlugin plugin = getDefault();
    	if( plugin != null ){
    		plugin.getLog().log( status );
    	}
    }
    
    public static List<MMCUConverter> loadMMCUConverters(){
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint extPoint = reg.getExtensionPoint( "TinyOS.Environment.MMCU" );

        List<MMCUConverter> result = new ArrayList<MMCUConverter>();
        
        for( IExtension ext : extPoint.getExtensions() ){
            for( IConfigurationElement element : ext.getConfigurationElements() ){
                if( element.getName().equals( "Converter" ) ){
                    if( element.getAttribute( "class" ) != null ){
                        try{
                            result.add( ( MMCUConverter )element.createExecutableExtension( "class" ) );
                        } 
                        catch ( CoreException e ){
                            log( e.getStatus() );
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    public static void error( Exception ex ){
        log( new Status( IStatus.ERROR, PLUGIN_ID, 0, ex.getMessage(), ex ));
    }
    
    public static void warning( IStatus status ){
        log( new Status( IStatus.WARNING, status.getPlugin(), status.getCode(), status.getMessage(), status.getException() ) );
    }
}
