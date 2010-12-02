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
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.elements.values.FloatingValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class ConditionalExpression extends AbstractFixedExpression implements Expression{
    public static final String CONDITION = "condition";
    public static final String IF_VALUE = "ifvalue";
    public static final String ELSE_VALUE = "elsevalue";
    
    public ConditionalExpression(){
        super( "ConditionalExpression", CONDITION, IF_VALUE, ELSE_VALUE );
    }
    
    public ConditionalExpression( ASTNode condition, ASTNode ifValue, ASTNode elseValue ){
        this();
        setField( CONDITION, condition );
        setField( IF_VALUE, ifValue );
        setField( ELSE_VALUE, elseValue );
    }
    
    public ConditionalExpression( Expression condition, Expression ifValue, Expression elseValue ){
        this();
        setCondition( condition );
        setIfValue( ifValue );
        setElseValue( elseValue );
    }
    
    public Type resolveType() {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        Expression left = getIfValue();
        Expression right = getElseValue();
        if( left == null || right == null )
            return resolved( "type", null );
        
        Type leftType = left.resolveType();
        if( leftType == null )
            return resolved( "type", null );
        
        Type rightType = right.resolveType();
        if( rightType == null )
            return resolved( "type", null );
        
        /*
         * 3. One of the following shall hold for the second and third operands:
         *  both operands have arithmetic type;
         *  both operands have the same structure or union type;
         *  both operands have void type;
         *  both operands are pointers to qualified or unqualified versions of compatible types;
         *  one operand is a pointer and the other is a null pointer constant; or
         *  one operand is a pointer to an object or incomplete type and the other is a pointer to a
         * qualified or unqualified version of void.
         */
        
        if( ConversionTable.instance().equals( leftType, rightType ) )
            return resolved( "type", leftType );
        
        //  both operands have arithmetic type;
        if( leftType.asBase() != null && rightType.asBase() != null ){
            BaseType leftBase = leftType.asBase();
            BaseType rightBase = rightType.asBase();
            
            if( leftBase.isArithmeticType() && rightBase.isArithmeticType() ){
                return resolved( "type", BaseType.arithmetic( leftBase, rightBase ) );
            }
        }
        
        //  both operands have the same structure or union type;
            // already done
        
        //  both operands have void type;
            // already done
        
        //  both operands are pointers to qualified or unqualified versions of compatible types;
        	// implicitly done trough equality
        
        //  one operand is a pointer and the other is a null pointer constant; or
        	// null pointer constant is of type void
        
        //  one operand is a pointer to an object or incomplete type and the other is a pointer to a
        if( leftType.asPointerType() != null && rightType.asBase() == BaseType.VOID )
        	return leftType;
        if( rightType.asPointerType() != null && leftType.asBase() == BaseType.VOID )
        	return rightType;
        
        return resolved( "type", null );
    }
    
    public Value resolveConstantValue() {
        if( isResolved( "value" ))
            return resolved( "value" );
        
        Expression condition = getCondition();
        if( condition == null )
            return resolved( "value", null );
        
        Value value = condition.resolveConstantValue();
        if( value == null )
            return resolved( "value", null );
        
        boolean evaluated = true;
        
        if( value instanceof IntegerValue ){
            evaluated = ((IntegerValue)value).asInteger() != 0;
        }
        else if( value instanceof FloatingValue ){
            evaluated = ((FloatingValue)value).asFloating() != 0;
        }
        else if( value instanceof UnknownValue ){
        	Type type = resolveType();
        	
        	Expression exprTrue = getIfValue();
        	Expression exprFalse = getElseValue();
        	
        	if( exprTrue == null || exprFalse == null )
        		return resolved( "value", null );
        	
        	if( exprTrue.resolveConstantValue() != null && exprFalse.resolveConstantValue() != null )
        		return resolved( "value", new UnknownValue( type ));
        	
        	return resolved( "value", null );
        }
        else
            return resolved( "value", null );
        
        Expression expr;
        
        if( evaluated ){
            expr = getIfValue();
        }
        else{
            expr = getElseValue();
        }
        
        if( expr == null )
            return resolved( "value", null );
        
        return resolved( "value", expr.resolveConstantValue() );
    }
    
    public Expression getCondition(){
        return (Expression)getNoError( 0 );
    }
    
    public void setCondition( Expression condition ){
        setField( 0, condition );
    }
    
    public Expression getIfValue(){
        return (Expression)getNoError( 1 );
    }
    
    public void setIfValue( Expression ifValue ){
        setField( 1, ifValue );
    }
    
    public Expression getElseValue(){
        return (Expression)getNoError( 2 );
    }
    
    public void setElseValue( Expression elseValue ){
        setField( 2, elseValue );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !(node instanceof Expression ))
            throw new ASTException( node, "Must be an Expression" );
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
        Expression condition = getCondition();
        if( condition == null )
            return false;
        
        Value value = condition.resolveConstantValue();
        if( value == null )
            return false;
        
        boolean evaluated = true;
        
        if( value instanceof IntegerValue ){
            evaluated = ((IntegerValue)value).asInteger() != 0;
        }
        else if( value instanceof FloatingValue ){
            evaluated = ((FloatingValue)value).asFloating() != 0;
        }
        else
            return false;
        
        Expression expr;
        
        if( evaluated ){
            expr = getIfValue();
        }
        else{
            expr = getElseValue();
        }
        
        return expr != null && expr.isConstant();
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    
}
