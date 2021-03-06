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

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;

import tinyos.yeti.make.EnvironmentVariable;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.make.MakeMacro;

public interface IPlatform {

	public String getName();
	
	public String getDescription();
	
	public ImageDescriptor getImage();
	
	public IMakeExtraDescription[] getExtras();
	
	public ISensorBoard[] getSensorboards();
	
	/**
	 * The default separator for C variables generated by the nested c compiler.
	 */
	public static final String DEFAULT_NESC_SEPARATOR = "$";
	
	/**
	 * Gets the includes set by the user for this platform.
	 * @return the includes set by the user
	 */
	public MakeInclude[] getDefaultIncludes();
	
	/**
	 * Gets includes automatically set by the system. These files will
	 * be included in any other file.
	 * @return includes set automatically by the system, can be <code>null</code>
	 */
	public File[] getGlobalIncludes();
	
	/**
	 * Gets the environment variables that are to be used when building
	 * an application.
	 * @return the variables, may be <code>null</code>
	 */
	public EnvironmentVariable[] getDefaultEnvironmentVariables();
	
	/**
	 * Gets the macros that should be used when compiling for this
	 * platform.
	 * @return the macros
	 */
	public MakeMacro[] getMacros();
	
	/**
	 * Adds a listener to this platform.
	 * @param listener the new listener
	 */
	public void addPlatformListener( IPlatformListener listener );
	
	/**
	 * Removes a listener from this platform.
	 * @param listener the listener to remove
	 */
	public void removePlatformListener( IPlatformListener listener );
	
    /**
     * The separator for C variables used by the nested C compiler for this platform.
     * The name of the (static) C variables generated by the nested C compiler is determined by concatenating the module name
     * and the variable name using a separator as delimiter. 
     * @return The separator or null if it could not be determined.
     */
    public String getNestedCVariableSeparator();
}
