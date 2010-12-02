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
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.macros.ConstantMacro;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class MacroFactory implements IMakeTargetPropertyFactory<IMacro[]>{
	public boolean supportsXML(){
		return true;
	}
	
	public void write( IMacro[] value, XWriteStack xml ){
		if( value instanceof ConstantMacro[] ){
			for( ConstantMacro macro : (ConstantMacro[])value ){
				xml.push( "macro" );
				xml.setAttribute( "name", macro.getName() );
				xml.setAttribute( "content", macro.getConstant() );
				xml.pop();
			}
		}
	}
	public ConstantMacro[] read( XReadStack xml ){
		List<ConstantMacro> macros = new ArrayList<ConstantMacro>();
		
		while( xml.go( "macro" )){
			macros.add( 
					new ConstantMacro(
							xml.getString( "name", "name" ),
							xml.getString( "content", "content" )
					));
			xml.pop();
		}
		
		return macros.toArray( new ConstantMacro[ macros.size() ] );
	}
	
	public void write( IMacro[] value, MakeTargetPropertyKey<IMacro[]> key,
			ILaunchConfigurationWorkingCopy configuration ){

	 	ConstantMacro[] macros;
	 	if( value instanceof ConstantMacro[] )
	 		macros = (ConstantMacro[])value;
	 	else
	 		macros = null;
	 	
	 	List<String> macroNames = new ArrayList<String>();
	 	List<String> macroValues = new ArrayList<String>();
	 	if( macros != null ){
	 		for( ConstantMacro macro : macros ){
	 			macroNames.add( macro.getName() );
	 			macroNames.add( macro.getConstant() );
	 		}
	 	}
	 	configuration.setAttribute( "tinyos." + key.getName() + ".names", macroNames );
	 	configuration.setAttribute( "tinyos." + key.getName() + ".values", macroValues );
	}
	
	@SuppressWarnings("unchecked")
	public IMacro[] read( MakeTargetPropertyKey<IMacro[]> key,
			ILaunchConfiguration configuration ){
		
		try{
			List<String> macroNames = configuration.getAttribute( "tinyos." + key.getName() + ".names", Collections.EMPTY_LIST );
			List<String> macroValues = configuration.getAttribute( "tinyos." + key.getName() + ".values", Collections.EMPTY_LIST );
			
			ConstantMacro[] result = new ConstantMacro[ Math.min( macroNames.size(), macroValues.size() )];
			for( int i = 0; i < result.length; i++ ){
				result[i] = new ConstantMacro( macroNames.get( i ), macroValues.get( i ));
			}
			return result;
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex.getStatus() );
			return new ConstantMacro[]{};
		}
	}
}
