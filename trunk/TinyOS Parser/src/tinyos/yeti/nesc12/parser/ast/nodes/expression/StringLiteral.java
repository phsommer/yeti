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
import tinyos.yeti.nesc12.parser.ast.elements.Generic;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.values.StringValue;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractTokenASTNode;

public class StringLiteral extends AbstractTokenASTNode implements Expression{
    private StringValue value;
    
    public StringLiteral( Token token ){
        super( "StringLiteral", token );
    }
    
    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }
    
    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }
    
    public Type resolveType() {
        return value == null ? null : value.getType();
    }
    
    public Generic resolveGeneric() {
        return resolveConstantValue();
    }
    
    public Value[] resolveConstantValues() {
        if( isResolved( "values" ))
            return resolved( "values" );
        return resolved( "values", new Value[]{ resolveConstantValue() });
    }
    
    public Value resolveConstantValue() {
        return value;
    }
    
    public boolean hasCommas() {
        return false;
    }

    public boolean isConstant() {
        return true;
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        
        if( token != null ){
            String value = token.getText();
            boolean wide = value.startsWith( "L" );
            if( wide )
                value = value.substring( 2, value.length()-1 );
            else
                value = value.substring( 1, value.length()-1 );
            this.value = StringValue.string( value, wide, stack );
        }
    }
}
