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


/**
 * This factory is responsible of reading and writing one property.
 * @author Benjamin Sigg
 */
public interface IStringMakeTargetPropertyFactory<T>{
	/**
	 * Writes <code>value</code> as string.
	 * @param value the value to write
	 * @return the value as string
	 */
	public String write( T value );
	
	/**
	 * Reads a <code>T</code> from <code>content</code>.
	 * @param content the content to read
	 * @return a new <code>T</code>
	 */
	public T read( String content );
	
	/**
	 * Creates an array.
	 * @param size the size of the array
	 * @return the new array
	 */
	public T[] array( int size );
}
