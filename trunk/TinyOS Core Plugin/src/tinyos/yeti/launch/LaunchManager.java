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
package tinyos.yeti.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.TinyOSPlugin;

/**
 * The {@link LaunchManager} can be used to check whether a launch or
 * debugging session can be successful.
 * @author Benjamin Sigg
 */
public class LaunchManager{
	private ILaunchVeto[] vetos;
	
	public static LaunchManager getDefault(){
		return TinyOSPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * May issue a veto before applications of the platform are called.
	 * @param monitor to report progress
	 * @return <code>true</code> if the call should continue
	 */
	public boolean launch( IProgressMonitor monitor ){
		loadVetos();
		monitor.beginTask( "check vetos", vetos.length );
		for( ILaunchVeto veto : vetos ){
			IProgressMonitor sub = new SubProgressMonitor( monitor, 1 );
			if( veto.veto( sub ) ){
				monitor.done();
				return false;
			}
			sub.done();
		}
		return true;
	}
	
	private synchronized void loadVetos(){
		if( vetos == null ){
	        IExtensionRegistry reg = Platform.getExtensionRegistry();
	        IExtensionPoint extPoint = reg.getExtensionPoint( TinyOSPlugin.PLUGIN_ID + ".LaunchVeto" );
	        List<ILaunchVeto> result = new ArrayList<ILaunchVeto>();
	        
	        for( IExtension ext : extPoint.getExtensions() ){
	            for( IConfigurationElement element : ext.getConfigurationElements() ){
	                if( element.getName().equals( "veto" ) ){
	                    try{
	                    	result.add( (ILaunchVeto)element.createExecutableExtension( "class" ) );
	                    }   
	                    catch ( CoreException e ){
	                    	TinyOSPlugin.log( e );
	                    }
	                }
	            }
	        }

	        vetos = result.toArray( new ILaunchVeto[ result.size() ] );
		}
	}
}
