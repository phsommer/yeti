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
package tinyos.yeti.environment.basic.platform.mmcu;

import tinyos.yeti.environment.basic.path.IPlatformFile;
import tinyos.yeti.environment.basic.platform.MMCUConverter;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.macros.ConstantMacro;

public class MSP430 implements MMCUConverter{
	public boolean interested( IPlatformFile file ){
		return "msp430".equals( file.getArchitecture() );
	}
	
	public IMacro[] convert( IPlatformFile file ){
		// example: msp430xw425 would become __MSP430_W425__
		
		String mmcu = file.getMMCU();
		if( mmcu == null || !mmcu.startsWith( "msp430" ))
			return new IMacro[]{ new ConstantMacro( "__MSP430__", "1" ) };
		
		int index = mmcu.indexOf( "x" );
		if( index < 0 )
			return new IMacro[]{ new ConstantMacro( "__MSP430__", "1" ) };
		
		String append = mmcu.substring( index+1 ).toUpperCase();
		
		return new IMacro[]{
				new ConstantMacro( "__MSP430__", "1" ),
				new ConstantMacro( "__MSP430_" + append + "__", "1" )};
	}
}
