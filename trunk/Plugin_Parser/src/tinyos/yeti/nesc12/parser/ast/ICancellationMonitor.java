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
package tinyos.yeti.nesc12.parser.ast;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.nesc12.CancellationException;

/**
 * Used at places where an {@link IProgressMonitor} would be too much. A 
 * {@link ICancellationMonitor} can provide the client with an {@link IProgressMonitor}
 * if needed.
 * @author Benjamin Sigg
 */
public interface ICancellationMonitor{
    public static final ICancellationMonitor NULL = new ICancellationMonitor(){
        public IProgressMonitor getProgressMonitor(){
            return null;
        }
        public void checkCancellation() throws CancellationException{
            // do nothing
        }
    };
    
    /**
     * Gets an {@link IProgressMonitor}.
     * @return the new monitor
     */
    public IProgressMonitor getProgressMonitor();
    
    /**
     * Checks whether the operation is canceled and if so throws an exception
     * @throws CancellationException the exception thrown if the operation
     * was canceled
     */
    public void checkCancellation() throws CancellationException;
}
