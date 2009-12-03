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
package tinyos.yeti.environment.basic.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import tinyos.yeti.environment.basic.commands.AbstractCommand;
import tinyos.yeti.environment.basic.commands.IExecutionResult;

/**
 * Runs the 'env' application in an interactive login shell and returns the
 * output in a map.
 * @author Benjamin Sigg
 *
 */
public class EnvCommand extends AbstractCommand<Map<String,String>>{
	public EnvCommand(){
		setAssumesInteractive( true );
		setCommand( "env" );
	}
	
	public Map<String, String> result( IExecutionResult result ){
		Map<String, String> results = new HashMap<String, String>();
		String output = result.getOutput();
		if( output == null )
			return results;
		
		Scanner scanner = new Scanner( output );
		while( scanner.hasNextLine() ){
			String line = scanner.nextLine();
			int index = line.indexOf( '=' );
			if( index > 0 ){
				String key = line.substring( 0, index );
				String value = line.substring( index+1 );
				results.put( key, value );
			}
		}
		
		return results;
	}

	public boolean shouldPrintSomething(){
		return true;
	}
}
