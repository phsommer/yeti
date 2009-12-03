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

public class AVR implements MMCUConverter{
	public boolean interested( IPlatformFile file ){
		return "avr".equals( file.getArchitecture() );
	}
	
	public IMacro[] convert( IPlatformFile file ){
		String mmcu = file.getMMCU();
		if( mmcu == null )
			return null;
		
		String name = null;
		if( mmcu.startsWith( "atmega" ))
			name = "ATmega" + mmcu.substring( 6 ).toUpperCase();
		else if( mmcu.startsWith( "attiny" ))
			name = "ATtiny" + mmcu.substring( 6 ).toUpperCase();
		else
			name = mmcu.toUpperCase();
		
		return new IMacro[]{ 
				new ConstantMacro( "__AVR_" + name + "__", "1" )
		};
	}
}
