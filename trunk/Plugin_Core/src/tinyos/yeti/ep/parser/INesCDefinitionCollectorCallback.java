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

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.model.ProjectModel;

/**
 * Used to interact with a {@link INesCDefinitionCollector}.
 * @author Benjamin Sigg
 */
public interface INesCDefinitionCollectorCallback{
    /**
     * Called when the inclusion of some element (interface, component)
     * was found.
     * @param name the name of the included element
     * @param monitor used to cancel this method
     * @param kind a guess what kind of element would be included, the list
     * should be short but the correct kind should be in the list
     */
    public void elementIncluded( String name, IProgressMonitor monitor, Kind... kind );
    
    /**
     * Should be called when a file has been included.
     * @param name the name of the file which might not be the whole path
     * @param requireLoad if <code>true</code> then the included file gets
     * parsed and loaded the same way {@link #elementIncluded(String, Kind...)} would
     * react. If not set then this callback just stores the information about
     * the additional included file
     * @param monitor used to cancel this method
     * @see ProjectModel#parseFile(java.io.File)
     * @see ProjectModel#parseFile(org.eclipse.core.resources.IResource)
     */
    public void fileIncluded( String name, boolean requireLoad, IProgressMonitor monitor );
    
    /**
     * Should be called when a file has been included.
     * @param file the included file
     * @param requireLoad if <code>true</code> then the included file gets
     * parsed and loaded the same way {@link #elementIncluded(String, Kind...)} would
     * react. If not set then this callback just stores the information about
     * the additional included file
     * @param monitor used to cancel this method
     * @see ProjectModel#parseFile(java.io.File)
     * @see ProjectModel#parseFile(org.eclipse.core.resources.IResource)
     */
    public void fileIncluded( File file, boolean requireLoad, IProgressMonitor monitor );
    
    /**
     * Called when a new declaration (field, function, typedef, not interface
     * or component) was found.
     * @param declaration the new declaration
     */
    public void declarationFound( IDeclaration declaration );
    
    /**
     * Called when a macro has been declared
     * @param macro the new macro
     */
    public void macroDefined( IMacro macro );
    
    /**
     * Called when a macro has been undeclared.
     * @param name the name of the undeclared macro, might not exist
     */
    public void macroUndefined( String name );
}
