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
package tinyos.yeti.environment.basic.platform;

import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.EnvironmentVariable;
import tinyos.yeti.make.MakeInclude;

/**
 * A platform manager can tell which platforms are available.
 * @author Benjamin Sigg
 */
public interface IPlatformManager{
    /**
     * Gets the available platforms.
     * @return the platforms
     */
    public IPlatform[] getPlatforms();
    
    /**
     * Gets a list of includes that should be available in all platforms.
     * @return the list of includes, can be <code>null</code> or empty
     */
    public MakeInclude[] getDefaultMakeIncludes();
    
    /**
     * Gets a list of environment variables that should be set on all platforms.
     * @return the list of variables, may be <code>null</code> or empty
     */
    public EnvironmentVariable[] getDefaultEnvironmentVariables();
}
