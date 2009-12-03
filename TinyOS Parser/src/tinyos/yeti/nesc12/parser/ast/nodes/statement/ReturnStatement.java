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
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionMap;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;

public class ReturnStatement extends AbstractFixedASTNode implements Statement {
    public static final String EXPRESSION = "expression";
    
    public ReturnStatement(){
        super( "ReturnStatement", EXPRESSION );
    }
    
    public ReturnStatement( ASTNode expression ){
        this();
        setField( EXPRESSION, expression );
    }
    
    public ReturnStatement( Expression expression ){
        this();
        setExpression( expression );
    }
    
    public void setExpression( Expression expression ){
        setField( 0, expression );
    }
    public Expression getExpression(){
        return (Expression)getNoError( 0 );
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
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            Type required = stack.get( FunctionDefinition.RESULT_TYPE );
            if( required != null ){
                Expression expr = getExpression();
                if( expr == null ){
                    if( required.asBase() != BaseType.VOID ){
                        stack.warning( "return without value in function returning non-void", this );
                    }
                }
                else{
                    if( required.asBase() == BaseType.VOID ){
                        stack.warning( "return with value in function returning void", this );
                    }
                    else{
                        Type type = expr == null ? null : expr.resolveType();
                        if( type != null ){
                            ConversionTable.instance().check(
                                    type,
                                    required, 
                                    ConversionMap.assignment( 
                                            stack, 
                                            expr,
                                            expr == null ? null : expr.resolveConstantValue() ) );
                        }
                    }
                }
            }
        }
    }
    
    public void flow( ControlFlow flow ){
        // nowhere
    }
    
    public Type resolveExpressionType(){
        return BaseType.VOID;
    }
    
    public boolean isFunctionEnd(){
        return true;
    }
}
