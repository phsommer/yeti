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
package tinyos.yeti.make;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.ep.ISensorBoard;
import tinyos.yeti.ep.MakeExtra;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.make.targets.IMakeTargetMorpheable;

/**
 * Represents a make target: the options given to ncc when compiling a project.<br>
 * Note: this interface is not intended to be implemented by clients.
 */
public interface IMakeTarget extends IMakeTargetMorpheable{
    /**
     * Gets the TinyOS project associated with this make target
     * @return the tinyos project
     */
    public ProjectTOS getProjectTOS();
    
    /**
     * Gets a list of command arguments for Make, this includes the
     * target and the make extras.
     * @return the arguments for Make
     */
    public List<String> getMakeCommands();
    
    /**
     * Gets a list of commands which could be given to ncc when working with
     * this target. This list of commands is most useful when calling a tool
     * like ncg.
     * @return the list of commands for nescc
     */
    public List<String> getNesccFlags();
    
    /**
     * Gets a list of commands that should be given to ncc. These flags can
     * be put into a makefile or set as environment variable called "PFLAGS".
     * @return the content of PFLAGS
     */
    public List<String> getPFlags();
    
    /**
     * Gets the name for this target, this is the name presented to the user.
     * @return the user friendly name
     */
    public String getName();
    
    /**
     * Gets a unique identifier for this target, unique only within
     * the current project.
     * @return the unique identifier
     */
    public String getId();
    
    /**
     * Gets the target node, for example 'tinynode'.
     * @return the target node, might be <code>null</code>
     */
    public String getTarget();
    
    /**
     * Gets the platform which is used by this target.
     * @return the platform, might be <code>null</code>
     */
    public IPlatform getPlatform();
    
    /**
     * Gets additional extras for this target.
     * @return additional activated extras
     */
    public MakeExtra[] getMakeExtras();
    
    /**
     * Gets all includes which are to be used when working with this target.
     * @return all includes
     */
    public MakeInclude[] getIncludes();
    
    /**
     * Gets the set of directories which must not be used in searches for any
     * file and not be given to ncc. These excludes override any setting made
     * by an include-rule.
     * @return the exclude rules, can be <code>null</code>
     */
    public MakeExclude[] getExcludes();
    
    /**
     * Gets a set of typedefs to be used in any file, but only by the plugin.
     * @return the set of typedefs
     */
    public MakeTypedef[] getTypedefs();
    
    public IMacro[] getMacros();

    public String[] getBoards();
    
    /**
     * Gets a set of environment variables to be used when compiling the project.
     * @return the variables, might be <code>null</code>
     */
    public EnvironmentVariable[] getEnvironmentVariables();
    
    /**
     * Gets the list of sensor boards which are known and used by this option.
     * @return the list of boards
     */
    public ISensorBoard[] getSensorBoards();
    
    public boolean isNostdinc();
    public double getLoopTime();
    public boolean getLoop();
    
    /**
     * Gets the path to the component which is the toplevel component of the application.
     * This path starts at the projects source directory and goes to the file that represents
     * the component without the ending *.nc.
     * @return most often a name of a configuration, but might also be another
     * component
     * @deprecated replaced by {@link #getComponentFile()}
     */
    @Deprecated
    public String getComponent();

    /**
     * Gets the file which is referenced by {@link #getComponent()}.
     * @return path to the component
     */
    public IFile getComponentFile();
    
    /**
     * Checks whether the target is ready to be executed. A status be of any
     * severity, but an error almost certainly guarantees a failure.
     * @return the readiness
     */
    public IStatus ready();
}