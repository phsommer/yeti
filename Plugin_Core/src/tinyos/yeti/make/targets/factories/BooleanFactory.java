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

public class BooleanFactory implements IMakeTargetPropertyFactory<Boolean>{
	public boolean supportsXML(){
		return true;
	}
	
	public void write( Boolean value, XWriteStack out ){
		out.setText( String.valueOf( value ) );
	}
	
	public Boolean read( XReadStack in ){
		return Boolean.valueOf( in.getText() );
	}
	
	public void write( Boolean value, MakeTargetPropertyKey<Boolean> key, ILaunchConfigurationWorkingCopy configuration ){
		configuration.setAttribute( "tinyos." + key.getName(), value.booleanValue() );
	}
	
	public Boolean read( MakeTargetPropertyKey<Boolean> key, ILaunchConfiguration configuration ){
		try{
			return configuration.getAttribute( "tinyos." + key.getName(), false );
		}
		catch( CoreException e ){
			TinyOSPlugin.log( e.getStatus() );
			return false;
		}
	}
}
