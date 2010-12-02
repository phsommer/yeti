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
package tinyos.yeti.nesc12.parser.ast.nodes.declaration;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleField;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Flag;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class IdentifierList extends AbstractListASTNode<Identifier> {
    public static final Flag STORE_UNTYPED_FIELDS = new Flag( "identifier list stores untyped fields" );
    
    public IdentifierList(){
        super( "IdentifierList" );
    }
    
    public IdentifierList( Identifier child ){
        this();
        add( child );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        
        if( stack.presentLevel( STORE_UNTYPED_FIELDS )){
            stack.remove( STORE_UNTYPED_FIELDS );
            super.resolve( stack );
            stack.checkCancellation();
            stack.put( STORE_UNTYPED_FIELDS );
            
            for( int i = 0, n = getChildrenCount(); i<n; i++ ){
                Identifier id = getTypedChild( i );
                if( id != null ){
                    stack.putField( new SimpleField( null, null,  stack.name( id ), null, null, null, null ), id.getRange().getRight(), false );
                }
            }
        }
        else{
            super.resolve( stack );
        }
    }
    
    @Override
    public IdentifierList add( Identifier node ) {
        super.add( node );
        return this;
    }
    
    @Override
    protected void checkChild( Identifier child ) throws ASTException {
        
    }

    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }

    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }
}
