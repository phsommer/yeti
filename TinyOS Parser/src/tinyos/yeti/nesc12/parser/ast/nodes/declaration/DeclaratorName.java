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
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class DeclaratorName extends AbstractFixedASTNode implements Declarator{
    public DeclaratorName(){
        super( "DeclaratorName", "name" );
    }
    
    public DeclaratorName( Identifier name ){
        this();
        setName( name );
    }
    
    public Type resolveType( Type name, AnalyzeStack stack ){
        return resolved( "type", name );
    }
    
    public Modifiers resolveModifiers(){
    	if( isResolved( "modifiers" ))
    		return resolved( "modifiers" );
    	
    	ASTNode node = this;
    	while( node != null ){
    		DeclarationSpecifierList specifiers;
    		
    		if( node instanceof Declaration ){
    			specifiers = ((Declaration)node).getSpecifiers();
    		}
    		else if( node instanceof FunctionDefinition ){
    			specifiers = ((FunctionDefinition)node).getSpecifiers();
    		}
    		else{
    			node = node.getParent();
    			continue;
    		}
    		
    		if( specifiers == null )
				return resolved( "modifiers", null );
			return resolved( "modifiers", specifiers.resolveModifiers() );
    	}
    	
    	return resolved( "modifiers", null );
    }
    
    public ModelAttribute[] resolveAttributes(){
    	return null;
    }
    
    /**
     * Traverses the tree upwards to find attributes.
     * @return the attributes
     */
    public ModelAttribute[] resolveModelAttributes(){
    	if( isResolved( "attributes" ))
    		return resolved( "attributes" );
    	
    	ASTNode node = this;
    	Declarator declarator = null;
    	while( node instanceof Declarator ){
    		declarator = (Declarator)node;
    		node = node.getParent();
    	}
    	
    	return resolved( "attributes", declarator.resolveAttributes() );
    }
    
    public Type resolveType(){
    	return resolved( "type" );
    }
    
    public Name resolveName() {
        return resolved( "name" );
    }
    
    public Field[] resolveIndices() {
    	return null;
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        Identifier name = getName();
        if( name != null ){
            resolved( "name", stack.name( name ) );
        }
    }
    
    public void setName( Identifier name ){
        setField( 0, name );
    }
    
    public Identifier getName(){
        return (Identifier)getNoError( 0 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !( node instanceof Identifier ))
            throw new ASTException( node, "Must be an Identifier" );
    }

    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }

    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }
    
    public FunctionDeclarator getFunction(){
    	return null;
    }
}
