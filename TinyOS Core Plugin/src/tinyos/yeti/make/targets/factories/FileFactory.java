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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class FileFactory implements IMakeTargetPropertyFactory<IFile>{
	public boolean supportsXML(){
		return true;
	}

	public void write( IFile value, XWriteStack out ){
		IPath path = value.getFullPath();
		out.setText( path.toPortableString() );
	}

	public IFile read( XReadStack in ){
		String text = in.getText();
		if( "".equals( text ))
			return null;
		
		IPath path = Path.fromPortableString( text );
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.getFile( path );
	}

	public IFile read( MakeTargetPropertyKey<IFile> key, ILaunchConfiguration configuration ){
		try{
			String portable = configuration.getAttribute( "tinyos." + key.getName(), (String)null );
			if( portable == null )
				return null;
			IPath path = Path.fromPortableString( portable );
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			return root.getFile( path );
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex );
			return null;
		}
	}


	public void write( IFile value, MakeTargetPropertyKey<IFile> key, ILaunchConfigurationWorkingCopy configuration ){
		if( value != null ){
			configuration.setAttribute( "tinyos." + key.getName(), value.getFullPath().toPortableString() );
		}
	}

}
