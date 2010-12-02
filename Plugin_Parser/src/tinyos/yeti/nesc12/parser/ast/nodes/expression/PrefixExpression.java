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
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.types.PointerType;
import tinyos.yeti.nesc12.parser.ast.elements.values.FloatingValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class PrefixExpression extends AbstractFixedExpression implements Expression {
    public static final String OPERATOR = "operator";
    public static final String EXPRESSION = "expression";
    
    public PrefixExpression(){
        super( "PrefixExpression", OPERATOR, EXPRESSION );
    }
    
    public PrefixExpression( ASTNode operator, ASTNode expression ){
        this();
        setField( OPERATOR, operator );
        setField( EXPRESSION, expression );
    }

    public PrefixExpression( UnaryOperator operator, Expression expression ){
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
        
        Type type = expr.resolveType();
        if( type == null )
            return resolved( "type", null );
        
        UnaryOperator unary = getOperator();
        UnaryOperator.Operator operator = unary == null ? null : unary.getOperator();
        
        if( operator != null ){
            switch( operator ){
                case ADDRESS:
                    type = new PointerType( type );
                    break;
                    
                case POINTER:
                    PointerType pointer = type.asPointerType();
                    type = pointer == null ? null : pointer.getRawType();
                    break;
            }
        }
        
        return resolved( "type", type );
    }
    
    public Value resolveConstantValue() {
        if( isResolved( "value" ))
            return resolved( "value" );
        
        UnaryOperator operator = getOperator();
        if( operator != null && !operator.isConstant() )
            return resolved( "value", null );

        Expression expr = getExpression();
        if( expr == null )
            return resolved( "value", null );
        
        Value value = expr.resolveConstantValue();
        if( value == null )
            return resolved( "value", null );
        
        switch( operator.getOperator() ){
            case INCREMENT:
            case DECREMENT:
                return resolved( "value", null );
            
            case POSITIVE:
                return resolved( "value", value );
            case NEGATIVE:
                if( value instanceof IntegerValue ){
                    IntegerValue i = (IntegerValue)value;
                    return resolved( "value", new IntegerValue( i.getType(), -i.asInteger() ) );
                }
                else if( value instanceof FloatingValue ){
                    FloatingValue f = (FloatingValue)value;
                    return resolved( "value", new FloatingValue( f.getType(), -f.asFloating() ) );
                }
                return null;
            case INVERSE:
                if( value instanceof IntegerValue ){
                    IntegerValue i = (IntegerValue)value;
                    return resolved( "value", new IntegerValue( i.getType(), ~i.asInteger() ) );
                }
                return null;
            case NOT:
                if( value instanceof IntegerValue ){
                    IntegerValue i = (IntegerValue)value;
                    return resolved( "value", new IntegerValue( BaseType.U_INT, i.asInteger() == 0 ? 0 : 1 ) );
                }
                else if( value instanceof FloatingValue ){
                    FloatingValue f = (FloatingValue)value;
                    return resolved( "value", new IntegerValue( BaseType.U_INT, f.asFloating() == 0 ? 0 : 1 ) );
                }
                return resolved( "value", null );
        }
        
        return resolved( "value", null );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            checkType( stack );
        }
    }
    
    private void checkType( AnalyzeStack stack ){
        Expression expr = getExpression();
        UnaryOperator unary = getOperator();
        
        if( expr == null || unary == null )
            return;
        
        UnaryOperator.Operator operator = unary.getOperator();
        if( operator == null )
            return;
        
        if( operator == UnaryOperator.Operator.POINTER ){
            Type type = expr.resolveType();
            if( type == null )
                return;
            
            if( type.asPointerType() == null ){
                stack.warning( "not a pointer", expr );
            }
        }
    }
    
    public void setOperator( UnaryOperator operator ){
        setField( 0, operator );
    }
    
    public UnaryOperator getOperator(){
        return (UnaryOperator)getNoError( 0 );
    }
    
    public Expression getExpression(){
        return (Expression)getNoError( 1 );
    }
    
    public void setExpression( Expression expression ){
        setField( 1, expression );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !(node instanceof UnaryOperator ))
                throw new ASTException( node, "Must be an UnaryOperator" );
        }
        if( index == 1 ){
            if( !(node instanceof Expression ))
                throw new ASTException( node, "Must be an Expression" );
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
        UnaryOperator operator = getOperator();
        if( operator != null && !operator.isConstant() )
            return false;
        
        Expression expr = getExpression();
        return expr != null && expr.isConstant();
    }
}
