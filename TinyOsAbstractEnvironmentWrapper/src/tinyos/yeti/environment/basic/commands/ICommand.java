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

import java.io.File;
import java.util.Map;

import tinyos.yeti.environment.basic.path.IPathManager;

/**
 * A {@link ICommand} is a set of commands which can be given to a terminal
 * like "bash" to execute some tool. A {@link ICommand} also contains code
 * to interpret the result of the tool and translate the result into Java-objects.<br>
 * Clients should call {@link #setup()} before executing a command, this method
 * will check whether the command can be executed or not.
 * 
 * @param <R> the kind of result this command will yield
 * @author Benjamin Sigg
 */
public interface ICommand<R>{
    /**
     * Gets the command that will be executed.
     * @return the command to execute, not <code>null</code>
     */
    public String[] getCommand();
    
    /**
     * Gets the environment parameters that will be set for execution. The format
     * of these strings is as follows: keys are plain names without whitespaces
     * or special signs. Values may contain whitespaces and special signs. Values
     * are formated such that they can be given to the application "env". This
     * means that e.g. the <code>"</code> is masked with <code>\"</code>. Values 
     * may or may not be encompassed by <code>"</code>.
     * @return the parameters, can be <code>null</code>
     */
    public Map<String,String> getEnvironmentParameters();
    
    /**
     * Whether the default parameters set by the {@link IPathManager#getEnvironmentVariables()}
     * should be used when executing this command or not.
     * @return <code>true</code> if the default parameters should be used
     */
    public boolean useDefaultParameters();
    
    /**
     * Gets the directory in which the command will be executed.
     * @return the directory, can be <code>null</code>
     */
    public File getDirectory();
    
    /**
     * Tells whether the command is expected to print something or not.
     * @return <code>true</code> if the command will print something onto
     * the console, <code>false</code> if not
     */
    public boolean shouldPrintSomething();
    
    /**
     * Whether this command should run in an interactive shell or not.
     * @return <code>true</code> if an interactive shell should be used
     */
    public boolean assumesInteractive();
    
    /**
     * Should be called before the command is executed. It is up to the command
     * what it does in here
     * 
     * Called by the {@link CommandManager} before this command gets executed.
     * @return <code>true</code> if setup was successful, <code>false</code> if
     * the execution should be canceled
     */
    public boolean setup();
    
    /**
     * Called after this command was executed. The command should return
     * its result.
     * @param result the result from the executed command
     * @return the result from this command, might be <code>null</code>
     */
    public R result( IExecutionResult result );
}
