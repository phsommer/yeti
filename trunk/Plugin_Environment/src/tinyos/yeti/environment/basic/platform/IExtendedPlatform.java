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

import java.io.File;

import tinyos.yeti.environment.basic.path.IPlatformFile;
import tinyos.yeti.ep.IPlatform;

/**
 * An extended platform knows its origin.
 * @author Benjamin Sigg
 */
public interface IExtendedPlatform extends IPlatform{
    /**
     * Gets the directory in which the platform files lie
     * @return the platforms directory, might be <code>null</code>
     */
    public File getDirectory();
    
    /**
     * Gets <code>file</code> as platform file.
     * @param file the file to read
     * @return the contents of <code>file</code>
     */
    public IPlatformFile getPlatformFile();
}
