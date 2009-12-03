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
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.values.DataObject;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class FieldAccess extends AbstractFixedExpression implements Expression{
    public static final String EXPRESSION = "expression";
    public static final String NAME = "name";
    
    public FieldAccess(){
        super( "FieldAccess", EXPRESSION, NAME );
    }
    
    public FieldAccess( ASTNode expression, ASTNode name ){
        this();
        setField( EXPRESSION, expression );
        setField( NAME, name );
    }
    
    public FieldAccess( Expression expression, Identifier name ){
        this();
        setExpression( expression );
        setName( name );
    }
    
    public Type resolveType() {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        Expression expr = getExpression();
        Identifier name = getName();
        
        if( expr == null || name == null )
            return resolved( "type", null );
        
        Type type = expr.resolveType();
        if( type == null || type.asDataObjectType() == null )
            return resolved( "type", null );
        
        DataObjectType data = type.asDataObjectType();
        Field field = data.getField( new SimpleName( null, name.getName() ));
        if( field == null )
            return resolved( "type", null );
        
        return resolved( "type", field.getType() );
    }
    
    public Value resolveConstantValue() {
        if( isResolved( "value" ))
            return resolved( "value" );
        
        Expression expr = getExpression();
        Identifier name = getName();
        
        if( expr == null || name == null )
            return resolved( "value", null );
        
        Value value = expr.resolveConstantValue();
        if( value == null || !(value instanceof DataObject) )
            return resolved( "value", null );
        
        DataObject data = (DataObject)value;
        return resolved( "value", data.getValue( name ) );        
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        checkValidName( stack );
    }
    
    private void checkValidName( AnalyzeStack stack ){
        Expression expr = getExpression();
        Identifier name = getName();
        
        if( expr == null || name == null )
            return;
        
        Type type = expr.resolveType();
        if( type == null )
            return;
        
        DataObjectType data = type.asDataObjectType();
        if( data == null ){
        	if( stack.isReportErrors() ){
        		stack.error( "can't access field of non data-object type '" + type.toLabel( null, Type.Label.EXTENDED ) + "'", name );
        	}
            return;
        }
        
        Field field = data.getField( new SimpleName( null, name.getName() ) );
        if( field == null ){
        	if( stack.isReportErrors() ){
        		stack.error( "no field '" + name.getName() + "' in data-object", name );
        	}
        	return;
        }
        
        if( stack.isCreateReferences() ){
        	stack.reference( this, field.getPath() );
        }
    }
    
    public Expression getExpression(){
        return (Expression)getNoError( 0 );
    }
    
    public void setExpression( Expression expression ){
        setField( 0, expression );
    }
    
    public Identifier getName(){
        return (Identifier)getNoError( 1 );
    }
    
    public void setName( Identifier name ){
        setField( 1, name );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !(node instanceof Expression ))
                throw new ASTException( node, "Must be an Expression" );
        }
        
        if( index == 1 ){
            if( !(node instanceof Identifier ))
                throw new ASTException( node, "Must be an Identifier" );
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
