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

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc.IMultiReader;

/**
 * Used during startup to collect the declarations of elements that can
 * be included indirectly into other files.
 * @author Benjamin Sigg
 */
public interface INesCInitializer {
    /**
     * Informs this initializer that some macro is used within the files.
     * @param macro the macro that is used
     */
    public void addMacro( IMacro macro );
    
    /**
     * Collects all declarations that can be included indirectly into
     * other files (like interface, modules, configurations, ...). The
     * {@link Kind} of declarations should only be {@link IDeclaration.Kind#isUnincludedAccess()}. 
     * @param parseFile the name of the file that gets analyzed
     * @param reader the content to analyze
     * @param monitor used to inform the user about progress or to cancel the operation
     * @return the new declarations or <code>null</code> if none were found
     * or an error happened
     * @throws IOException if the content can't be accessed
     */
    public IDeclaration[] analyze( IParseFile parseFile, IMultiReader reader, IProgressMonitor monitor ) throws IOException;
}
