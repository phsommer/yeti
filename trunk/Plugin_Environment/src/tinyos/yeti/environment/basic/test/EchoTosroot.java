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

public class EchoTosroot extends AbstractTest{
	private AbstractEnvironment environment;
	
	public EchoTosroot( AbstractEnvironment environment ){
		this.environment = environment;
		
		setName( "Tosroot" );
		setDescription( "Compares the environment variable TOSROOT with the settings of this plugin" );
	}
	
	public IStatus run( OutputStream out, OutputStream err, IProgressMonitor monitor ) throws Exception {
		String tosrootIn = environment.getPathManager().getTosRootPath();
		
		if( tosrootIn == null || tosrootIn.length() == 0 ){
			return new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "TOSROOT not set in plugin" );
		}
		
		ICommandExecuter executer = environment.getCommandExecuter();
		Echo echo = new Echo( "$TOSROOT", true );
		echo.setUseDefaultParameters( false );
		
		String tosrootEx = executer.execute( echo, monitor, out, out, err );
		
		if( tosrootEx == null || tosrootEx.length() == 0 ){
			return new Status( IStatus.WARNING, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "TOSROOT not set outside plugin" );
		}
		
		if( !tosrootEx.equals( tosrootIn )){
			return new Status( IStatus.WARNING, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, "TOSROOT inside and outside plugin are not the same" );
		}
		
		return Status.OK_STATUS;
	}
}
