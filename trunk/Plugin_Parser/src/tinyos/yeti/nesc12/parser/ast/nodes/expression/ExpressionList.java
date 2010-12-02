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
package tinyos.yeti.nesc12.parser.ast.nodes.expression;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.elements.Generic;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;

public class ExpressionList extends AbstractListASTNode<Expression> implements Expression{
    public ExpressionList(){
        super( "ExpressionList" );
    }
    
    public ExpressionList( Expression expression ){
        this();
        add( expression );
    }

    @Override
    public ExpressionList add( Expression node ) {
        super.add( node );
        return this;
    }
    
    @Override
    public ExpressionList addError( ErrorASTNode error ) {
        super.addError( error );
        return this;
    }
    
    public Type resolveType() {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        if( getChildrenCount() != 1 )
            return resolved( "type", null );
        
        Expression expr = getTypedChild( 0 );
        if( expr == null )
            return resolved( "type", null );
        
        return resolved( "type", expr.resolveType() );
    }
    
    public Generic resolveGeneric() {
        return resolveConstantValue();
    }
    
    public Value resolveConstantValue() {
        if( isResolved( "value" ))
            return resolved( "value" );
        
        if( getChildrenCount() != 1 )
            return resolved( "value", null );
        
        Expression expr = getTypedChild( 0 );
        if( expr == null )
            return resolved( "value", null );
        
        return resolved( "value", expr.resolveConstantValue() );
    }
    
    public Value[] resolveConstantValues() {
        if( isResolved( "values" ))
            return resolved( "values" );
        
        Value[] result = new Value[ getChildrenCount() ];
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            Expression expr = getNoError( i );
            if( expr != null ){
                result[i] = expr.resolveConstantValue();
            }
        }
        
        return resolved( "values", result );
    }
    
    @Override
    protected void checkChild( Expression child ) throws ASTException {
        
    }

    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }
    
    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }

    public boolean hasCommas() {
        return getChildrenCount() > 1;
    }

    public boolean isConstant() {
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            Expression child = getTypedChild( i );
            if( child != null ){
                if( !child.isConstant() )
                    return false;
            }
        }
        
        return true;
    }
}
