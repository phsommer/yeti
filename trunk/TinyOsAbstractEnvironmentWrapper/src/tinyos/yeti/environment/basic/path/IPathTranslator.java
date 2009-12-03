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
package tinyos.yeti.environment.basic.path;

import java.io.File;

import tinyos.yeti.ep.IEnvironment;

/**
 * Can translate paths to files and the other direction as well. This
 * interface just cuts out {@link IEnvironment#systemToModel(File)}
 * and {@link IEnvironment#modelToSystem(String)}.
 * @author Benjamin Sigg
 */
public interface IPathTranslator {
	/**
     * Transforms a path that is used within the model to a file that
     * can be seen on the hard disk.<br>
     * Environments that do not work within a "sandbox" can just
     * return <code>new File( file );</code>.
     * @param file the internal name of the file
     * @return the real file
     */
    public File modelToSystem( String file );
    
    /**
     * Transforms a file that is used on real hard disk to a path
     * as it can be seen within the sandbox in which tinyos runs. Some
     * environments will just return <code>file.getPath()</code>.
     * @param file the file to transform
     * @return the internal path of <code>file</code>
     */
    public String systemToModel( File file );
}
