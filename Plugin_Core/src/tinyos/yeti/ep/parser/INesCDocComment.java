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
package tinyos.yeti.ep.parser;

/**
 * A comment for some {@link IASTModelNode}
 * @author Benjamin Sigg
 */
public interface INesCDocComment{
	/**
	 * Gets the documentation of an element.
	 * @return the documentation
	 */
	public String getComment();
	
	/**
	 * Gets the path to the element that is documented
	 * @return the path to the owner of this comment
	 */
	public IASTModelPath getPath();
}
