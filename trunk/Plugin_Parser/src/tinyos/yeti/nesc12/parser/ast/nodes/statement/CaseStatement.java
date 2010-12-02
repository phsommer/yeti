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

import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;

public class CaseStatement extends AbstractFixedASTNode implements Statement {
    public static final String CONDITION = "condition";
    public static final String BLOCK = "block";
    
    private Token keyword;
    
    public CaseStatement(){
        super( "CaseStatement", CONDITION, BLOCK );
    }
    
    public CaseStatement( Token keyword, ASTNode condition, ASTNode block ){
        this();
        setKeyword( keyword );
        setField( CONDITION, condition );
        setField( BLOCK, block );
    }
    
    public CaseStatement( Expression condition, Statement block ){
        this();
        setCondition( condition );
        setBlock( block );
    }
    
    public void setKeyword( Token keyword ){
        this.keyword = keyword;
    }
    public Token getKeyword(){
        return keyword;
    }
    
    public void setCondition( Expression condition ){
        setField( 0, condition );
    }
    public Expression getCondition(){
        return (Expression)getNoError( 0 );
    }
    
    public void setBlock( Statement block ){
        setField( 1, block );
    }
    public Statement getBlock(){
        return (Statement)getNoError( 1 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Expression ) )
                throw new ASTException( node, "Must be an Expression" );
        }
        if( index == 1 ) {
            if( !( node instanceof Statement ) )
                throw new ASTException( node, "Must be a Statement" );
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

    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            check( stack );
        }
    }
    
    private void check( AnalyzeStack stack ){
        SwitchStatement.SwitchResolver resolver = stack.get( SwitchStatement.SWITCH_RESOLVER );
        if( resolver == null ){
            stack.error( "case statement outside of switch block", getKeyword() );
        }
        
        Expression condition = getCondition();
        if( condition == null )
            return;
        
        if( condition.hasCommas() ){
            stack.error( "only one label allowed", condition );
            return;
        }
        
        Value value = condition.resolveConstantValue();
        if( value == null ){
            stack.error( "case label must be constant", condition );
        }
        else if( resolver != null ){
            resolver.put( value, this );
        }
        
        Type type = condition.resolveType();
        if( type == null || type.asBase() == null || !type.asBase().isIntegerType() ){
            stack.error( "case label must be an integer type", condition );
        }
    }
    
    public void flow( ControlFlow flow ){
        flow.line( this, getBlock() );
    }
    
    public Type resolveExpressionType(){
        return BaseType.VOID;
    }
    
    public boolean isFunctionEnd(){
        return false;
    }
}
