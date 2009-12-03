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
package tinyos.yeti.nesc12.parser.meta;

/**
 * Describes the distance between elements.
 * @author besigg
 *
 * @param <N> the elements
 */
public interface Distance<N> {
	/**
	 * Calculates the distance between <code>a</code> and <code>b</code>.
	 * @param a the first element
	 * @param b the second element
	 * @return a number greater than 0, {@link Double#POSITIVE_INFINITY} is a 
	 * valid result. Do not return to great numbers: some algorithms sum up 
	 * the numbers and might have an overflow.
	 */
	public double distance( N a, N b );
}
