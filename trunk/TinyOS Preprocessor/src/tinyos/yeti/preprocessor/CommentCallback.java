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
package tinyos.yeti.preprocessor;

/**
 * Gets informed about the position and the content of comments (single line,
 * multi line and NesC-Doc comments included). Since comments are filtered
 * out before the preprocessor is applied they have only an offset in the
 * input file and their length is not affected by any macro.
 * @author Benjamin Sigg
 */
public interface CommentCallback{
	/**
	 * A single line comment, without the newline character.
	 * @param offsetInFile offset of the comment in the original file
	 * @param file the underlying file
	 * @param comment the comment itself
	 * @param topLevel if <code>true</code>, then the comment comes from the top level file
	 */
	public void singleLineComment( int offsetInFile, FileInfo file, String comment, boolean topLevel );

	/**
	 * A multi line comment.
	 * @param offsetInFile offset of the comment in the original file
	 * @param file the underlying file
	 * @param comment the comment itself
	 * @param topLevel if <code>true</code>, then the comment comes from the top level file
	 */
	public void multiLineComment( int offsetInFile, FileInfo file, String comment, boolean topLevel );
}
