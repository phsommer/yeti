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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.nature.MissingNatureException;

/**
 * Checks whether a given {@link IResource} is buildable as a 
 * TinyOS application
 * @author Benjamin Sigg
 */
public class Buildable extends PropertyTester{
	public boolean test( Object receiver, String property, Object[] args, Object expectedValue ){
		try{
			if( !(receiver instanceof IResource ))
				return false;
		
			IResource resource = (IResource)receiver;
			IProject project = resource.getProject();
			if( project == null )
				return false;
			
			if( !project.isOpen() )
				return false;
		
			if( !project.hasNature( TinyOSCore.NATURE_ID ))
				return false;
			
			if( resource != project ){
				if( !"nc".equals( resource.getFileExtension() ))
					return false;
				
				ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
				if( tos.getSourceContainer( resource ) == null )
					return false;
			}
			
			return true;
		}
		catch( MissingNatureException ex ){
			// obviously not
			return false;
		}
		catch( CoreException e ){
			TinyOSPlugin.log( e.getStatus() );
			return false;
		}
	}
}
