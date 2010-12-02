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
package tinyos.yeti.preprocessor.parser.elements;

import tinyos.yeti.preprocessor.lexer.PurgingReader;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

public class Source extends TokenSequence{
    private PurgingReader input;
    
    public Source() {
        super();
    }

    public Source( PreprocessorElement... token ) {
        super( token );
    }

    public Source( PreprocessorElement token ) {
        super( token );
    }
    
    /**
     * Sets the reader which was used to create this source
     * @param input the reader, can be <code>null</code>
     */
    public void setInput( PurgingReader input ){
        this.input = input;
    }
    
    /**
     * Gets the reader that was used for creation of this source.
     * @return the reader, can be <code>null</code>
     */
    public PurgingReader getInput(){
        return input;
    }
    
    @Override
    protected void toString( StringBuilder builder, int tabs ) {
        toString( builder, tabs, "source", null, tokens() );
    }
}
