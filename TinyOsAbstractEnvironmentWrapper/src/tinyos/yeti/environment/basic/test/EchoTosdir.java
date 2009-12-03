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

import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.environment.basic.AbstractEnvironment;
import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;
import tinyos.yeti.environment.basic.commands.Echo;
import tinyos.yeti.environment.basic.commands.ICommandExecuter;

public class EchoTosdir extends AbstractTest{
	private AbstractEnvironment environment;
	
	public EchoTosdir( AbstractEnvironment environment ){
		this.environment = environment;
		
		setName( "Tosdir" );
		setDescription( "Compares the environment variable TOSDIR with the settings of this plugin" );
	}
	
	public IStatus run( OutputStream out, OutputStream err, IProgressMonitor monitor ) throws Exception {
		String tosdirIn = environment.getPathManager().getTosDirectoryPath();
		
		if( tosdirIn == null || tosdirIn.length() == 0 ){
			return new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "TOSDIR not set in plugin" );
		}
		
		ICommandExecuter executer = environment.getCommandExecuter();
		Echo echo = new Echo( "$TOSDIR", true );
		echo.setUseDefaultParameters( false );
		
		String tosdirEx = executer.execute( echo, monitor, out, out, err );
		
		if( tosdirEx == null || tosdirEx.length() == 0 ){
			return new Status( IStatus.WARNING, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "TOSDIR not set outside plugin" );
		}
		
		if( !tosdirEx.equals( tosdirIn )){
			return new Status( IStatus.WARNING, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "TOSDIR inside and outside plugin are not the same" );
		}
		
		return Status.OK_STATUS;
	}
}
