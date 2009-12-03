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
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Initializer;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypeName;

public class CompoundLiteral extends AbstractFixedExpression implements Expression {
    public static final String TYPE = "type";
    public static final String INITIALIZER = "initializer";
    
    public CompoundLiteral(){
        super( "CompoundLiteral", TYPE, INITIALIZER );
    }
    
    public CompoundLiteral( ASTNode type, ASTNode initializer ){
        this();
        setField( TYPE, type );
        setField( INITIALIZER, initializer );
    }
    
    public CompoundLiteral( TypeName type, Initializer initializer ){
        this();
        setType( type );
        setInitializer( initializer );
    }
    
    public Type resolveType() {
        if( isResolved( "type" ))
            return resolved( "type" );
        
        TypeName type = getType();
        if( type == null )
            return resolved( "type", null );
            
        return resolved( "type", type.resolveType() );
    }
    
    public Value resolveConstantValue() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            resolveType();
        }
    }
    
    public TypeName getType(){
        return (TypeName)getNoError( 0 );
    }
    
    public void setType( TypeName type ){
        setField( 0, type );
    }
    
    public Initializer getInitializer(){
        return (Initializer)getNoError( 1 );
    }
    
    public void setInitializer( Initializer initializer ){
        setField( 1, initializer );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !(node instanceof TypeName ))
                throw new ASTException( node, "Must be a TypeName" );
        }
        if( index == 1 ){
            if( !(node instanceof Initializer ))
                throw new ASTException( node, "Must be an Initializer" );
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
        return false;
    }

    public boolean isConstant() {
        // TODO not yet implemented
        return false;
    }
}
