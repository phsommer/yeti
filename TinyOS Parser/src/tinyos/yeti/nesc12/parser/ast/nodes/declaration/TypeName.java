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
import tinyos.yeti.nesc12.parser.ast.elements.Generic;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.GenericArgument;

public class TypeName extends AbstractFixedASTNode implements GenericArgument{
    public static final String MODIFIERS = "modifiers";
    public static final String DECLARATOR = "declarator";
    
    public TypeName(){
        super( "TypeName", MODIFIERS, DECLARATOR );
    }
    
    public TypeName( ASTNode modifiers, ASTNode declarator ){
        this();
        setField( MODIFIERS, modifiers );
        setField( DECLARATOR, declarator );
    }
    
    public TypeName( DeclarationSpecifierList modifiers, AbstractDeclarator declarator ){
        this();
        setModifiers( modifiers );
        setDeclarator( declarator );
    }
    
    public Generic resolveGeneric() {
        return resolveType( null );
    }
    
    public Type resolveType(){
        return resolveType( null );
    }
    
    private Type resolveType( AnalyzeStack stack ){
        if( isResolved( "type" ))
            return resolved( "type" );
        
        DeclarationSpecifierList list = getModifiers();
        if( list == null )
            return resolved( "type", null );
        
        Type base = list.resolveType();
        if( base == null )
            return resolved( "type", null );
        
        AbstractDeclarator decl = getDeclarator();
        if( decl != null ){
            base = decl.resolveType( base, stack );
        }
        
        return resolved( "type", base );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            resolveType( stack );
            
            DeclarationSpecifierList list = getModifiers();
            if( list != null ){
                list.checkResolvesType( stack );                
                list.checkModifiers(
                        stack, 
                        null,
                        0,
                        Modifiers.ALL & ~Modifiers.ALL_TYPE_QUALIFIER, 
                        null,
                        true,
                        null );
            }
        }
    } 

    public void setModifiers( DeclarationSpecifierList modifiers ){
        setField( 0, modifiers );
    }
    public DeclarationSpecifierList getModifiers(){
        return (DeclarationSpecifierList)getNoError( 0 );
    }
    
    public void setDeclarator( AbstractDeclarator declarator ){
        setField( 1, declarator );
    }
    public AbstractDeclarator getDeclarator(){
        return (AbstractDeclarator)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !(node instanceof DeclarationSpecifierList ))
                throw new ASTException( node, "Must be a DeclarationSpecifierList" );
        }
        if( index == 1 ){
            if( !(node instanceof AbstractDeclarator ))
                throw new ASTException( node, "Must be an AbstractDeclarator" );
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
