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
package tinyos.yeti.nesc12.parser.ast.nodes.error;

import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractTokenASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;

public class TokenErrorASTNode extends AbstractTokenASTNode implements ErrorASTNode{
    private String message;
    
    public TokenErrorASTNode( String message, Token token ){
        super( "Error", token );
        if( token == null )
            throw new IllegalArgumentException( "token must not be null" );
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage( String message ) {
        this.message = message;
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
        if( stack.isReportErrors() ){
            stack.error( message, this );
        }
        super.resolve( stack );
    }
    
}
