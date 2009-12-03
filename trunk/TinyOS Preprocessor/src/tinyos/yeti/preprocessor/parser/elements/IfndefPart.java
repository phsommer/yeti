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

import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

public class IfndefPart extends PreprocessorElement{
    private Identifier identifier;
    
    public IfndefPart( PreprocessorToken token, Identifier identifier ){
        super( token );
        this.identifier = identifier;
    }
    
    public String identifier(){
        return identifier.name();
    }
    
    public boolean valid(){
        return identifier != null;
    }
    
    @Override
    protected void toString( StringBuilder builder, int tabs ) {
        toString( builder, tabs, "ifndef", identifier() );
    }
    
    @Override
    public PreprocessorElement[] getChildren() {
        if( identifier == null )
            return null;
        
        return new PreprocessorElement[]{ identifier };
    }
    
    @Override
    public int getChildrenCount() {
        return 1;
    }
    
    @Override
    public PreprocessorElement getChild( int index ) {
        return identifier;
    }
}