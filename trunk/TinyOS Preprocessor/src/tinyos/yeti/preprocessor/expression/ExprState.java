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
package tinyos.yeti.preprocessor.expression;

import tinyos.yeti.preprocessor.lexer.State;
import tinyos.yeti.preprocessor.output.Insight;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

public class ExprState {
    private State states;
    private PreprocessorElement element;
    private boolean valid = true;
    
    public ExprState( State states, PreprocessorElement element ){
        this.states = states;
        this.element = element;
    }
    
    public State getStates() {
        return states;
    }
    
    public void reportError( String message, Insight insight ){
        states.reportError( message, insight, element );
        invalidate();
    }
    
    public void reportWarning( String message, Insight insight ){
        states.reportWarning( message, insight, element );
    }
    
    public void reportMessage( String message, Insight insight ){
        states.reportMessage( message, insight, element );
    }
    
    /**
     * Tells whether a {@link Lazy} found an error or all lazies were
     * executed normally.
     * @return <code>true</code> if at least one error happend
     */
    public boolean isValid() {
        return valid;
    }
   
    public void invalidate(){
        valid = false;
    }
}
