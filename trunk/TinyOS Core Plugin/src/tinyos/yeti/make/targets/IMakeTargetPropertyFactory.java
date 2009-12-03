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
package tinyos.yeti.make.targets;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

/**
 * This factory is responsible of reading and writing one property.
 * @author Benjamin Sigg
 */
public interface IMakeTargetPropertyFactory<T>{
	/**
	 * Writes <code>value</code> into <code>out</code>.
	 * @param value the value to write
	 * @param out to write into
	 */
	public void write( T value, XWriteStack out );
	
	/**
	 * Reads a <code>T</code> from <code>in</code>.
	 * @param in the stream to read from
	 * @return a new <code>T</code>
	 */
	public T read( XReadStack in );
	
	/**
	 * Whether this factory supports writing or reading to or from xml.
	 * @return <code>true</code> if this factory can handle xml
	 */
	public boolean supportsXML();
	
	/**
	 * Writes <code>value</code> into <code>configuration</code>
	 * @param value the value to write, can be <code>null</code>
	 * @param key the key for which this factory is used
	 * @param configuration to write into
	 */
	public void write( T value, MakeTargetPropertyKey<T> key, ILaunchConfigurationWorkingCopy configuration );
	
	/**
	 * Reads a <code>T</code> from <code>configuration</code>.
	 * @param configuration to read from
	 * @param key the key for which this factory is used
	 * @return the value that has been read, might be <code>null</code>
	 */
	public T read( MakeTargetPropertyKey<T> key, ILaunchConfiguration configuration );
}
