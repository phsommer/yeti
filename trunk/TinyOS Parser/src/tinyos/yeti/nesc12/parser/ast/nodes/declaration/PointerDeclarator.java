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

public class PointerDeclarator extends AbstractFixedASTNode implements Declarator{
    public static final String POINTER = "pointer";
    public static final String DECLARATOR = "declarator";
    
    public PointerDeclarator(){
        super( "PointerDeclarator", POINTER, DECLARATOR );
    }
    
    public PointerDeclarator( ASTNode pointer, ASTNode declarator ){
        this();
        setField( POINTER, pointer );
        setField( DECLARATOR, declarator );
    }
    
    public PointerDeclarator( Pointer pointer, Declarator declarator ){
        this();
        setPointer( pointer );
        setDeclarator( declarator );
    }
    
    public Type resolveType( Type name, AnalyzeStack stack ) {
        String id = "type";
        
        if( isResolved( id ))
            return resolved( id );
        
        if( name == null )
            return resolved( id, null );
        
        Pointer pointer = getPointer();
        if( pointer == null )
            return resolved( id, null );
        
        name = pointer.resolveType( name );
        
        Declarator decl = getDeclarator();
        if( decl == null )
            return resolved( id, null );
        
        Type type = decl.resolveType( name, stack );
        if( type == null )
            return resolved( id, null );
        
        return resolved( id, type );
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
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
    }
    
    public void setPointer( Pointer pointer ){
        setField( 0, pointer );
    }
    
    public Pointer getPointer(){
        return (Pointer)getNoError( 0 );
    }
    
    public void setDeclarator( Declarator declarator ){
        setField( 1, declarator );
    }
    
    public Declarator getDeclarator(){
        return (Declarator)getNoError( 1 );
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
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !( node instanceof Pointer ))
                throw new ASTException( node, "Must be a Pointer" );
        }
        if( index == 1 ){
            if( !( node instanceof Declarator ))
                throw new ASTException( node, "Must be a Declarator" );
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
}
