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
package tinyos.yeti.ep.parser;

/**
 * A macro represents some directive which would be executed by the
 * preprocessor.
 * @author Benjamin Sigg
 */
public interface IMacro{
    /**
     * Gets the name of this macro.
     * @return the name
     */
    public String getName();
    
    /**
     * Gets the number of arguments this macro needs
     * @return the number of arguments
     * @see #isFunctionMacro()
     */
    public int getArgumentCount();
    
    /**
     * Whether this is a function or not.
     * @return <code>true</code> if this macro requires arguments
     */
    public boolean isFunctionMacro();
    
    /**
     * Tells whether this macro needs a variable number of arguments. If so,
     * then {@link #getArgumentCount()} tells how many arguments this macro
     * needs at least.
     * @return whether variable arguments can be used
     */
    public boolean isVararg();
    
    /**
     * Runs this macro.
     * @param arguments the arguments
     * @return the result, not <code>null</code>
     */
    public String run( String... arguments );
}
