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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class StringFactory implements IMakeTargetPropertyFactory<String>{
	public boolean supportsXML(){
		return true;
	}
	
	public void write( String value, XWriteStack out ){
		out.setText( value );
	}
	public String read( XReadStack in ){
		return in.getText();
	}
	
	public void write( String value, MakeTargetPropertyKey<String> key, ILaunchConfigurationWorkingCopy configuration ){
		configuration.setAttribute( "tinyos." + key.getName(), value );
	}
	public String read( MakeTargetPropertyKey<String> key, ILaunchConfiguration configuration ){
		try{
			return configuration.getAttribute( "tinyos." + key.getName(), (String)null );
		}
		catch( CoreException e ){
			TinyOSPlugin.log( e.getStatus() );
			return null;
		}
	}
}
