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

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;

public class DataObjectFieldDeclaratorList extends AbstractListASTNode<DataObjectFieldDeclarator>{
    
    public DataObjectFieldDeclaratorList(){
        super( "DataObjectFieldDeclaratorList" );
    }
    
    public DataObjectFieldDeclaratorList( DataObjectFieldDeclarator child ){
        super( "DataObjectFieldDeclaratorList" );
        add( child );
    }

    /**
     * Tries to find all fields that are described in this list.
     * @param fields a list where new fields will be added, can be <code>null</code>
     * @return all the new fields, no <code>null</code> values. Either this
     * is <code>fields</code> or a new list if <code>fields</code> was <code>null</code>
     */
    public List<Field> resolveFields( List<Field> fields ){
        if( fields == null )
            fields = new ArrayList<Field>( getChildrenCount() );
        
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            DataObjectFieldDeclarator decl = getNoError( i );
            if( decl != null ){
                Field field = decl.resolveField();
                if( field != null )
                    fields.add( field );
            }
        }
        
        return fields;
    }
    
    public void resolveFields( Type base, AnalyzeStack stack ){
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            DataObjectFieldDeclarator decl = getNoError( i );
            if( decl != null ){
                decl.resolveField( base, stack );
            }
        }
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO Auto-generated method stub
        super.resolve( stack );
    }
    
    @Override
    public DataObjectFieldDeclaratorList add( DataObjectFieldDeclarator node ) {
        super.add( node );
        return this;
    }
    
    @Override
    protected void checkChild( DataObjectFieldDeclarator child )  throws ASTException {
        
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
