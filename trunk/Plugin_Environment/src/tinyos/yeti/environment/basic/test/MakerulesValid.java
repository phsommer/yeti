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
package tinyos.yeti.environment.basic.test;

import java.io.File;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.environment.basic.AbstractEnvironment;
import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;

public class MakerulesValid extends AbstractTest{
	private AbstractEnvironment environment;
	
	public MakerulesValid( AbstractEnvironment environment ){
		this.environment = environment;
		
		setName( "Makerules valid" );
		setDescription( "Checks whether MAKERULES points to an existing location" );
	}
	
	public IStatus run(OutputStream out, OutputStream err, IProgressMonitor monitor) throws Exception {
		String path = environment.getPathManager().getMakerulesPath();
		if( path == null ){
			return new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "MAKERULES not set" );
		}
		
		File file = environment.modelToSystem( path );
		if( file == null || !file.exists() ){
			return new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "MAKERULES does not point to an existing file" );
		}
		if( !file.isFile() ){
			return new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "MAKERULES does not point to a file" );
		}
		if( !file.canRead() ){
			return new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "MAKERULES points to a file which is not readable" );
		}
		
		return Status.OK_STATUS;
	}
}
