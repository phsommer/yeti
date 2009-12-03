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
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypeName;

public class SizeofExpression extends AbstractFixedExpression implements Expression{
    public static final String ARGUMENT = "argument";
    
    public SizeofExpression(){
        super( "SizeofExpression", ARGUMENT );
    }

    public SizeofExpression( ASTNode argument ){
        this();
        setArgument( argument );
    }
    

    public Type resolveType() {
        Value value = resolveConstantValue();
        if( value == null || value.getType() == null )
            return BaseType.U_INT;
        
        return value.getType();
    }
    
    public Value resolveConstantValue() {
        if( isResolved( "value" ))
            return resolved( "value" );
        
        ASTNode argument = getArgument();
        if( argument == null )
            return resolved( "value", null );
        
        int check = -1;
        
        if( argument instanceof Expression ){
            Expression expr = (Expression)argument;
            Type type = expr.resolveType();
            if( type == null )
                return resolved( "value", null );
            
            check = type.sizeOf();
            if( check == -1 ){
                Value value = expr.resolveConstantValue();
                if( value != null ){
                    check = value.sizeOf();
                }
                else if( type.asGenericType() != null ){
                	return resolved( "value", new UnknownValue( BaseType.U_INT ));
                }
            }
        }
        if( argument instanceof TypeName ){
            TypeName name = (TypeName)argument;
            Type type = name.resolveType();
            if( type == null )
                return resolved( "value", null );
            
            if( type.asGenericType() != null )
            	return resolved( "value", new UnknownValue( BaseType.U_INT ));
            
            check = type.sizeOf();
        }
        
        if( check == -1 )
            return resolved( "value", null );
        
        return resolved( "value", new IntegerValue( BaseType.U_INT, check ) );
    }

    public boolean hasCommas() {
        return false;
    }

    public boolean isConstant() {
        return resolveConstantValue() != null;
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    
    
    public void setArgument( ASTNode argument ){
        setField( 0, argument );
    }
    
    public ASTNode getArgument(){
        return getNoError( 0 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( node instanceof Expression )
            return;
        
        if( node instanceof TypeName )
            return;
        
        throw new ASTException( node, "Must be either Expression or TypeName" );
    }

    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }

    
    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }
}
