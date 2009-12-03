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
package tinyos.yeti.environment.basic.commands;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A command executer executes {@link ICommand}s. 
 * @author Benjamin Sigg
 */
public interface ICommandExecuter{
    /**
     * Just executes <code>command</code>.
     * @param <R> the kind of result that is expected from <code>command</code>
     * @param command the command to work with
     * @return the result of the command or <code>null</code> if an error happens
     */
    public <R> R execute( ICommand<R> command ) throws InterruptedException, IOException;
    
    /**
     * Executes the command <code>command</code>.
     * @param command the command to execute
     * @param monitor a progress monitor is used to inform about progress, might be <code>null</code>
     * @param info to write information from the executer
     * @param out the standard output stream to write into, might be <code>null</code>
     * @param error the standard error stream, might be <code>null</code>
     * @return the result of the command
     */
    public <R> R execute(
            ICommand<R> command,
            IProgressMonitor monitor,
            OutputStream info,
            OutputStream out,
            OutputStream error ) throws InterruptedException, IOException;
}
