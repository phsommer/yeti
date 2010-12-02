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
package tinyos.yeti.preprocessor;

import tinyos.yeti.preprocessor.lexer.Macro;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * A callback that gets called when a new macro is declared
 * @author Benjamin Sigg
 */
public interface MacroCallback{
    /**
     * Called when a new macro has been declared
     * @param macro the new macro
     */
    public void declared( Macro macro );
    
    /**
     * Called when a macro has been undeclared
     * @param name the name of the macro
     * @param macro the macro, if it was ever declared
     */
    public void undeclared( String name, Macro macro );
    
    /**
     * Called if <code>macro</code> is applied to the source code.
     * @param macro the macro that is applied
     * @param identifier the element that gets replaced by <code>macro</code>
     */
    public void applied( Macro macro, PreprocessorElement identifier );
}
