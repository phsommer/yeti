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

import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;

public class ParenthesizedDeclarator extends AbstractFixedASTNode implements Declarator {
    public static final String DECLARATOR = "declarator";
    
    public ParenthesizedDeclarator(){
        super( "ParenthesizedDeclarator", DECLARATOR );
    }
    
    public ParenthesizedDeclarator( ASTNode declarator ){
        this();
        setField( DECLARATOR, declarator );
    }
    
    public ParenthesizedDeclarator( Declarator declarator ){
        this();
        setDeclarator( declarator );
    }
    
    public Type resolveType( Type name, AnalyzeStack stack ) {
        String id = "type";
        if( isResolved( id ))
            return resolved( id );
        
        Declarator declarator = getDeclarator();
        if( declarator == null )
            return resolved( id, null );
        
        return resolved( id, declarator.resolveType( name, stack ) );
    }
    
    public Name resolveName() {
        if( isResolved( "name" ))
            return resolved( "name" );
        
        Declarator decl = getDeclarator();
        if( decl == null )
            return resolved( "name", null );
        
        return resolved( "name", decl.resolveName() );
    }
    
    public ModelAttribute[] resolveAttributes(){
    	if( isResolved( "attributes" ))
    		return resolved( "attributes" );
    	
    	Declarator decl = getDeclarator();
    	if( decl == null )
    		return resolved( "attributes", null );
    		
    	return resolved( "attributes", decl.resolveAttributes() );
    }
    
    public Field[] resolveIndices() {
    	if( isResolved( "indices" ))
    		return resolved( "indices" );
    	
    	Declarator decl = getDeclarator();
    	if( decl == null )
    		return resolved( "indices", null );
    	
    	return resolved( "indices", decl.resolveIndices() );
    }

    public FunctionDeclarator getFunction(){
    	Declarator declarator = getDeclarator();
    	if( declarator == null ){
    		return null;
    	}
    	else{
    		return declarator.getFunction();
    	}
    }
    
    public void setDeclarator( Declarator declarator ){
        setField( 0, declarator );
    }
    public Declarator getDeclarator(){
        return (Declarator)getNoError( 0 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !(node instanceof Declarator))
            throw new ASTException( node, "Must be a Declarator" );
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
