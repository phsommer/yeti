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
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;

public class ExpressionStatement extends AbstractFixedASTNode implements Statement {
    public static final String EXPRESSION = "expression";
    
    public ExpressionStatement(){
        super( "ExpressionStatement", EXPRESSION );
    }
    
    public ExpressionStatement( ASTNode expression ){
        this();
        setField( EXPRESSION, expression );
    }
    
    public ExpressionStatement( Expression expression ){
        this();
        setExpression( expression );
    }
    
    public void setExpression( Expression expression ){
        setField( 0, expression );
    }
    public Expression getExpression(){
        return (Expression)getField( 0 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !( node instanceof Expression ) )
            throw new ASTException( node, "Must be an Expression" );
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
        // TODO method not implemented
        super.resolve( stack );
    }
    
    public void flow( ControlFlow flow ){
        flow.follow( this );
    }
    
    public Type resolveExpressionType(){
        if( isResolved( "type" ))
            return resolved( "type" );
        
        Expression expr = getExpression();
        if( expr == null )
            return resolved( "type", null );
        
        return resolved( "type", expr.resolveType() );
    }
    
    public boolean isFunctionEnd(){
        return false;
    }
}
