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

import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractTokenASTNode;

public class ArithmeticOperator extends AbstractTokenASTNode{
    public static enum Operator{
        MUL, DIV, MOD,
        ADD, SUB,
        SHIFT_LEFT, SHIFT_RIGHT,
        SMALLER, SMALLER_EQ, GREATER, GREATER_EQ,
        EQ, NOT_EQ,
        BIT_AND,
        BIT_XOR,
        BIT_OR,
        AND,
        OR
    }
    
    private Operator operator;
    
    public ArithmeticOperator( Token token, Operator operator ){
        super( "ArithmeticOperator", token );
        setOperator( operator );
    }
    
    public void setOperator( Operator operator ) {
        this.operator = operator;
    }
    public Operator getOperator() {
        return operator;
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
}
