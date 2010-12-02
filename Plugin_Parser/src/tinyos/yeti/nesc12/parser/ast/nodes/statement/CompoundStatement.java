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
package tinyos.yeti.nesc12.parser.ast.nodes.statement;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Flag;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusherFactory;

public class CompoundStatement extends AbstractListASTNode<Statement> implements Statement{
    public static final Flag NO_PUSH = new Flag( "top level compound statement" );

    public CompoundStatement(){
        super( "CompoundStatement" );
    }

    public CompoundStatement( ErrorASTNode error ){
        this();
        addError( error );
    }

    public CompoundStatement( Statement child ){
        this();
        add( child );
    }

    @Override
    public CompoundStatement add( Statement node ) {
        super.add( node );
        return this;
    }

    @Override
    protected void checkChild( Statement child ) throws ASTException {

    }

    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }

    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        if( stack.present( NO_PUSH )){
            stack.remove( NO_PUSH );
            super.resolve( stack );
        }
        else{
            stack.push( FieldPusherFactory.STANDARD );
            super.resolve( stack );
            stack.pop( getRight() );
        }
    }

    public void flow( ControlFlow flow ){
        Statement[] children = new Statement[ getChildrenCount() ];
        for( int i = 0; i < children.length; i++ )
            children[i] = getTypedChild( i );
        flow.line( this, children );
    }

    public Type resolveExpressionType(){
        if( isResolved( "type" ))
            return resolved( "type" );
        
        if( getChildrenCount() == 0 )
            return resolved( "type", BaseType.VOID );
        
        Statement statement = getNoError( getChildrenCount()-1 );
        if( statement == null )
            return resolved( "type", null );
        
        return resolved( "type", statement.resolveExpressionType() );
    }
    
    public boolean isFunctionEnd(){
        return false;
    }
}
