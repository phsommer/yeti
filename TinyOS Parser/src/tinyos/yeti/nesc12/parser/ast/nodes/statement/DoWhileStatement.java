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
package tinyos.yeti.nesc12.parser.ast.nodes.statement;

import java.math.BigInteger;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusherFactory;

public class DoWhileStatement extends AbstractFixedASTNode implements Statement {
    public static final String BODY = "body";
    public static final String EXPRESSION = "expression";
    
    public DoWhileStatement(){
        super( "DoWhileStatement", BODY, EXPRESSION );
    }
    
    public DoWhileStatement( ASTNode body, ASTNode expression ){
        this();
        setField( BODY, body );
        setField( EXPRESSION, expression );
    }
    
    public DoWhileStatement( Statement body, Expression expression ){
        this();
        setExpression( expression );
        setBody( body );
    }

    public void setBody( Statement body ){
        setField( 0, body );
    }
    public Statement getBody(){
        return (Statement)getNoError( 0 );
    }
    
    public void setExpression( Expression expression ){
        setField( 1, expression );
    }
    public Expression getExpression(){
        return (Expression)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 1 ) {
            if( !( node instanceof Expression ) )
                throw new ASTException( node, "Must be an Expression" );
        }
        if( index == 0 ) {
            if( !( node instanceof Statement ) )
                throw new ASTException( node, "Must be a Statement" );
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

    @Override
    public void resolve( AnalyzeStack stack ) {
        stack.push( FieldPusherFactory.STANDARD );
        if( stack.isReportErrors() ){
            // no check necessary for condition expression
            stack.put( BreakStatement.BREAKABLE );
            stack.put( ContinueStatement.CONTINUABLE );

            super.resolve( stack );
            stack.checkCancellation();
            
            stack.remove( BreakStatement.BREAKABLE );
            stack.remove( ContinueStatement.CONTINUABLE );
        }
        else{
            super.resolve( stack );
            stack.checkCancellation();
        }
        stack.pop( getRight() );
    }
    

    public Value resolveConstantCondition(){
        if( isResolved( "condition" ))
            return resolved( "condition" );
        
        Expression expr = getExpression();
        if( expr == null )
            return resolved( "condition", null );
        
        return resolved( "condition", expr.resolveConstantValue() );
    }
    
    public boolean resolveConditionAlwaysTrue(){
        if( isResolved( "true" ))
            return resolved( "true" );
        
        Value value = resolveConstantCondition();
        if( value instanceof IntegerValue ){
            boolean condition = !BigInteger.ZERO.equals( ((IntegerValue)value).getValue() );
            return resolved( "true", condition );
        }
        
        return resolved( "true", false );
    }
    
    public boolean resolveConditionAlwaysFalse(){
        if( isResolved( "false" ))
            return resolved( "false" );
        
        Value value = resolveConstantCondition();
        if( value instanceof IntegerValue ){
            boolean condition = !BigInteger.ZERO.equals( ((IntegerValue)value).getValue() );
            return resolved( "false", !condition );
        }
        
        return resolved( "false", false );        
    }
    
    
    public void flow( ControlFlow flow ){
        flow.choice( this, getBody() );  
        flow.follow( this );
    }
    
    public Type resolveExpressionType(){
        return BaseType.VOID;
    }
    
    public boolean isFunctionEnd(){
        return false;
    }
}
