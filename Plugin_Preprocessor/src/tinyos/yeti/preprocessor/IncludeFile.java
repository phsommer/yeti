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

import java.io.Reader;

/**
 * A representation of a file that gets included into another file. 
 * @author Benjamin Sigg
 */
public interface IncludeFile {
    /**
     * Opens a reader for this file.
     * @return the reader or <code>null</code> if the file can't be read.
     */
    public Reader read();

    
    /**
     * Gets the full path to this file. The path can be used to find
     * the file again by a {@link IncludeProvider}.
     * @return the path
     */
    public FileInfo getFile();
}
