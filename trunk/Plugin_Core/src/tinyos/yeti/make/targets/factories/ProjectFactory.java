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
package tinyos.yeti.make.targets.factories;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class ProjectFactory implements IMakeTargetPropertyFactory<IProject>{
	public void write( IProject value, XWriteStack out ){
		// ignore
	}
	public IProject read( XReadStack in ){
		// ignore
		return null;
	}
	
	public boolean supportsXML(){
		return false;
	}
	
	public void write( IProject value, MakeTargetPropertyKey<IProject> key,
			ILaunchConfigurationWorkingCopy configuration ){
	
		try{
			configuration.setAttribute( "tinyos." + key.getName(), value == null ? null : value.getName() );
			
			IResource[] resources = configuration.getMappedResources();
			if( resources == null ){
				if( value != null ){
					configuration.setMappedResources( new IResource[]{ value } );
				}
			}
			else{
				int count = 0;
				for( IResource resource : resources ){
					if( !(resource instanceof IProject) ){
						count++;
					}
				}
				
				if( value != null ){
					count++;
				}
				
				if( count != resources.length ){
					IResource[] next = new IResource[ count ];
					int index = 0;
					for( IResource resource : resources ){
						if( !(resource instanceof IProject )){
							next[index] = resource;
						}
					}
					
					if( value != null ){
						next[ count-1 ] = value;
					}
					
					configuration.setMappedResources( next );
				}
				else if( value != null ){
					for( int i = 0; i < resources.length; i++ ){
						if( resources[i] instanceof IProject ){
							resources[i] = value;
						}
					}
					configuration.setMappedResources( resources );
				}
			}
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex.getStatus() );
		}
	}
	
	public IProject read( MakeTargetPropertyKey<IProject> key,
			ILaunchConfiguration configuration ){
		try{
			IResource[] resources = configuration.getMappedResources();
			if( resources == null )
				return null;
			
			for( IResource resource : resources ){
				if( resource instanceof IProject ){
					return (IProject)resource;
				}
			}
			
			return null;
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex.getStatus() );
			return null;
		}
	}
}
