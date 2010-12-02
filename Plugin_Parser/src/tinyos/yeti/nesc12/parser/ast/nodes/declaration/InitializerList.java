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
package tinyos.yeti.nesc12.parser.ast.nodes.declaration;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;

public class InitializerList extends AbstractListASTNode<Initializer> implements Initializer{
    public InitializerList(){
        super( "InitializerList" );
    }
    
    public InitializerList( Initializer child ){
        this();
        add( child );
    }

    public Type resolveType() {
    	if( getChildrenCount() != 1 )
    		return null;
    	
    	Initializer child = getNoError( 0 );
    	if( child == null )
    		return null;
    	
    	return child.resolveType();
    }
    
    public boolean isAssignmentable(){
    	if( getChildrenCount() != 1 )
    		return false;
    	
    	Initializer child = getNoError( 0 );
    	if( child == null )
    		return false;
    	
    	return child.isAssignmentable();
    }

    public Value resolveValue(){
    	if( getChildrenCount() != 1 )
    		return null;
    	
    	Initializer child = getNoError( 0 );
    	if( child == null )
    		return null;
    	
    	return child.resolveValue();
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    
    @Override
    public InitializerList add( Initializer node ) {
        super.add( node );
        return this;
    }
    
    @Override
    protected void checkChild( Initializer child ) throws ASTException {
        
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