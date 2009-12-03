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
import tinyos.yeti.make.MakeExclude;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class ExcludeFactory implements IMakeTargetPropertyFactory<MakeExclude[]>{
	public boolean supportsXML(){
		return true;
	}
	public void write( MakeExclude[] value, XWriteStack xml ){
		for( MakeExclude exclude : value ){
			xml.push( "exclude" );
			xml.setText( exclude.getPattern() );
			xml.pop();
		}
	}
	
	public MakeExclude[] read( XReadStack xml ){
		List<MakeExclude> excludes = new ArrayList<MakeExclude>();
		while( xml.go( "exclude" )){
			excludes.add( new MakeExclude( xml.getText() ));
			xml.pop();
		}
		return excludes.toArray( new MakeExclude[ excludes.size() ] );
	}
	
	public void write( MakeExclude[] value,
			MakeTargetPropertyKey<MakeExclude[]> key,
			ILaunchConfigurationWorkingCopy configuration ){
	
		List<String> list = new ArrayList<String>();
		if( value != null ){
			for( MakeExclude exclude : value ){
				list.add( exclude.getPattern() );
			}
		}
		configuration.setAttribute( "tinyos." + key.getName(), list );
	}
	
	@SuppressWarnings("unchecked")
	public MakeExclude[] read( MakeTargetPropertyKey<MakeExclude[]> key,
			ILaunchConfiguration configuration ){
	
		try{
			List<String> list = configuration.getAttribute( "tinyos." + key.getName(), Collections.EMPTY_LIST );
			MakeExclude[] result = new MakeExclude[ list.size() ];
			for( int i = 0; i < result.length; i++ ){
				result[i] = new MakeExclude( list.get( i ));
			}
			return result;
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex.getStatus() );
			return new MakeExclude[]{};
		}
	}
}
