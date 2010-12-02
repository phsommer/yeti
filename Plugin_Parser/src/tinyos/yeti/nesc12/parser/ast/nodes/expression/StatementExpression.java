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
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;

public class StatementExpression extends AbstractFixedExpression{
    public static final String STATEMENT = "statement";
    
    public StatementExpression(){
        super( "StatementExpression", STATEMENT );
    }
    
    public StatementExpression( ASTNode node ){
        this();
        setField( STATEMENT, node );
    }
    
    public StatementExpression( CompoundStatement statement ){
        this();
        setStatement( statement );
    }
    
    public void setStatement( CompoundStatement statement ){
        setField( 0, statement );
    }
    
    public CompoundStatement getStatement(){
        return (CompoundStatement)getNoError( 0 );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ){
        super.resolve( stack );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException{
        if( index == 0 ){
            if( !( node instanceof CompoundStatement )){
                throw new ASTException( this, "Must be a CompoundStatement" );
            }
        }
    }

    @Override
    protected void endVisit( ASTVisitor visitor ){
        visitor.endVisit( this );
    }

    @Override
    protected boolean visit( ASTVisitor visitor ){
        return visitor.visit( this );
    }

    public boolean hasCommas(){
        return false;
    }

    public boolean isConstant(){
        return false;
    }

    public Value resolveConstantValue(){
        return null;
    }

    public Type resolveType(){
        CompoundStatement statement = getStatement();
        if( statement == null )
            return null;
        
        return statement.resolveExpressionType();
    }
}
