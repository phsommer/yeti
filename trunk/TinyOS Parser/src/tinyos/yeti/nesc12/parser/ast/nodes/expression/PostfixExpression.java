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

public class PostfixExpression extends AbstractFixedExpression implements Expression {
    public static final String EXPRESSION = "expression";
    public static final String OPERATOR = "operator";
    
    public PostfixExpression(){
        super( "PostfixExpression", EXPRESSION, OPERATOR );
    }

    public PostfixExpression( ASTNode expression, ASTNode operator ){
        this();
        setField( EXPRESSION, expression );
        setField( OPERATOR, operator );
    }
    
    public PostfixExpression( Expression expression, PostfixOperator operator ){
        this();
        setOperator( operator );
        setExpression( expression );
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
        // increment and decrement prevent constant value
        return null;
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    
    public void setExpression( Expression expression ){
        setField( 0, expression );
    }
    
    public Expression getExpression(){
        return (Expression)getNoError( 0 );
    }
    
    public void setOperator( PostfixOperator operator ){
        setField( 1, operator );
    }
    public PostfixOperator getOperator(){
        return (PostfixOperator)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !(node instanceof Expression ))
                throw new ASTException( node, "Must be an Expression" );
        }
        if( index == 1 ){
            if( !(node instanceof PostfixOperator ))
                throw new ASTException( node, "Must be an PostfixOperator" );
        }
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
        Expression expr = getExpression();
        return expr != null && expr.hasCommas();
    }

    public boolean isConstant() {
        return false;
    }
}
