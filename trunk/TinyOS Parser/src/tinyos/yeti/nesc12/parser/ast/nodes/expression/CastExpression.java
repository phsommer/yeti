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
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypeName;

public class CastExpression extends AbstractFixedExpression implements Expression{
    public static final String TYPE = "type";
    public static final String EXPRESSION = "expression";
    
    public CastExpression(){
        super( "CastExpression", TYPE, EXPRESSION );
    }
    
    public CastExpression( ASTNode type, ASTNode expression ){
        this();
        setField( TYPE, type );
        setField( EXPRESSION, expression );
    }
    
    public CastExpression( TypeName type, Expression expression ){
        this();
        setType( type );
        setExpression( expression );
    }

    public Type resolveType() {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        TypeName name = getType();
        if( name == null )
            return resolved( "type", null );
            
        return resolved( "type", name.resolveType() );
    }
    
    public Type resolveValueType(){
        if( isResolved( "value type" ))
            return resolved( "value type" );
        
        Expression expr = getExpression();
        if( expr == null )
            return resolved( "value type", null );
        
        return resolved( "value type", expr.resolveType() );
    }
    
    public Value resolveConstantValue() {
        if( isResolved( "value" ))
            return resolved( "value" );
        
        Expression expr = getExpression();
        if( expr == null )
            return resolved( "value", null );
        
        Value value = expr.resolveConstantValue();
        if( value == null )
            return resolved( "value", null );
        
        Type type = resolveType();
        if( type == null ){
        	if( value != null )
        		return resolved( "value", new UnknownValue( null ));
        	
            return resolved( "value", null );
        }
        
        return resolved( "value", type.cast( value ) );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
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
        
        Expression expr = getExpression();
        Value value;
        
        if( expr != null )
            value = expr.resolveConstantValue();
        else
            value = null;
        
        ConversionTable.instance().check( valueType, resultType, ConversionMap.cast( stack, this, value ) );
    }
    
    public TypeName getType(){
        return (TypeName)getNoError( 0 );
    }
    
    public void setType( TypeName type ){
        setField( 0, type );
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
            if( !(node instanceof TypeName ))
                throw new ASTException( node, "Must be a TypeName" );
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
        Expression expr = getExpression();
        return expr != null && expr.isConstant();
    }
}
