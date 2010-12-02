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
import tinyos.yeti.nesc12.parser.ast.elements.values.ArrayValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class ArraySubscripting extends AbstractFixedExpression implements Expression{
    public static final String EXPRESSION = "expression";
    public static final String INDEX = "index";
    
    public ArraySubscripting(){
        super( "ArraySubscripting", EXPRESSION, INDEX );
    }
    
    public ArraySubscripting( ASTNode expression, ASTNode index ){
        this();
        setField( EXPRESSION, expression );
        setField( INDEX, index );
    }
    
    public ArraySubscripting( Expression expression, Expression index ){
        this();
        setExpression( expression );
        setIndex( index );
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
        
        if( type.asArrayType() == null )
            return resolved( "type", null );
        
        return resolved( "type", type.asArrayType().getRawType() );
    }
    
    public Value resolveConstantValue() {
        if( isResolved( "value" ))
            return resolved( "value" );
        
        Expression expr = getExpression();
        Expression index = getIndex();
        
        if( expr == null || index == null )
            return resolved( "value", null );
        
        Value exprValue = expr.resolveConstantValue();
        if( !(exprValue instanceof ArrayValue) )
            return resolved( "value", null );
        
        Value indexValue = index.resolveConstantValue();
        if( !(indexValue instanceof IntegerValue) )
            return resolved( "value", null );
        
        ArrayValue array = (ArrayValue)exprValue;
        long indexInt = ((IntegerValue)indexValue).asInteger();
        
        if( indexInt >= 0 && indexInt < array.getLength() )
            return resolved( "value", array.getValue( (int)indexInt ) );
        else
            return resolved( "value", null );
    }
    
    public Expression getExpression(){
        return (Expression)getNoError( 0 );
    }
    
    public void setExpression( Expression expression ){
        setField( 0, expression );
    }
    
    public Expression getIndex(){
        return (Expression)getNoError( 1 );
    }
    
    public void setIndex( Expression index ){
        setField( 1, index );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( node instanceof Expression )
            return;
        
        throw new ASTException( node, "Must be an expression" );
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
        Expression expr = getExpression();
        Expression index = getIndex();
        
        return expr != null && index != null && expr.isConstant() && index.isConstant();
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
}
