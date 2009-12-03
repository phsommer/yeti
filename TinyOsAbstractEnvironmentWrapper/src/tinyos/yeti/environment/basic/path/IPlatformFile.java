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
package tinyos.yeti.environment.basic.path;


/**
 * A platform file provides additional information for a platform. They
 * are named ".platform" or ".family" in the file system.
 * @author Benjamin Sigg
 */
public interface IPlatformFile{
    /**
     * Reads the contents of and returns the additional
     * inclusions that were found in file.
     * @return the additional inclusions, in the format of an -I directive
     */
    public String[] getIncludes();
    
    /**
     * Gets the processor the platform is using, for example "avr" or "msp430".
     * @return the processor or <code>null</code> if not found
     */
    public String getArchitecture();
    
    /**
     * Gets the value of the MMCU option. This can be something like "atmega128".
     * @return the mmcu, may be <code>null</code>
     */
    public String getMMCU();
}
