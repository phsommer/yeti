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
import tinyos.yeti.nesc12.parser.ast.elements.values.FloatingValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.NumberValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class ArithmeticExpression extends AbstractFixedExpression implements Expression{
    public static final String LEFT = "left";
    public static final String OPERATOR = "operator";
    public static final String RIGHT = "right";
    
    public ArithmeticExpression(){
        super( "ArithmeticExpression", LEFT, OPERATOR, RIGHT );
    }
    
    public ArithmeticExpression( ASTNode left, ASTNode operator, ASTNode right ){
        this();
        setField( LEFT, left );
        setField( OPERATOR, operator );
        setField( RIGHT, right );
    }
    
    public ArithmeticExpression( Expression left, ArithmeticOperator operator, Expression right ){
        this();
        setLeftExpression( left );
        setOperator( operator );
        setRightExpression( right );
    }
    
    public Expression getLeftExpression(){
        return (Expression)getNoError( 0 );
    }
    
    public void setLeftExpression( Expression left ){
        setField( 0, left );
    }
    
    public ArithmeticOperator getOperator(){
        return (ArithmeticOperator)getNoError( 1 );
    }
    
    public void setOperator( ArithmeticOperator operator ){
        setField( 1, operator );
    }
    
    public Expression getRightExpression(){
        return (Expression)getNoError( 2 );
    }
    
    public void setRightExpression( Expression right ){
        setField( 2, right );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 1 ){
            if( !(node instanceof ArithmeticOperator) )
                throw new ASTException( node, "Must be an ArithmeticOperator" );
        }
        if( index == 0 || index == 2 ){
            if( !(node instanceof Expression) )
                throw new ASTException( node, "Must be an Expression" );
        }
    }

    public Type resolveType() {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        ArithmeticOperator operator = getOperator();
        if( operator == null )
            return resolved( "type", null );
        
        ArithmeticOperator.Operator op = operator.getOperator();
        if( op == null )
            return resolved( "type", null );
        
        Expression left = getLeftExpression();
        if( left == null )
            return resolved( "type", null );
        
        Expression right = getRightExpression();
        if( right == null )
            return resolved( "type", null );
        
        Type leftType = left.resolveType();
        if( leftType == null )
            return resolved( "type", null );
        
        Type rightType = right.resolveType();
        if( rightType == null )
            return resolved( "type", null );
        
        return resolved( "type", resolveType( leftType, op, rightType ));
    }
    
    public static Type resolveType( Type leftType, ArithmeticOperator.Operator op, Type rightType ){
        BaseType right = rightType.asBase();
        BaseType left = leftType.asBase();
        
        if( right == null || left == null )
            return null;
        
        if( !right.isArithmeticType() )
            return null;
        if( !left.isArithmeticType() )
            return null;
        
        switch( op ){
            case MOD:
                return BaseType.S_INT;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                return left;
            case BIT_AND:
            case BIT_OR:
            case BIT_XOR:
                if( !right.isIntegerType() )
                    return null;
                if( !left.isIntegerType() )
                    return null;
                break;
        }
        
        return BaseType.arithmetic( left, right );
    }
    
    public Value resolveConstantValue() {
        if( isResolved( "value" ))
            return resolved( "value" );
        
        ArithmeticOperator operator = getOperator();
        if( operator == null )
            return resolved( "value", null );
        
        ArithmeticOperator.Operator op = operator.getOperator();
        if( op == null )
            return resolved( "value", null );
        
        Expression left = getLeftExpression();
        if( left == null )
            return resolved( "value", null );
        
        Expression right = getRightExpression();
        if( right == null )
            return resolved( "value", null );
        
        Object leftConst = left.resolveConstantValue();
        Object rightConst = right.resolveConstantValue();
        

        Type type = resolveType();
        if( type == null ){
        	if( leftConst == null || rightConst == null ){
        		return resolved( "value", null );
        	}
        	else{
        		return resolved( "value", new UnknownValue( null ));
        	}
        }
        
        boolean leftNumber = leftConst instanceof NumberValue;
        boolean leftUnknown = leftConst instanceof UnknownValue;
        boolean rightNumber = rightConst instanceof NumberValue;
        boolean rightUnknown = rightConst instanceof UnknownValue;
        
        if( leftNumber && rightNumber ){
        	NumberValue a = (NumberValue)leftConst;
        	NumberValue b = (NumberValue)rightConst;
        
        	return resolved( "value", resolveValue( type, a, op, b ));
        }
        if( (leftNumber || leftUnknown) && (rightNumber || rightUnknown ) ){
        	return resolved( "value", new UnknownValue( type ));
        }
        return resolved( "value", null );
    }
    
    
    public static Value resolveValue( Type type, NumberValue left, ArithmeticOperator.Operator op, NumberValue right ){    
        boolean floatingA = left.isFloating();
        boolean floatingB = right.isFloating();
        
        boolean floating = floatingA || floatingB;
        
        NumberValue result = null;
        
        switch( op ){
            case ADD: 
                if( floating )
                    result = number( type, left.asFloating() + right.asFloating() );
                else
                    result = number( type, left.asInteger() + right.asInteger() );
                break;
                
            case SUB:
                if( floating )
                    result = number( type, left.asFloating() - right.asFloating() );
                else
                    result = number( type, left.asInteger() - right.asInteger() );
                break;
                
            case MUL:
                if( floating )
                    result = number( type, left.asFloating() * right.asFloating() );
                else
                    result = number( type,  left.asInteger() * right.asInteger() );
                break;
                
            case DIV:
                if( floating )
                    result = number( type, left.asFloating() / right.asFloating() );
                else{
                    if( right.asInteger() == 0 )
                        result = null;
                    else
                        result = number( type, left.asInteger() / right.asInteger() );
                }
                break;
                
            case MOD:
                if( floating )
                    result = number( type, left.asFloating() % right.asFloating() );
                else{
                    if( right.asInteger() == 0 )
                        result = null;
                    else
                        result = number( type, left.asInteger() % right.asInteger() );
                }
                break;
                
            case AND:
            case OR:
                boolean boolA = floatingA ? left.asFloating() != 0 : left.asInteger() != 0;
                boolean boolB = floatingB ? right.asFloating() != 0 : right.asInteger() != 0;
                
                switch( op ){
                    case AND:
                        result = number( type, boolA && boolB ? 1 : 0 );
                        break;
                    case OR:
                        result = number( type, boolA || boolB ? 1 : 0 );
                        break;
                }
                break;
            
            case BIT_AND:
                if( floating )
                    result = null;
                else
                    result = number( type, left.asInteger() & right.asInteger() );
                break;
                
            case BIT_OR:
                if( floating )
                    result = null;
                else
                    result = number( type, left.asInteger() | right.asInteger() );
                break;
                
            case BIT_XOR:
                if( floating )
                    result = null;
                else
                    result = number( type, left.asInteger() ^ right.asInteger() );
                break;
                
            case SHIFT_LEFT:
                if( floating )
                    result = null;
                else
                    result = number( type, left.asInteger() << right.asInteger() );
                break;
                
            case SHIFT_RIGHT:
                if( floating )
                    result = null;
                else
                    result = number( type, left.asInteger() >>> right.asInteger() );
                break;
                
            case EQ:
                if( floating )
                    result = number( type, left.asFloating() == right.asFloating() ? 1 : 0 );
                else
                    result = number( type, left.asInteger() == right.asInteger() ? 1 : 0 );
                break;
                
            case GREATER:
                if( floating )
                    result = number( type, left.asFloating() > right.asFloating() ? 1 : 0 );
                else
                    result = number( type, left.asInteger() > right.asInteger() ? 1 : 0 );
                break;
                
            case GREATER_EQ:
                if( floating )
                    result = number( type, left.asFloating() >= right.asFloating() ? 1 : 0 );
                else
                    result = number( type, left.asInteger() >= right.asInteger() ? 1 : 0 );
                break;
                
            case NOT_EQ:
                if( floating )
                    result = number( type, left.asFloating() != right.asFloating() ? 1 : 0 );
                else
                    result = number( type, left.asInteger() != right.asInteger() ? 1 : 0 );
                break;
                
            case SMALLER:
                if( floating )
                    result = number( type, left.asFloating() < right.asFloating() ? 1 : 0 );
                else
                    result = number( type, left.asInteger() < right.asInteger() ? 1 : 0 );
                break;
                
            case SMALLER_EQ:
                if( floating )
                    result = number( type, left.asFloating() <= right.asFloating() ? 1 : 0 );
                else
                    result = number( type, left.asInteger() <= right.asInteger() ? 1 : 0 );
                break;
        }
        
        return result;
    }

    private static NumberValue number( Type type, long value ){
        BaseType base = type.asBase();
        
        if( base != null ){
            switch( base ){
                case DOUBLE:
                case LONG_DOUBLE:
                case FLOAT:
                    return new FloatingValue( base, value );
                case S_CHAR:
                case U_CHAR:
                case S_SHORT:
                case U_SHORT:
                case S_INT:
                case U_INT:
                case S_LONG:
                case U_LONG:
                case S_LONG_LONG:
                case U_LONG_LONG:
                    return new IntegerValue( base, value );
            }
        }
        return null;
    }
    
    private static NumberValue number( Type type, double value ){
        BaseType base = type.asBase();
        
        if( base != null ){
            switch( base ){
                case DOUBLE:
                case LONG_DOUBLE:
                case FLOAT:
                    return new FloatingValue( base, value );
                case S_CHAR:
                case U_CHAR:
                case S_SHORT:
                case U_SHORT:
                case S_INT:
                case U_INT:
                case S_LONG:
                case U_LONG:
                case S_LONG_LONG:
                case U_LONG_LONG:
                    return new IntegerValue( base, (long)value );
            }
        }
        return null;
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
        Expression left = getLeftExpression();
        if( left != null && left.hasCommas() )
            return true;
        
        Expression right = getRightExpression();
        if( right != null && right.hasCommas() )
            return true;
        
        return false;
    }

    public boolean isConstant() {
        Expression left = getLeftExpression();
        if( left == null || !left.isConstant() )
            return false;
        
        Expression right = getRightExpression();
        if( right == null || !right.isConstant() )
            return false;
        
        return true;
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
}
