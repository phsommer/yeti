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
package tinyos.yeti.model;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.make.MakeTarget;

/**
 * Contains and creates all {@link IParseFile}s which are used by this plugin
 * and any client.
 * @author Benjamin Sigg
 */
public interface IFileModel{
    /**
     * Gets the parse file which represents <code>file</code>. If <code>file</code>
     * is unknown then a new {@link IParseFile} has to be created for it.
     * @param file the file that should be represented
     * @return the representation
     */
    public IParseFile parseFile( File file );
    
    /**
     * Gets the parse file which represents <code>file</code>. The resulting
     * files method {@link IParseFile#getPath()} must return <code>path</code>.
     * @param path the path of the file
     * @return the file
     */
    public IParseFile parseFile( String path );
    
    /**
     * Called when the make target of the project was updated. This model
     * should create a new list of accessible files.
     * @param target the new default make target
     * @param monitor to report progress
     */
    public void refresh( MakeTarget target, IProgressMonitor monitor );
    
    /**
     * Gets all known files with the given extension.
     * @param extension the extension
     * @return the list of files
     */
    public IParseFile[] getFiles( String extension );
    
    /**
     * Gets a list of all files, this list must not be related to the model
     * (i.e. deleting something in the list will not be reflected in the model). 
     * @return the list of files
     */
    public List<IParseFile> getAllFiles();
    
    /**
     * Clears any cache of this model. If <code>full</code>, then all
     * files have to be abandoned. If <code>false</code> only the files 
     * that are from the project have to be removed.
     * @param full whether to delete all files, or only those from the project
     * @param monitor to report progress
     */
    public void clear( boolean full, IProgressMonitor monitor );
}
