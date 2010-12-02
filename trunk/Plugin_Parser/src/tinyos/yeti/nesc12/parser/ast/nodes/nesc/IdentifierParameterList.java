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
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;

public class IdentifierParameterList extends AbstractListASTNode<IdentifierParameter>{
    public IdentifierParameterList(){
        super( "IdentifierParameterList" );
    }
    
    public IdentifierParameterList( IdentifierParameter child ){
        this();
        add( child );
    }
    
    /**
     * Finds out what kind of parameters this list contains.
     * @return an array of parameter types, contains <code>null</code> entries
     * for parameters with errors
     */
    public Type[] resolveTypes(){
        if( isResolved( "types" ))
            return resolved( "types" );
        
        Type[] types = new Type[ getChildrenCount() ];
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            IdentifierParameter param = getNoError( i );
            if( param != null )
                types[i] = param.resolveType();
        }
        
        return resolved( "types", types );
    }
    
    public Field[] resolveFields( AnalyzeStack stack ){
    	if( isResolved( "fields" ))
    		return resolved( "fields" );
    	
    	Field[] fields = new Field[ getChildrenCount() ];
    	for( int i = 0, n = fields.length; i<n; i++ ){
    		IdentifierParameter param = getNoError( i );
    		if( param != null )
    			fields[i] = param.resolveField( stack );
    	}
    	
    	return resolved( "fields", fields );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    
    @Override
    public IdentifierParameterList add( IdentifierParameter node ) {
        super.add( node );
        return this;
    }
    
    @Override
    protected void checkChild( IdentifierParameter child ) throws ASTException {
        
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
