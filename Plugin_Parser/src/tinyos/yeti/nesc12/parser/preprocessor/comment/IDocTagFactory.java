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
package tinyos.yeti.nesc12.parser.preprocessor.comment;

/**
 * Analyzes and creates new doc-tags.
 * @author Benjamin Sigg
 */
public interface IDocTagFactory{
	/**
	 * Creates a new tag.
	 * @param name the name of the tag
	 * @param description the description of this tag
	 * @param nameOffset the offset of the tag in the document
	 * @param descriptionChars indices of characters in <code>description</code>
	 * @param descriptionOffsets the offset of the characters denoted by <code>descriptionChars</code>
	 * @return the new tag
	 */
	public IDocTag create( String name, String description, int nameOffset, int[] descriptionChars, int[] descriptionOffsets );
}
