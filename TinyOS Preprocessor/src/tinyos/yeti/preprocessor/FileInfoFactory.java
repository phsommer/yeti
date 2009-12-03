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
 * A factory used to create new {@link FileInfo}s
 * @author Benjamin Sigg
 */
public interface FileInfoFactory{
    /**
     * Called by the #line directive when the name of a file changes. Note
     * that this method may be called more than once, especially that
     * <code>current</code> can be the result of this method.
     * @param current the current name of the file
     * @param newname the name that should be applied
     * @return a new info for the given file
     */
    public FileInfo createLine( FileInfo current, String newname );
}
