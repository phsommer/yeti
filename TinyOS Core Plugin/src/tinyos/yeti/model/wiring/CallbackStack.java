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
package tinyos.yeti.model.wiring;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.INesCDefinitionCollector;
import tinyos.yeti.ep.parser.INesCDefinitionCollectorCallback;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.model.ProjectModel;

/**
 * A stack of {@link INesCDefinitionCollectorCallback}s. 
 * @author Benjamin Sigg
 */
public interface CallbackStack<C extends INesCDefinitionCollectorCallback> {
    /**
     * Gets the model of the whole project.
     * @return the model
     */
    public ProjectModel getModel();

    /**
     * Called from the top {@link INesCDefinitionCollectorCallback} when <code>declaration</code>
     * was found.
     * @param declaration the new declaration
     */
    public void declared( IDeclaration declaration );

    /**
     * Called when a new macro was found.
     * @param macro the new macro
     */
    public void defined( IMacro macro );
    
    /**
     * Called when a macro was undefined.
     * @param macro the name of the macro
     */
    public void undefined( String macro );
    
    /**
     * Called when a new file was included.
     * @param file the included file
     */
    public void included( IParseFile file );

    /**
     * Tries to get the callback that is responsible to load the file
     * for the element <code>name</code>.
     * @param name the name of the element to access
     * @param wiring if set, then only the wiring is interesting and
     * the result of this method may not contain any declarations
     * @param monitor used to cancel the operation
     * @param kind what kind of element to access
     * @return the callback or <code>null</code>
     */
    public C load( String name, boolean wiring, IProgressMonitor monitor, Kind... kind );

    /**
     * Tries to get the callback that is responsible to load the file
     * <code>file</code>.
     * @param file the file to load
     * @param wiring if set then only the wiring is interesting and
     * the result of this method may not contain any declarations
     * @param monitor used to cancel the operation
     * @return the callback or <code>null</code>
     */
    public C load( IParseFile file, boolean wiring, IProgressMonitor monitor );

    /**
     * Creates a new collector for the given file.
     * @param file the file which gets analyzed
     * @return the new collector or <code>null</code> if no collector is available
     * for <code>file</code>
     */
    public INesCDefinitionCollector createCollector( IParseFile file );

}
