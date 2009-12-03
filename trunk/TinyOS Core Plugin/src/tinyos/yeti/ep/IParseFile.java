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
package tinyos.yeti.ep;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IAdaptable;

import tinyos.yeti.ProjectTOS;


/**
 * Information about a file that gets parsed.<br>
 * <b>Note</b>: this interface is not intended to be implemented by clients
 * @author Benjamin Sigg
 */
public interface IParseFile extends IAdaptable{
    /**
     * Gets the unique path to the file that is describes by this.
     * @return the unique path
     */
    public String getPath();
    
    /**
     * Gets the name of this file.
     * @return the name
     */
    public String getName();
    
    /**
     * Gets the index for the time when this file is checked when searching
     * an element for inclusion.
     * @return the index
     */
    public int getIndex();
    
    /**
     * A project file is a file that is part of the project. A project file
     * can be edited by the user while a non-project file is static.
     * @return <code>true</code> if this file is from the project
     */
    public boolean isProjectFile();
    
    /**
     * Gets the source container in which this file is defined, only
     * applicable to {@link #isProjectFile() project files}.
     * @return the source folder, can be <code>null</code>
     */
    public IContainer getProjectSourceContainer();
    
    /**
     * Returns the project to which this file belongs. Note that the project
     * might not be known for some files.
     * @return the project or <code>null</code>
     */
    public ProjectTOS getProject();
    
    /**
     * Transforms this file into a real file
     * @return the file
     */
    public File toFile();
}
