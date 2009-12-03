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

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.preprocessor.parser.ElementVisitor;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

public class IdentifierList extends PreprocessorElement {
    private List<Identifier> identifiers = new ArrayList<Identifier>();
    
    public IdentifierList(){
        super( null );
    }
    
    public IdentifierList( Identifier initial ){
        super( null );
        identifiers.add( initial );
    }
    
    public List<Identifier> identifiers(){
        return identifiers;
    }
    
    @Override
    protected void toString( StringBuilder builder, int tabs ) {
        toString( builder, tabs, "identifier_list", null, identifiers );
    }
    
    @Override
    public void visit( ElementVisitor visitor ) {
        visitor.visit( this );
        for( Identifier id : identifiers )
            id.visit( visitor );
        visitor.endVisit( this );
    }
    
    @Override
    public void visitReverse( ElementVisitor visitor ) {
        visitor.visit( this );
        for( int i = identifiers.size(); i >= 0; i-- )
            identifiers.get( i ).visit( visitor );
        visitor.endVisit( this );
    }
    
    @Override
    public PreprocessorElement[] getChildren() {
        return identifiers.toArray( new PreprocessorElement[ identifiers.size() ] );
    }
    
    @Override
    public int getChildrenCount() {
        return identifiers.size();
    }
    
    @Override
    public PreprocessorElement getChild( int index ) {
        return identifiers.get( index );
    }
}
