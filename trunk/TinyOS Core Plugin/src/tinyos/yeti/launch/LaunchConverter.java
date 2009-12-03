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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

/**
 * Code to convert {@link MakeTarget}s from and to {@link ILaunchConfiguration}s.
 * @author Benjamin Sigg
 */
public final class LaunchConverter{
	private LaunchConverter(){
		// nothing
	}
	
	public static IProject getProject( ILaunchConfiguration configuration ){
		MakeTarget target = new MakeTarget();
		read( target, MakeTargetPropertyKey.PROJECT, configuration );
		return target.getProject();
	}
	
	public static MakeTarget read( ILaunchConfiguration configuration ){
		MakeTarget target = new MakeTarget();
		read( target, MakeTargetPropertyKey.PROJECT, configuration );
		
		ProjectTOS project = target.getProjectTOS();
		if( project == null )
			target.setDefaults( new MakeTargetSkeleton( null ) );
		else
			target.setDefaults( project.getMakeTargets().getDefaults() );
		
		for( MakeTargetPropertyKey<?> key : MakeTargetPropertyKey.KEYS ){
			if( MakeTargetPropertyKey.PROJECT != key ){
				read( target, key, configuration );
			}
		}
		
		String name = configuration.getName();
		target.setName( name );
		target.setId( name );
		
		return target;
	}
	
	private static <T> void read( MakeTarget target, MakeTargetPropertyKey<T> key, ILaunchConfiguration configuration ){
		try{
			IMakeTargetPropertyFactory<T> factory = key.getFactory();
			if( factory != null ){
				T value = factory.read( key, configuration );
				target.putLocalProperty( key, value );
				
				target.setUseLocalProperty( key, configuration.getAttribute( "tinyos." + key.getName() + ".use-local", key.likeLocal() || key.isArray() ) );
				target.setUseDefaultProperty( key, configuration.getAttribute( "tinyos." + key.getName() + ".use-default", !key.likeLocal() ) );
			}
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex.getStatus() );
		}
	}
	
	public static void write( MakeTarget target, ILaunchConfigurationWorkingCopy configuration ){
		for( MakeTargetPropertyKey<?> key : MakeTargetPropertyKey.KEYS ){
			write( target, key, configuration );
		}
	}
	
	private static <T> void write( MakeTarget target, MakeTargetPropertyKey<T> key, ILaunchConfigurationWorkingCopy configuration ){
		IMakeTargetPropertyFactory<T> factory = key.getFactory();
		if( factory != null ){
			configuration.setAttribute( "tinyos." + key.getName() + ".use-local", target.isUseLocalProperty( key ) );
			configuration.setAttribute( "tinyos." + key.getName() + ".use-default", target.isUseDefaultProperty( key ) );
			factory.write( target.getLocalProperty( key ), key, configuration );
		}
	}
}
