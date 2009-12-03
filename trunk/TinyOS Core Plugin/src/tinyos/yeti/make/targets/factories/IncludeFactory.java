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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.make.MakeInclude.Include;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class IncludeFactory implements IMakeTargetPropertyFactory<MakeInclude[]>{
	public boolean supportsXML(){
		return true;
	}
	
	public void write( MakeInclude[] value, XWriteStack xml ){
		for( MakeInclude include : value ){
			xml.push( "include" );
			xml.setAttribute( "recursive", include.isRecursive() ? "true" : "false" );
			// xml.setAttribute( "type", MakeInclude.type( include.getType() ) );
			xml.setAttribute( "ncc", include.isNcc() ? "true" : "false" );
			xml.setAttribute( "include", MakeInclude.include( include.getInclude() ) );
			xml.setAttribute( "global", include.isGlobal() ? "true" : "false" );
			xml.setText( include.getPath() );
			xml.pop();
		}
	}
	
	public MakeInclude[] read( XReadStack xml ){
		List<MakeInclude> includes = new ArrayList<MakeInclude>();
		while( xml.go( "include" ) ){
			String type = xml.getString( "type", null );
			boolean ncc = false;
			MakeInclude.Include include = MakeInclude.Include.NONE;
			boolean global = false;
			
			if( type != null ){
				if( "source".equals( type )){
					ncc = true;
					include = Include.SOURCE;
				}
				else if( "system".equals( type )){
					include = Include.SYSTEM;
				}
				else if( "global".equals( type )){
					global = true;
				}
			}
			else{
				ncc = xml.getBoolean( "ncc", false );
				include = MakeInclude.include( xml.getString( "include", MakeInclude.include( Include.NONE ) ) );
				global = xml.getBoolean( "global", false );
			}
			
			includes.add( 
					new MakeInclude(
							xml.getText(), include,
							xml.getBoolean( "recursive", false ), ncc, global ));
			xml.pop();
		}
		return includes.toArray( new MakeInclude[ includes.size() ]);
	}
	
	public void write( MakeInclude[] value,
			MakeTargetPropertyKey<MakeInclude[]> key,
			ILaunchConfigurationWorkingCopy configuration ){
	
		List<String> includesList = new ArrayList<String>();
		if( value != null ){
			for( MakeInclude include : value ){
				String recursive = include.isRecursive() ? "+" : "-";
				String ncc = include.isNcc() ? "t" : "f";
				String system;
				switch( include.getInclude() ){
					case NONE: system = "n"; break;
					case SOURCE: system = "s"; break;
					case SYSTEM: system = "y"; break;
					default: system = "n"; break;
				}
				String global = include.isGlobal() ? "t" : "f";
				
				includesList.add( recursive + "v" + ncc + system + global + include.getPath() );
			}
		}
	 	
	 	configuration.setAttribute( "tinyos." + key.getName(), includesList );
	}
	
	@SuppressWarnings("unchecked")
	public MakeInclude[] read( MakeTargetPropertyKey<MakeInclude[]> key,
			ILaunchConfiguration configuration ){
		
		try{
			List<String> includeList = configuration.getAttribute( "tinyos." + key.getName(), Collections.EMPTY_LIST );
			MakeInclude[] result = new MakeInclude[ includeList.size() ];
			
			for( int i = 0; i < result.length; i++ ){
				String include = includeList.get( i );
				
				boolean recursive = include.charAt( 0 ) == '+';
				boolean ncc = false;
				Include system = Include.NONE;
				boolean global = false;
				int cut = 2;
				
				switch( include.charAt( 1 )){
					case 'g': global = true; break;
					case 'o': ncc = true; system = Include.SOURCE; break;
					case 'y': system = Include.SYSTEM; break;
					case 'v':
						ncc = include.charAt( 2 ) == 't';
						switch( include.charAt( 3 )){
							case 'n': system = Include.NONE; break;
							case 's': system = Include.SOURCE; break;
							case 'y': system = Include.SYSTEM; break;
						}
						global = include.charAt( 4 ) == 't';
						cut = 5;
						break;
					default: throw new CoreException( new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, "unknown include type: " + include.charAt( 1 ) ));
				}
				include = include.substring( cut );
				
				result[i] = new MakeInclude( include, system, recursive, ncc, global );
			}
			
			return result;
		}
		catch( CoreException e ){
			TinyOSPlugin.log( e.getStatus() );
			return new MakeInclude[]{};
		}
	}
}
