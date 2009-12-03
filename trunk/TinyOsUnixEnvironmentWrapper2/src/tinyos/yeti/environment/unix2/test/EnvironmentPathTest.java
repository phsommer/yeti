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
package tinyos.yeti.environment.unix2.test;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import tinyos.yeti.environment.basic.test.AbstractTest;
import tinyos.yeti.environment.unix2.TinyOSUnixEnvironmentPlugin2;
import tinyos.yeti.environment.unix2.validation.EnvironmentVariableVeto;

public class EnvironmentPathTest extends AbstractTest{
	public EnvironmentPathTest(){
		setName( "Environment variables" );
		setDescription( "Compare the environment variables known to Eclipse with the variables known to the shell" );
	}
	
	public IStatus run( OutputStream out, OutputStream err, IProgressMonitor monitor ) throws Exception{
		Map<String,String> envp = System.getenv();
		Map<String,String> shell = EnvironmentVariableVeto.listBashEnvVariables();
		
		String[] problems = EnvironmentVariableVeto.listDifferences( envp, shell );
		
		if( problems.length == 0 )
			return Status.OK_STATUS;
		
		PrintWriter writer = new PrintWriter( out ){
			@Override
			public void close(){
				// ignore
			}
		};
		
		writer.println( "Environment variables differ, this can lead to failures when compiling or debugging an application." );
		
		for( String key : problems ){
			writer.println();
			writer.println( key + ":" );
			writer.println( "\tEclipse = '" + valueOf( envp.get( key ) ) + "'" );
			writer.println( "\tShell   = '" + valueOf( shell.get( key ) ) + "'" );
		}
		
		writer.flush();
		return new Status( IStatus.WARNING, TinyOSUnixEnvironmentPlugin2.PLUGIN_ID, "Not all environment variables are the same" );
	}
	
	private String valueOf( String string ){
		if( string == null )
			return "";
		return string;
	}
}
