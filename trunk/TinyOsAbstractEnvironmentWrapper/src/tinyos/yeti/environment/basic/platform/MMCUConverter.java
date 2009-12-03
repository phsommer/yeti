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
package tinyos.yeti.environment.basic.platform;

import tinyos.yeti.environment.basic.path.IPlatformFile;
import tinyos.yeti.ep.parser.IMacro;

/**
 * An {@link MMCUConverter} is responsible to convert the MMCU of a platform
 * file to a set of macros. These macros will be applied to all source files.
 * @author Benjamin Sigg
 */
public interface MMCUConverter {
	/**
	 * Tells whether this converted knows how to read <code>file</code>.
	 * @param file some platform file
	 * @return <code>true</code> if this converter knows how to handle <code>file</code>
	 */
	public boolean interested( IPlatformFile file );
	
	/**
	 * Converts the MMCU of <code>file</code> into a set of macros.
	 * @param file the file to analyze 
	 * @return the macros of the file
	 */
	public IMacro[] convert( IPlatformFile file );
}
