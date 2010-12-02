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
package tinyos.yeti.nesc12.collector;

import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.preprocessor.lexer.Macro;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.elements.Define;

/**
 * A wrapper around a {@link Macro}, only intended to be used as {@link Macro}.
 * @author Benjamin Sigg
 */
public class MacroWrapper implements IMacro, Macro{
    private Macro macro;
    
    public MacroWrapper( Macro macro ){
        this.macro = macro;
    }
    
    public int getArgumentCount(){
        return 0;
    }
    
    public boolean isFunctionMacro() {
        return false;
    }

    public String getName(){
        return macro.getName();
    }

    public boolean isVararg(){
        return false;
    }

    public String run( String... arguments ){
        return "";
    }

    public PreprocessorElement getLocation(){
        return null;
    }

    public String[] getParameters(){
        return macro.getParameters();
    }

    public PreprocessorElement getTokenSequence( PreprocessorElement replacing ){
        return macro.getTokenSequence( replacing );
    }

    public VarArg getVarArg(){
        return macro.getVarArg();
    }

    public void informNext( String[] arguments ){
        macro.informNext( arguments );
    }

    public boolean isInProgress(){
        return macro.isInProgress();
    }

    public boolean requireInformNext(){
        return macro.requireInformNext();
    }

    public void setInProgress( boolean progress ){
        macro.setInProgress( progress );
    }

    public boolean validSubstitution( Define define ){
        return macro.validSubstitution( define );
    }
}
