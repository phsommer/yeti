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

import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
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
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.AbstractDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclarationSpecifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declarator;
import tinyos.yeti.nesc12.parser.ast.util.DeclarationStack;

public class IdentifierParameter extends AbstractFixedASTNode{
    public IdentifierParameter(){
        super( "IdentifierParameter", "specifiers", "declarator", "attributes" );
    }
    
    public IdentifierParameter( DeclarationSpecifierList specifiers, ASTNode declarator, AttributeList attributes ){
        this();
        setSpecifiers( specifiers );
        setField( "declarator", declarator );
        setAttributes( attributes );
    }
    
    public Field resolveField( AnalyzeStack stack ){
    	if( isResolved( "field" ))
    		return resolved( "field" );
    	
    	Type type = resolveType();

    	if( type == null )
    		return resolved( "field", null );
    	
    	Name name = resolveName();
    	DeclarationStack declarations = stack.getDeclarationStack();
    	declarations.push( FieldModelNode.fieldId( name, type ));
    	Field field = declarations.set( null, type, name, resolveModelAttributes(), null, null );
    	declarations.pop();
    	return resolved( "field", field );
    }
    
    public ModelAttribute[] resolveModelAttributes(){
    	AttributeList list = getAttributes();
    	if( list == null )
    		return null;
    	return list.resolveModelAttributes();
    }
    
    public Type resolveType(){
        return resolveType( null );
    }
    
    private Type resolveType( AnalyzeStack stack ){
        if( isResolved( "type" ))
            return resolved( "type" );
        
        DeclarationSpecifierList specifiers = getSpecifiers();
        if( specifiers == null )
            return resolved( "type", null );
        
        Type base = specifiers.resolveType();
        if( base == null )
            return resolved( "type", null );
        
        ASTNode decl = getDeclarator();
        if( decl instanceof Declarator ){
            base = ((Declarator)decl).resolveType( base, stack );
        }
        else if( decl instanceof AbstractDeclarator ){
            base = ((AbstractDeclarator)decl).resolveType( base, stack );
        }
        
        return resolved( "type", base );
    }
    
    public Name resolveName(){
        if( isResolved( "name" ))
            return resolved( "name" );
        
        ASTNode decl = getDeclarator();
        if( !(decl instanceof Declarator ))
            return resolved( "name", null );
        
        return resolved( "name", ((Declarator)decl).resolveName() );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            resolveType( stack );
            
            DeclarationSpecifierList specifiers = getSpecifiers();
            if( specifiers != null ){
                specifiers.checkResolvesType( stack );
                specifiers.checkModifiers( stack,
                        null,
                        0,
                        Modifiers.ALL,
                        null,
                        true, 
                        null );
            }
        }
    }
    
    public void setSpecifiers( DeclarationSpecifierList specifiers ){
        setField( 0, specifiers );
    }
    public DeclarationSpecifierList getSpecifiers(){
        return (DeclarationSpecifierList)getNoError( 0 );
    }
    
    public void setDeclarator( Declarator declarator ){
        setField( 1, declarator );
    }
    public void setDeclarator( AbstractDeclarator declarator ){
        setField( 1, declarator );
    }
    
    public ASTNode getDeclarator(){
        return getNoError( 1 );
    }
    
    public void setAttributes( AttributeList attributes ){
        setField( 2, attributes );
    }
    public AttributeList getAttributes(){
        return (AttributeList)getNoError( 2 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof DeclarationSpecifierList ) )
                throw new ASTException( node, "Must be a DeclarationSpecifierList" );
        }
        if( index == 1 ) {
            if( !( node instanceof Declarator ) && !( node instanceof AbstractDeclarator ) )
                throw new ASTException( node, "Must be a Declarator or an AbstractDeclarator" );
        }
        if( index == 3 ) {
            if( !( node instanceof AttributeList ) )
                throw new ASTException( node, "Must be a NesCAttributeList" );
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
