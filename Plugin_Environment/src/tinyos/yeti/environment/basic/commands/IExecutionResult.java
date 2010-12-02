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

/**
 * The result of an execution of an {@link ICommand}, can be given to
 * {@link ICommand#result(IExecutionResult)} to advise the command finishing
 * its work.
 * @author Benjamin Sigg
 */
public interface IExecutionResult{
    /**
     * Gets the output of the executed command.
     * @return the output
     */
    public String getOutput();
    
    /**
     * Gets the error output of the executed command.
     * @return the error output
     */
    public String getError();
    
    /**
     * Gets the exit state of the executed command.
     * @return the exit state
     */
    public int getExitValue();
}
