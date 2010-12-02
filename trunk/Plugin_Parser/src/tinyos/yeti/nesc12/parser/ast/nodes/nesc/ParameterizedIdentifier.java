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
package tinyos.yeti.nesc12.parser.ast.nodes.nesc;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.LazyRangeDescription;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ExpressionList;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class ParameterizedIdentifier extends AbstractFixedASTNode{
    public static final String IDENTIFIER = "identifier";
    public static final String EXPRESSION = "expression";
    
    public ParameterizedIdentifier(){
        super( "ParameterizedIdentifier", IDENTIFIER, EXPRESSION );
    }
    
    public ParameterizedIdentifier( ASTNode identifier, ASTNode expression ){
        this();
        setField( IDENTIFIER, identifier );
        setField( EXPRESSION, expression );
    }
    
    public ParameterizedIdentifier( Identifier identifier, Expression expression ){
        this();
        setIdentifier( identifier );
        setExpression( expression );
    }
    
    public LazyRangeDescription[] resolveParameterRanges( AnalyzeStack stack ){
        Expression expr = getExpression();
        if( expr == null )
            return null;
        
        if( expr instanceof ExpressionList ){
            ExpressionList list = (ExpressionList)expr;
            LazyRangeDescription[] ranges = new LazyRangeDescription[ list.getChildrenCount() ];
            for( int i = 0, n = ranges.length; i<n; i++ ){
                ASTNode node = list.getChild( i );
                if( node != null ){
                    ranges[i] = stack.range( node );
                }
            }
            return ranges;
        }
        
        return new LazyRangeDescription[]{ stack.range( expr ) };
    }
    
    /**
     * Resolves the constant values of the parameters.
     * @return the constant values which represent the parameters or <code>null</code>
     */
    public Value[] resolveParameters(){
        if( isResolved( "parameters" ))
            return resolved( "parameters" );
        
        Expression expr = getExpression();
        if( expr == null )
            return resolved( "parameters", null );
            
        return resolved( "parameters", expr.resolveConstantValues() );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    
    public void setIdentifier( Identifier identifier ){
        setField( 0, identifier );
    }
    public Identifier getIdentifier(){
        return (Identifier)getNoError( 0 );
    }
    
    public void setExpression( Expression expression ){
        setField( 1, expression );
    }
    public Expression getExpression(){
        return (Expression)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Identifier ) )
                throw new ASTException( node, "Must be an Identifier" );
        }
        if( index == 1 ) {
            if( !( node instanceof Expression ) )
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
}
