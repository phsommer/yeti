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
package tinyos.yeti.nesc12.parser.ast.nodes.declaration;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.ArrayType;
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;

public class ArrayAbstractDeclarator extends AbstractFixedASTNode implements AbstractDeclarator {
    public ArrayAbstractDeclarator(){
        super( "ArrayAbstractDeclarator", "declarator", "size" );
    }
    
    public ArrayAbstractDeclarator( AbstractDeclarator declarator, ASTNode size ){
        this();
        setDeclarator( declarator );
        setField( "size", size );
    }
    
    public Type resolveType( Type base, AnalyzeStack stack ) {
        String id = "type";
        if( isResolved( id ))
            return resolved( id );
        
        if( base == null )
            return resolved( id, null );
        
        ASTNode size = getSize();
        if( size == null ){
            base = ArrayType.variable( base, stack, this );
        }
        else if( size instanceof VariableLength ){
            base = ArrayType.incomplete( base, stack, this );
        }
        else{
            Expression expr = (Expression)size;
            Value value = expr.resolveConstantValue();
            if( value == null )
                return resolved( id, null );
            
            if( value instanceof IntegerValue ){
                IntegerValue i = (IntegerValue)value;
                if( i.intValue() < 0 )
                    return resolved( id, null );
                
                base = ArrayType.specified( base, i.intValue(), stack, this );
            }
        }
        
        if( base == null )
            return resolved( id, null );
        
        AbstractDeclarator decl = getDeclarator();
        if( decl == null )
            return base;
        
        return resolved( id, decl.resolveType( base, stack ) );        
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    
    public void setDeclarator( AbstractDeclarator declarator ){
        setField( 0, declarator );
    }
    public AbstractDeclarator getDeclarator(){
        return (AbstractDeclarator)getNoError( 0 );
    }
    
    public void setSize( Expression size ){
        setField( 1, size );
    }
    public void setSize( VariableLength size ){
        setField( 1, size );
    }
    public ASTNode getSize(){
        return getNoError( 1 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof AbstractDeclarator ) )
                throw new ASTException( node, "Must be a AbstractDeclarator" );
        }
        if( index == 1 ) {
            if( !( node instanceof Expression ) && !( node instanceof VariableLength ) )
                throw new ASTException( node, "Must be an Expression or a VariableLength" );
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
}
