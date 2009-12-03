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
import java.util.List;

import java_cup.runtime.Symbol;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.lexer.NesCLexer;
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

public class IfStatement extends AbstractFixedASTNode implements Statement {
    public static void main( String[] args ) {
        String code =
            "int main(){" +
                "if( i == 1 )" +
                    "if( i == 2 )" +
                        "i = 5;" +
                    "else " +
                        "i = 6;" +
                 "else " +
                     "i = 8;" +
            "}";
        
        List<Symbol> tokens = NesCLexer.quickLexer( code );
        for( Symbol t : tokens ){
            System.out.println( NesCLexer.toString( t ) );
        }
        
        IfStatement s = Parser.quickParser( code, IfStatement.class );
        System.out.println( s );
    }
    
    public static final String CONDITION = "condition";
    public static final String IF_BRANCH = "ifbranch";
    public static final String ELSE_BRANCH = "elsebranch";
    
    public IfStatement(){
        super( "IfStatement", CONDITION, IF_BRANCH, ELSE_BRANCH );
    }
    
    public IfStatement( ASTNode condition, ASTNode ifBranch, ASTNode elseBranch ){
        this();
        setField( CONDITION, condition );
        setField( IF_BRANCH, ifBranch );
        setField( ELSE_BRANCH, elseBranch );
    }
    
    public IfStatement( Expression condition, Statement ifBranch, Statement elseBranch ){
        this();
        setCondition( condition );
        setIfBranch( ifBranch );
        setElseBranch( elseBranch );
    }
    
    public void setCondition( Expression condition ){
        setField( 0, condition );
    }
    public Expression getCondition(){
        return (Expression)getNoError( 0 );
    }
    
    public void setIfBranch( Statement ifBranch ){
        setField( 1, ifBranch );
    }
    public Statement getIfBranch(){
        return (Statement)getNoError( 1 );
    }
    
    public void setElseBranch( Statement elseBranch ){
        setField( 2, elseBranch );
    }
    public Statement getElseBranch(){
        return (Statement)getNoError( 2 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Expression ) )
                throw new ASTException( node, "Must be an Expression" );
        }
        if( index == 1 || index == 2 ) {
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
        super.resolve( stack );
        stack.checkCancellation();
        stack.pop( getRight() );
    }
    
    
    public Value resolveConstantCondition(){
        if( isResolved( "condition" ))
            return resolved( "condition" );
        
        Expression expr = getCondition();
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
        if( getElseBranch() == null ){
            flow.choice( this, getIfBranch() );
            flow.follow( this );
        }
        else{
            flow.choice( this, getIfBranch(), getElseBranch() );
        }
    }
    
    public Type resolveExpressionType(){
        return BaseType.VOID;
    }
    
    public boolean isFunctionEnd(){
        return false;
    }
}
