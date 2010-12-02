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
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;

public class DefaultStatement extends AbstractFixedASTNode implements Statement{
    private Token keyword;
    
    public DefaultStatement(){
        super( "DefaultStatement", "block" );
    }
    
    public DefaultStatement( Token keyword, Statement block ){
        this();
        setKeyword( keyword );
        setBlock( block );
    }
    
    public void setKeyword( Token keyword ){
        this.keyword = keyword;
    }
    public Token getKeyword(){
        return keyword;
    }
    
    public void setBlock( Statement block ){
        setField( 0, block );
    }
    public Statement getBlock(){
        return (Statement)getNoError( 0 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !( node instanceof Statement ) )
            throw new ASTException( node, "Must be a Statement" );
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
            SwitchStatement.SwitchResolver resolver = stack.get( SwitchStatement.SWITCH_RESOLVER );
            if( resolver == null ){
                stack.error( "default statement outside a switch block", getKeyword() );
            }
            else{
                resolver.put( this );
            }
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
