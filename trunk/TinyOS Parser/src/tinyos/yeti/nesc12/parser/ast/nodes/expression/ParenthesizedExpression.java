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
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class ParenthesizedExpression extends AbstractFixedExpression implements Expression{
    public static final String EXPRESSION = "expression";
    
    public ParenthesizedExpression(){
        super( "ParenthesizedExpression", EXPRESSION );
    }
    
    public ParenthesizedExpression( ASTNode expr ){
        this();
        setField( EXPRESSION, expr );
    }
    
    public ParenthesizedExpression( Expression expr ){
        this();
        setExpression( expr );
    }

    public Type resolveType() {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        Expression expr = getExpression();
        if( expr == null )
            return resolved( "type", null );
        
        return resolved( "type", expr.resolveType() );
    }
    
    public Value resolveConstantValue() {
        if( isResolved( "value" ))
            return resolved( "value" );
        
        Expression expr = getExpression();
        if( expr == null )
            return resolved( "value", null );
        
        return resolved( "value", expr.resolveConstantValue() );
    }
    
    public Expression getExpression(){
        return (Expression)getNoError( 0 );
    }
    
    public void setExpression( Expression expr ){
        setField( 0, expr );
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
        return false;
    }

    public boolean isConstant() {
        Expression expr = (Expression)getChild( 0 );
        return expr == null ? false : expr.isConstant();
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( node instanceof Expression )
            return;
        
        throw new ASTException( node, "Must be an Expression" );
    }    
}
