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

import org.eclipse.core.resources.IProject;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.make.MakeMacro;
import tinyos.yeti.nature.MissingNatureException;

/**
 * Properties that are extracted from a {@link IPlatform}.
 * @author Benjamin Sigg
 */
public class PlatformProperties implements IMakeTargetProperties{
	private IMakeTargetProperties information;
	
	/**
	 * Creates a new {@link PlatformProperties}.
	 * @param information used for project, target and {@link MakeTargetPropertyKey#INCLUDE_ENVIRONMENT_DEFAULT_PATHS}
	 */
	public PlatformProperties( IMakeTargetProperties information ){
		this.information = information;
	}
	
	public int getPriority(){
		return 20;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getLocalProperty( MakeTargetPropertyKey<T> key ){
		if( MakeTargetPropertyKey.INCLUDES.equals( key ))
			return (T)getIncludes();
		
		if( MakeTargetPropertyKey.MACROS.equals( key ))
			return (T)getMacros();
		
		return null;
	}
	
	public MakeInclude[] getIncludes(){
		if( !getProperty( MakeTargetPropertyKey.INCLUDE_ENVIRONMENT_DEFAULT_PATHS ))
			return null;
		
		IPlatform platform = getPlatform();
		if( platform == null )
			return null;
		
		return platform.getDefaultIncludes();
	}
	
	public MakeMacro[] getMacros(){
		IPlatform platform = getPlatform();
		if( platform == null )
			return null;
		
		return platform.getMacros();
	}
	
	public IPlatform getPlatform(){
		String target = information.getProperty( MakeTargetPropertyKey.TARGET );
		if( target == null )
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
			// silent
			return null;
		}
		
		IEnvironment environment = tos.getEnvironment();
        if( environment == null )
            return null;
        
        IPlatform[] platforms = environment.getPlatforms();
        if( platforms == null )
            return null;
        
        for( IPlatform platform : platforms ){
            if( platform.getName().equals( target )){
                return platform;
            }
        }
        
        return null;
	}
	
	public <T> T getProperty( MakeTargetPropertyKey<T> key ){
		if( MakeTargetPropertyKey.TARGET.equals( key ))
			return information.getProperty( key );
		
		if( MakeTargetPropertyKey.PROJECT.equals( key ))
			return information.getProperty( key );
		
		if( MakeTargetPropertyKey.INCLUDE_ENVIRONMENT_DEFAULT_PATHS.equals( key ))
			return information.getProperty( key );
		
		return getLocalProperty( key );
	}
}
