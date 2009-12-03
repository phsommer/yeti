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

public class EchoMakerules extends AbstractTest{
	private AbstractEnvironment environment;
	
	public EchoMakerules( AbstractEnvironment environment ){
		this.environment = environment;
		
		setName( "Makerules" );
		setDescription( "Compares the environment variable MAKERULES with the settings of this plugin" );
	}
	
	public IStatus run( OutputStream out, OutputStream err, IProgressMonitor monitor ) throws Exception {
		String makerulesIn = environment.getPathManager().getMakerulesPath();
		
		if( makerulesIn == null || makerulesIn.length() == 0 ){
			return new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "MAKERULES not set in plugin" );
		}
		
		ICommandExecuter executer = environment.getCommandExecuter();
		Echo echo = new Echo( "$MAKERULES", true );
		echo.setUseDefaultParameters( false );
		
		String makerulesEx = executer.execute( echo, monitor, out, out, err );
		
		if( makerulesEx == null || makerulesEx.length() == 0 ){
			return new Status( IStatus.WARNING, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "MAKERULES not set outside plugin" );
		}
		
		if( !makerulesEx.equals( makerulesIn )){
			return new Status( IStatus.WARNING, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "MAKERULES inside and outside plugin are not the same" );
		}
		
		return Status.OK_STATUS;
	}
}
