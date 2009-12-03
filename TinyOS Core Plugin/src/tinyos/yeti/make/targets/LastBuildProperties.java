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
package tinyos.yeti.make.targets;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.nature.MissingNatureException;

/**
 * Properties that depend on the last executed build.
 * @author Benjamin Sigg
 */
public class LastBuildProperties implements IMakeTargetProperties{
	private IMakeTargetProperties information;
	
	public LastBuildProperties( IMakeTargetProperties information ){
		this.information = information;
	}
	
	public int getPriority(){
		return 10;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getLocalProperty( MakeTargetPropertyKey<T> key ){
		if( MakeTargetPropertyKey.INCLUDES.equals( key ))
			return (T)getIncludes();
		
		return null;
	}
	
	public MakeInclude[] getIncludes(){
		if( !getProperty( MakeTargetPropertyKey.INCLUDE_LAST_BUILD ))
			return null;
		
		IProject project = information.getProperty( MakeTargetPropertyKey.PROJECT );
		if( project == null )
			return null;
		
		ProjectTOS tos;
		try{
			tos = TinyOSPlugin.getDefault().getProjectTOS( project );
	        if( tos == null )
				return null;
		}
		catch( MissingNatureException ex ){
			// ignore
			return null;
		}
		
		String target = information.getProperty( MakeTargetPropertyKey.TARGET );
		if( target == null )
			return null;
			
		IFolder folder = tos.getBuildContainer().getFolder( target );
		if( !folder.exists() )
			return null;
			
		IPath path = folder.getLocation();
		if( path == null )
			return null;
				
		IEnvironment environment = tos.getEnvironment();
		if( environment == null )
			return null;
		        
		String modelPath = environment.systemToModel( path.toFile() );
		if( modelPath == null )
			return null;
					
		return new MakeInclude[]{ new MakeInclude( modelPath, MakeInclude.Include.SOURCE, true, true, false ) };
	}
	
	public <T> T getProperty( MakeTargetPropertyKey<T> key ){
		if( MakeTargetPropertyKey.PROJECT.equals( key ))
			return information.getProperty( key );
		
		if( MakeTargetPropertyKey.TARGET.equals( key ))
			return information.getProperty( key );
		
		if( MakeTargetPropertyKey.INCLUDE_LAST_BUILD.equals( key ))
			return information.getProperty( key );
		
		return getLocalProperty( key );
	}
}
