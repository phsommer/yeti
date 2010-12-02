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
 * Interface to order and hide properties of a {@link IMakeTargetProperties}.
 * @author Benjamin Sigg
 */
public interface IMakeTargetPropertyComparator<T>{
	public enum Order{
		EQUAL,
		SMALLER,
		GREATER,
		HIDE_FIRST,
		HIDE_SECOND,
		HIDE_BOTH
	}
	
	/**
	 * Compares <code>first</code> with <code>second</code>. The array
	 * containing these elements will be ordered according to the result
	 * of this method. If either of the two elements gets a "hide" order, it
	 * will be removed from the array. One "hide" order is enough, it is not
	 * necessary that all calls to {@link #compare(Object, Object)} return "hide".<br>
	 * Please note that the underlying array already has an order that comes
	 * from the {@link IMakeTargetProperties#getPriority() priority}. Also 
	 * the properties specified in a child are in front of the properties specified
	 * in the parent.
	 * @param first the first object
	 * @param second the second object
	 * @return the order of the objects
	 */
	public Order compare( T first, T second );
}
