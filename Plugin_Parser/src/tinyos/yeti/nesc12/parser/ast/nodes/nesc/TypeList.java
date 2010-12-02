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
package tinyos.yeti.nesc12.parser.ast.nodes.nesc;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypeName;

public class TypeList extends AbstractListASTNode<TypeName>{
    public TypeList(){
        super( "TypeList" );
    }
    
    public TypeList( TypeName child ){
        this();
        add( child );
    }
    
    /**
     * Gets an array containing all the types this list specifies. The array
     * contains <code>null</code> entries if errors were detected.
     * @return the array of types containing <code>null</code> entries
     */
    public Type[] resolveTypes(){
        if( isResolved( "types" ))
            return resolved( "types" );
        
        Type[] types = new Type[ getChildrenCount() ];
        
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            TypeName child = getNoError( i );
            if( child != null ){
                types[i] = child.resolveType();
            }
        }
        
        return resolved( "types", types );
    }
    
    @Override
    public TypeList add( TypeName node ) {
        super.add( node );
        return this;
    }
    
    @Override
    protected void checkChild( TypeName child ) throws ASTException {
        
    }
    
    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }

    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
}
