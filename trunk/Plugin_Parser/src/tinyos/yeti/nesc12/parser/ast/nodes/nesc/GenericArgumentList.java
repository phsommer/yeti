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
import tinyos.yeti.nesc12.parser.ast.elements.Generic;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;

public class GenericArgumentList extends AbstractListASTNode<GenericArgument>{
    public GenericArgumentList(){
        super( "GenericArgumentList" );
    }
    
    public GenericArgumentList( GenericArgument child ){
        this();
        add( child );
    }
    
    /**
     * Tries to find out what kind of arguments in this list are.
     * @return the array of arguments, contains <code>null</code> for 
     * erroneous arguments.
     */
    public Generic[] resolveGenericArguments(){
        if( isResolved( "generic" ))
            return resolved( "generic" );
        
        Generic[] result = new Generic[ getChildrenCount() ];
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            GenericArgument arg = getNoError( i );
            if( arg != null )
                result[i] = arg.resolveGeneric();
        }
        
        return resolved( "generic", result );
    }
    
    @Override
    public GenericArgumentList add( GenericArgument node ) {
        super.add( node );
        return this;
    }
    
    @Override
    protected void checkChild( GenericArgument child ) throws ASTException {
        
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
