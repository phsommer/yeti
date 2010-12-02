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
package tinyos.yeti.environment.winXP.test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.environment.basic.helper.StreamGobbler;
import tinyos.yeti.environment.basic.test.AbstractTest;
import tinyos.yeti.environment.winXP.Environment;
import tinyos.yeti.environment.winXP.TinyOSWinXPEnvironmentWrapper;

public class CanCallCygwin extends AbstractTest {
	public CanCallCygwin(){
		setName( "Cygwin" );
		setDescription( "Tries to start and stop cygwin" );
	}
	
	public IStatus run( OutputStream out, OutputStream err, IProgressMonitor monitor ) {
		File bash = Environment.getEnvironment().getPathManager().getCygwinBash();
		if( bash == null ){
			return new Status( IStatus.ERROR, TinyOSWinXPEnvironmentWrapper.PLUGIN_ID, "Cygwin is not set up" );
		}
		if( !bash.exists() ){
			return new Status( IStatus.ERROR, TinyOSWinXPEnvironmentWrapper.PLUGIN_ID, "Cygwin path is not correct: '" + bash.getAbsolutePath() + "'" );
		}
		
		ProcessBuilder builder = new ProcessBuilder( bash.getAbsolutePath() );
		
		try {
			Process process = builder.start();
			StreamGobbler outGobbler = new StreamGobbler( process.getInputStream(), out );
			StreamGobbler errGobbler = new StreamGobbler( process.getErrorStream(), err );
			
			outGobbler.start();
			errGobbler.start();
			
			process.getOutputStream().write( "exit\n".getBytes() );
			process.getOutputStream().flush();
			
			Environment.getEnvironment().getCommandExecuter().waitFor( process, monitor );
			
			if( monitor.isCanceled() ){
				outGobbler.stop();
				errGobbler.stop();
			}
			else{
				outGobbler.join();
				errGobbler.join();
			}
		}
		catch (IOException e) {
			return new Status( IStatus.ERROR, TinyOSWinXPEnvironmentWrapper.PLUGIN_ID, e.getMessage(), e );
		}
		
		return Status.OK_STATUS;
	}
}
