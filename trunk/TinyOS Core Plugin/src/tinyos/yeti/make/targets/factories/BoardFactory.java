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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class BoardFactory implements IMakeTargetPropertyFactory<String[]>{
	public boolean supportsXML(){
		return true;
	}
	
	public void write( String[] value, XWriteStack out ){
		for( String board : value ){
			out.push( "board" );
			out.setText( board );
			out.pop();
		}
	}
	
	public String[] read( XReadStack in ){
		List<String> boards = new ArrayList<String>();
		
		while( in.go( "board" )){
			boards.add( in.getText() );
			in.pop();
		}
		
		return boards.toArray( new String[ boards.size() ] );
	}
	
	public void write( String[] value, MakeTargetPropertyKey<String[]> key, ILaunchConfigurationWorkingCopy configuration ){
		if( value == null ){
			configuration.setAttribute( "tinyos." + key.getName(), (List<String>)null );
		}
		else{
			configuration.setAttribute( "tinyos." + key.getName(), Arrays.asList( value ) );
		}
	}
	
	@SuppressWarnings("unchecked")
	public String[] read( MakeTargetPropertyKey<String[]> key, ILaunchConfiguration configuration ){
		try{
			List<String> list = configuration.getAttribute( "tinyos." + key.getName(), Collections.EMPTY_LIST );
			return list.toArray( new String[ list.size() ] );
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex.getStatus() );
			return new String[]{};
		}
	}
}
