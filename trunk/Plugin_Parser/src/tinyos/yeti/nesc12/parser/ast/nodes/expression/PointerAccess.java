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
import tinyos.yeti.nesc12.parser.ast.elements.types.PointerType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class PointerAccess extends AbstractFixedExpression implements Expression{
    public static final String EXPRESSION = "expression";
    public static final String NAME = "name";
    
    public PointerAccess(){
        super( "PointerAccess", EXPRESSION, NAME );
    }
    
    public PointerAccess( ASTNode expression, ASTNode name ){
        this();
        setField( EXPRESSION, expression );
        setField( NAME, name );
    }
    
    public PointerAccess( Expression expression, Identifier name ){
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
        if( type == null || type.asPointerType() == null )
            return resolved( "type", null );
        
        type = type.asPointerType().getRawType();
        
        if( type == null || type.asDataObjectType() == null )
            return resolved( "type", null );
        
        DataObjectType data = type.asDataObjectType();
        Field field = data.getField( new SimpleName( null, name.getName() ));
        if( field == null )
            return resolved( "type", null );
        
        return resolved( "type", field.getType() );
    }
    
    public Value resolveConstantValue() {
        // TODO missing, can this be a constant value?
        return null;
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            checkValidName( stack );
        }
    }
    
    private void checkValidName( AnalyzeStack stack ){
        Expression expr = getExpression();
        Identifier name = getName();
        
        if( expr == null || name == null )
            return;
        
        Type type = expr.resolveType();
        if( type == null )
            return;
        
        PointerType pointer = type.asPointerType();
        if( pointer == null ){
            stack.error( "expression does not resolve to a pointer", expr );
            return;
        }
        
        DataObjectType data = pointer.getRawType().asDataObjectType();
        if( data == null ){
            stack.error( "can't access field of non data-object type '" + type.toLabel( null, Type.Label.EXTENDED ) + "'", name );
            return;
        }
        
        Field field = data.getField( new SimpleName( null, name.getName() ) );
        if( field == null ){
            stack.error( "no field '" + name.getName() + "' in data-object", name );
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
