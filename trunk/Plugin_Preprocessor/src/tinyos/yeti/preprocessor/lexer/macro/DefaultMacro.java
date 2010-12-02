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
package tinyos.yeti.preprocessor.lexer.macro;

import tinyos.yeti.preprocessor.lexer.Macro;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.elements.Define;
import tinyos.yeti.preprocessor.parser.elements.TokenSequence;

public class DefaultMacro implements Macro{
    private String name;
    private String[] parameters;
    private VarArg vararg = VarArg.NO;
    
    private PreprocessorElement tokens = new TokenSequence();
    private boolean inProgress = false;
    
    public DefaultMacro( String name, String[] parameters, VarArg vararg, PreprocessorElement tokens ){
        setName( name );
        setParameters( parameters );
        setVararg( vararg );
        setTokenSequence( tokens );
    }
    
    public PreprocessorElement getLocation() {
        return null;
    }
    
    public boolean validSubstitution( Define define ) {
        return false;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }

    public String[] getParameters() {
        return parameters;
    }
    
    public void setParameters( String[] parameters ) {
        this.parameters = parameters;
    }

    public void setTokenSequence( PreprocessorElement tokens ) {
        this.tokens = tokens;
    }
    
    public PreprocessorElement getTokenSequence( PreprocessorElement element ){
        return tokens;
    }

    public VarArg getVarArg() {
        return vararg;
    }
    
    public void setVararg( VarArg vararg ) {
        this.vararg = vararg;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress( boolean progress ) {
        inProgress = progress;
    }
    
    public boolean requireInformNext(){
        return false;
    }
    
    public void informNext( String[] arguments ){
        // ignore
    }
}
