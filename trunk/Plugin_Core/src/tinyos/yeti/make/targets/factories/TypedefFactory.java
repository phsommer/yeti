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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.MakeTypedef;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class TypedefFactory implements IMakeTargetPropertyFactory<MakeTypedef[]>{
	public boolean supportsXML(){
		return true;
	}
	
	public void write( MakeTypedef[] value, XWriteStack xml ){
		for( MakeTypedef typedef : value ){
			xml.push( "typedef" );
			xml.setAttribute( "type", typedef.getType() );
			xml.setAttribute( "name", typedef.getName() );
			xml.pop();
		}
	}
	
	public MakeTypedef[] read( XReadStack xml ){
		List<MakeTypedef> typedefs = new ArrayList<MakeTypedef>();
		
		while( xml.go( "typedef" )){
			typedefs.add(
					new MakeTypedef( 
							xml.getString( "type", "type" ),
							xml.getString( "name", "name" ) ));
			xml.pop();
		}
		
		return typedefs.toArray( new MakeTypedef[ typedefs.size() ] );
	}
	
	public void write( MakeTypedef[] value, MakeTargetPropertyKey<MakeTypedef[]> key, ILaunchConfigurationWorkingCopy configuration ){
		List<String> name = new ArrayList<String>();
		List<String> type = new ArrayList<String>();
		
		if( value != null ){
			for( MakeTypedef typedef : value ){
				name.add( typedef.getName() );
				type.add( typedef.getType() );
			}
		}
		
		configuration.setAttribute( "tinyos." + key.getName() + ".names", name );
		configuration.setAttribute( "tinyos." + key.getName() + ".types", type );
	}
	
	@SuppressWarnings("unchecked")
	public MakeTypedef[] read( MakeTargetPropertyKey<MakeTypedef[]> key, ILaunchConfiguration configuration ){
		try{
			List<String> name = configuration.getAttribute( "tinyos." + key.getName() + ".names", Collections.EMPTY_LIST );
			List<String> type = configuration.getAttribute( "tinyos." + key.getName() + ".types", Collections.EMPTY_LIST );
		
			MakeTypedef[] result = new MakeTypedef[ Math.min( name.size(), type.size() )];
			for( int i = 0; i < result.length; i++ ){
				result[i] = new MakeTypedef( type.get( i ), name.get( i ));
			}
			return result;
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex.getStatus() );
			return new MakeTypedef[]{};
		}
	}
}
