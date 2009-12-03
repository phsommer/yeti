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

import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A test that can be run at any time. A test checks some setting of
 * an environment for its correctness.
 * @author Benjamin Sigg
 */
public interface ITest {
    /**
     * Gets a short name for this test.
     * @return the name
     */
    public String getName();
    
    /**
     * Gets a description for this test.
     * @return a description
     */
    public String getDescription();
    
    /**
     * Runs this test and tells whether it was successful or not
     * @param out stream to write standard output messages
     * @param err stream to write standard error messages
     * @param monitor to inform of the progress
     * @return how the test ended
     * @throws Exception if something goes wrong
     */
    public IStatus run( OutputStream out, OutputStream err, IProgressMonitor monitor ) throws Exception;
}
