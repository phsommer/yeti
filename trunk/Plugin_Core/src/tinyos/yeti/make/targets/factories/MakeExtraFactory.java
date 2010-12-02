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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.MakeExtra;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class MakeExtraFactory implements IMakeTargetPropertyFactory<MakeExtra[]>{
	public boolean supportsXML(){
		return true;
	}
	
	public void write( MakeExtra[] makeExtras, XWriteStack xml ){
		for( MakeExtra extra : makeExtras ){
			xml.push( "extra" );
			xml.setAttribute( "name", extra.getName() );

			if( extra.hasParameter() ){
				xml.push( "parameter" );
				xml.setAttribute( "name", extra.getParameterName() );
				xml.setAttribute( "ask", Boolean.toString( extra.askParameterAtCompileTime() ) );
				xml.setText( extra.getParameterValue() );
				xml.pop();
			}

			xml.pop();
		}
	}
	
	public MakeExtra[] read( XReadStack xml ){
		List<MakeExtra> extras = new ArrayList<MakeExtra>();
		
		while( xml.go( "extra" )){
			String name = xml.getString( "name", "name" );
			MakeExtra extra = new MakeExtra( name );

			if( xml.search( "parameter" )){
				extra.setParameterName( xml.getString( "name", "option" ) );
				extra.setAskParameterAtCompileTime( xml.getBoolean( "ask", false ) );
				extra.setParameterValue( xml.getText() );
				xml.pop();
			}

			extras.add( extra );
			xml.pop();
		}
		
		return extras.toArray( new MakeExtra[ extras.size() ] );
	}
	
	public void write( MakeExtra[] value, MakeTargetPropertyKey<MakeExtra[]> key, ILaunchConfigurationWorkingCopy configuration ){
		List<String> names = new ArrayList<String>( value.length );
		Map<String, String> parameterAsk = new HashMap<String, String>();
		Map<String, String> parameterNames = new HashMap<String, String>();
		Map<String, String> parameterValues = new HashMap<String, String>();

		if( value != null ){
			for( MakeExtra extra : value ){
				String name = extra.getName();
				names.add( name );

				if( extra.hasParameter() ){
					parameterAsk.put( name, String.valueOf( extra.askParameterAtCompileTime() ) );
					parameterNames.put( name, extra.getParameterName() );

					String parameterValue = extra.getParameterValue();
					if( parameterValue != null ){
						parameterValues.put( name, parameterValue );
					}
				}
			}
		}
		
		configuration.setAttribute( "tinyos." + key.getName() + ".names", names );
		configuration.setAttribute( "tinyos." + key.getName() + ".pAsk", parameterAsk );
		configuration.setAttribute( "tinyos." + key.getName() + ".pNames", parameterNames );
		configuration.setAttribute( "tinyos." + key.getName() + ".pValues", parameterValues );
	}
	
	@SuppressWarnings("unchecked")
	public MakeExtra[] read( MakeTargetPropertyKey<MakeExtra[]> key,
			ILaunchConfiguration configuration ){
	
		try{
			List<String> names = configuration.getAttribute( "tinyos." + key.getName() + ".names", Collections.EMPTY_LIST );
			Map<String, String> parameterAsk = configuration.getAttribute( "tinyos." + key.getName() + ".pAsk", Collections.EMPTY_MAP );
			Map<String, String> parameterNames = configuration.getAttribute( "tinyos." + key.getName() + ".pNames", Collections.EMPTY_MAP );
			Map<String, String> parameterValues = configuration.getAttribute( "tinyos." + key.getName() + ".pValues", Collections.EMPTY_MAP );
			
			MakeExtra[] result = new MakeExtra[ names.size() ];
			for( int i = 0; i < result.length; i++ ){
				String name = names.get( i );
				result[i] = new MakeExtra( name );
				
				result[i].setParameterName( parameterNames.get( name ) );
				result[i].setParameterValue( parameterValues.get( name ) );
				result[i].setAskParameterAtCompileTime( Boolean.parseBoolean( parameterAsk.get( name ) ) );
			}
			
			return result;
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex.getStatus() );
			return new MakeExtra[]{};
		}
	}
}
