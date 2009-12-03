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

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An {@link IncludeProvider} is used to resolve the #include directive.
 * @author Benjamin Sigg
 */
public interface IncludeProvider {
	/**
     * Searches a file that was included through <code>#include "filename"</code>. 
     * @param filename the name of the file
     * @param monitor used to inform about progress and to cancel this operation
     * @return the file or <code>null</code> if the file was not
     * found
     */
    public IncludeFile searchUserFile( String filename, IProgressMonitor monitor );
    
    /**
     * Searches a file that was included through <code>#include &lt;filename&gt;</code>. 
     * @param filename the name of the file
     * @param monitor used to inform about progress and to cancel this operation
     * @return the file or <code>null</code> if the file was not
     * found
     */
    public IncludeFile searchSystemFile( String filename, IProgressMonitor monitor );
}
