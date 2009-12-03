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
package tinyos.yeti.environment.basic.commands;

/**
 * Calls the <code>echo</code> programm.
 * @author Benjamin Sigg
 */
public class Echo extends AbstractCommand<String>{
	private boolean lastLineOnly;

	public Echo( String echo, boolean lastLineOnly ){
		setCommand( new String[]{ "echo", echo });
		setAssumesInteractive( true );
		this.lastLineOnly = lastLineOnly;
	}

	public String result(IExecutionResult result) {
		String output = result.getOutput();
		if( lastLineOnly ){
			int index = output.lastIndexOf( '\n' );
			if( index == -1 )
				return output;

			if( index+1 < output.length() )
				return output.substring( index+1 );

			return "";
		}
		else{
			return output;
		}
	}

	public boolean shouldPrintSomething() {
		return true;
	}
}
