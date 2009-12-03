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
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.ConstType;
import tinyos.yeti.nesc12.parser.ast.elements.types.PointerType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;

public class Pointer extends AbstractFixedASTNode {
    public Pointer(){
        super( "Pointer", "qualifiers", "pointer" );
    }
    public Pointer( DeclarationSpecifierList qualifiers, Pointer pointer ){
        this();
        setQualifiers( qualifiers );
        setPointer( pointer );
    }
    
    public Type resolveType( Type base ){
        Pointer pointer = getPointer();
        if( pointer != null ){
            base = pointer.resolveType( base );
        }
        
        base = new PointerType( base );
        
        Modifiers modifiers = resolveModifiers();
        if( modifiers != null ){
            if( modifiers.isConst() ){
                base = new ConstType( base );
            }
        }
        
        return base;
    }
    
    public Modifiers resolveModifiers(){
        if( isResolved( "modifiers" ))
            return resolved( "modifiers" );
        
        DeclarationSpecifierList qualifiers = getQualifiers();
        if( qualifiers == null )
            return resolved( "modifiers", null );
        
        return resolved( "modifiers", qualifiers.resolveModifiers() );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            DeclarationSpecifierList qualifiers = getQualifiers();
            if( qualifiers != null ){
                qualifiers.checkModifiers(
                        stack,
                        null,
                        Modifiers.ALL_TYPE_QUALIFIER,
                        Modifiers.ALL & ~Modifiers.ALL_TYPE_QUALIFIER,
                        null,
                        true,
                        null );
            }
        }
    }
    
    public void setQualifiers( DeclarationSpecifierList qualifiers ){
        setField( 0, qualifiers );
    }
    public DeclarationSpecifierList getQualifiers(){
        return (DeclarationSpecifierList)getNoError( 0 );
    }
    
    public void setPointer( Pointer pointer ){
        setField( 1, pointer );
    }
    public Pointer getPointer(){
        return (Pointer)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !(node instanceof DeclarationSpecifierList))
                throw new ASTException( node, "Must be a DeclarationSpecifierList" );
        }
        if( index == 1 ){
            if( !(node instanceof Pointer))
                throw new ASTException( node, "Must be a Pointer" );
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
