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
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.StringLiteralList;

public class ASMArgument extends AbstractFixedASTNode{
    public static final String OPERAND = "operand";
    public static final String VARIABLE = "variable";
    
    public ASMArgument(){
        super( "ASMArgument", OPERAND, VARIABLE );
    }
    
    public ASMArgument( ASTNode operand, ASTNode variable ){
        this();
        setField( 0, operand );
        setField( 1, variable );
    }
    
    public ASMArgument( StringLiteralList operand, Expression variable ){
        this();
        setOperator( operand );
        setVariable( variable );
    }
    
    public void setOperator( StringLiteralList operand ){
        setField( 0, operand );
    }
    public StringLiteralList getOperand(){
        return (StringLiteralList)getNoError( 0 );
    }
    
    public void setVariable( Expression variable ){
        setField( 1, variable );
    }
    public Expression getVariable(){
        return (Expression)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException{
        if( index == 0 ){
            if( !( node instanceof StringLiteralList ) )
                throw new ASTException( node, "Must be a StringLiteralList" );
        }
        if( index == 1 ){
            if( !(node instanceof Expression ))
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

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
}
