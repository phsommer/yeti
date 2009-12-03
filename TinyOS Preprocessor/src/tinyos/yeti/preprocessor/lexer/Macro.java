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
package tinyos.yeti.preprocessor.lexer;

import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.elements.Define;

/**
 * A macro is a token written in the original source that will replace
 * some of the source by new tokens.
 * @author Benjamin Sigg
 */
public interface Macro {
    public static enum VarArg{
        NO, YES_UNNAMED, YES_NAMED
    }
    
    /**
     * Tries to get a {@link PreprocessorElement} that can be used in
     * an error message. 
     * @return the element or <code>null</code>
     */
    public PreprocessorElement getLocation();
    
    /**
     * Checks whether <code>this</code> and <code>define</code> define
     * the same macro.
     * @param define another macro to check
     * @return <code>true</code> if <code>this</code> could be replaced by <code>define</code>
     * without anyone noticing the exchange.
     */
    public boolean validSubstitution( Define define );
    
    /**
     * Gets the name of this macro.
     * @return the name
     */
    public String getName();
    
    /**
     * Gets the list of parameter names for this macro.
     * @return the list of parameters, <code>null</code> indicates that 
     * this is not a function-macro, while an array of length 0 indicates
     * that this is a function-macro with no arguments 
     */
    public String[] getParameters();
    
    /**
     * Gets the kind of variable arguments this macro uses.
     * @return the kind of variable arguments
     */
    public VarArg getVarArg();
    
    /**
     * Gets the replacement for the tokens that has been identifier
     * to call this macro.
     * @param replacing the element which will be replaced by this macro
     * @return the replacement
     */
    public PreprocessorElement getTokenSequence( PreprocessorElement replacing );

    /**
     * Tells whether this macro is currently in use.
     * @return <code>true</code> if this macro is in use
     */
    public boolean isInProgress();
    
    /**
     * Sets whether this macro is currently in use or not.
     * @param progress whehter this macro is in use
     */
    public void setInProgress( boolean progress );
    
    /**
     * Tells whether {@link #informNext(String[])} should be called.
     * @return <code>true</code> if {@link #informNext(String[])} should be called
     */
    public boolean requireInformNext();
    
    /**
     * Informs this macro that it will now be used with the given arguments. Most
     * macros can just ignore this method
     * @param arguments the arguments
     */
    public void informNext( String[] arguments );
}
