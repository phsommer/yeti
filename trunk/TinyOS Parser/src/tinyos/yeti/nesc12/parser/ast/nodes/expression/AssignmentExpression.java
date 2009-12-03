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
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionMap;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class AssignmentExpression extends AbstractFixedExpression implements Expression{
    public static final String VARIABLE = "variable";
    public static final String OPERATOR = "operator";
    public static final String VALUE = "value";
    
    public AssignmentExpression(){
        super( "AssignmentExpression", VARIABLE, OPERATOR, VALUE );
    }
    
    public AssignmentExpression( ASTNode variable, ASTNode operator, ASTNode value ){
        this();
        setField( VARIABLE, variable );
        setField( OPERATOR, operator );
        setField( VALUE, value );
    }
    
    public AssignmentExpression( Expression variable, AssignmentOperator operator, Expression value ){
        this();
        setVariable( variable );
        setOperator( operator );
        setValue( value );
    }
    
    public Type resolveType() {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        Expression variable = getVariable();
        if( variable == null )
            return resolved( "type", null );
        
        return resolved( "type", variable.resolveType() );
    }
    
    public Type resolveValueType(){
        if( isResolved( "value type" ))
            return resolved( "value type" );
        
        Expression value = getValue();
        if( value == null )
            return resolved( "value type" );
        
        return resolved( "value type", value.resolveType() );
    }
    
    public Value resolveConstantValue() {
        // assignments are never constant
        return null;
        
        /*if( isResolved( "value" ))
            return resolved( "value" );
        
        Type type = resolveType();
        if( type == null )
            return resolved( "value", null );
        
        AssignmentOperator op = getOperator();
        if( op == null )
            return resolved( "value", null );
        
        Expression value = getValue();
        if( value == null )
            return resolved( "value", null );
        
        Value result = null;
        
        if( op.getOperator() == AssignmentOperator.Operator.ASSIGN ){
            result = value.resolveConstantValue();
        }
        else{
            ArithmeticOperator.Operator aop = null;
            switch( op.getOperator() ){
                case ADD:
                    aop = ArithmeticOperator.Operator.ADD;
                    break;
                case AND:
                    aop = ArithmeticOperator.Operator.BIT_AND;
                    break;
                case DIV:
                    aop = ArithmeticOperator.Operator.DIV;
                    break;
                case MOD:
                    aop = ArithmeticOperator.Operator.MOD;
                    break;
                case MUL:
                    aop = ArithmeticOperator.Operator.MUL;
                    break;
                case OR:
                    aop = ArithmeticOperator.Operator.BIT_OR;
                    break;
                case SHIFT_LEFT:
                    aop = ArithmeticOperator.Operator.SHIFT_LEFT;
                    break;
                case SHIFT_RIGHT:
                    aop = ArithmeticOperator.Operator.SHIFT_RIGHT;
                    break;
                case SUB:
                    aop = ArithmeticOperator.Operator.SUB;
                    break;
                case XOR:
                    aop = ArithmeticOperator.Operator.BIT_XOR;
                    break;
            }
            
            if( aop == null )
                return resolved( "value", null );
            
            Expression variable = getVariable();
            if( variable == null )
                return resolved( "value", null );
            
            Value left = variable.resolveConstantValue();
            if( left == null )
                return resolved( "value", null );
            
            if( !(left instanceof NumberValue))
                return resolved( "value", null );
            
            Value right = value.resolveConstantValue();
            if( right == null )
                return resolved( "value", null );
            
            if( !(right instanceof NumberValue))
                return resolved( "value", null );
            
            result = ArithmeticExpression.resolveValue( type, (NumberValue)left, aop, (NumberValue)right );
        }
        
        if( result == null )
            return resolved( "value", null );
        
        return resolved( "value", type.cast( result ) );*/
    }
    
    @Override
    public void resolve( AnalyzeStack stack ){
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            checkTypes( stack );
        }
    }
    
    private void checkTypes( AnalyzeStack stack ){
        Type resultType = resolveType();
        if( resultType == null )
            return;
        
        Type valueType = resolveValueType();
        if( valueType == null )
            return;
        
        Expression expr = getValue();
        Value value;
        if( expr != null )
            value = expr.resolveConstantValue();
        else
            value = null;
        
        ConversionTable.instance().check( valueType, resultType, ConversionMap.assignment( stack, this, value ) );
    }
    
    public void setVariable( Expression variable ){
        setField( 0, variable );
    }
    
    public Expression getVariable(){
        return (Expression)getNoError( 0 );
    }
    
    public void setOperator( AssignmentOperator operator ){
        setField( 1, operator );
    }
    
    public AssignmentOperator getOperator(){
        return (AssignmentOperator)getNoError( 1 );
    }
    
    public void setValue( Expression value ){
        setField( 2, value );
    }
    
    public Expression getValue(){
        return (Expression)getNoError( 2 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 1 ){
            if( !( node instanceof AssignmentOperator ) )
                throw new ASTException( node, "Must be an AssignmentOperator" );
        }
        if( index == 0 || index == 2 ){
            if( !( node instanceof Expression) )
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
        Expression e = getValue();
        if( e != null && e.hasCommas() )
            return true;
        
        e = getVariable();
        if( e != null && e.hasCommas() )
            return true;
        
        return false;
    }

    public boolean isConstant() {
        return false;
    }
}
