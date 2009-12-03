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
import tinyos.yeti.nesc12.parser.ast.elements.Generic;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeUtility;
import tinyos.yeti.nesc12.parser.ast.elements.values.StringValue;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;

public class StringLiteralList extends AbstractListASTNode<StringLiteral> implements Expression{
    public StringLiteralList(){
        super( "StringLiteralList" );
    }
    
    public StringLiteralList( StringLiteral child ){
        this();
        add( child );
    }
    
    @Override
    public StringLiteralList add( StringLiteral node ) {
        super.add( node );
        return this;
    }

    @Override
    protected void checkChild( StringLiteral child ) throws ASTException {
        // nothing to do
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
        if( isResolved( "type" ))
            return resolved( "type" );
        
        Type type = null;
        
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
        	StringLiteral child = getNoError( i );
        	if( child != null ){
        		Type childType = child.resolveType();
        		if( childType != null ){
        			if( BaseType.U_CHAR.equals( TypeUtility.raw( TypeUtility.pointer( childType )) )){
        				if( type == null )
        					type = childType;
        			}
        			else{
        				type = childType;
        			}
        		}
        	}
        }
        
        return resolved( "type", type );
    }
    
    public Generic resolveGeneric() {
        return resolveConstantValue();
    }
    
    public Value[] resolveConstantValues() {
        if( isResolved( "values" ))
            return resolved( "values" );
        return resolved( "values", new Value[]{ resolveConstantValue() } );
    }
    
    public Value resolveConstantValue() {
        if( isResolved( "value" ))
            return resolved( "value" );
        
        if( getChildrenCount() == 1 ){
            StringLiteral child = getTypedChild( 0 );
            if( child == null )
                return resolved( "value", null );
            
            return resolved( "value", child.resolveConstantValue() );
        }
        
        int[][] values = new int[getChildrenCount()][];
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            StringLiteral child = getTypedChild( i );
            if( child == null )
                return resolved( "value", null );
            
            Value value = child.resolveConstantValue();
            if( value == null )
                return resolved( "value", null );
            
            if( !(value instanceof StringValue))
                return resolved( "value", null );
            
            values[i] = ((StringValue)value).getValue();
        }
        
        int size = 0;
        for( int[] value : values )
            size += value.length-1;
        
        size += 1;
        int[] result = new int[ size ];
        int offset = 0;
        
        for( int[] value : values ){
            System.arraycopy( value, 0, result, offset, value.length-1 );
            offset += value.length-1;
        }
        
        return resolved( "value", new StringValue( resolveType(), false, result ) );
    }
    
    public boolean hasCommas() {
        return false;
    }

    public boolean isConstant() {
        return true;
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
}
