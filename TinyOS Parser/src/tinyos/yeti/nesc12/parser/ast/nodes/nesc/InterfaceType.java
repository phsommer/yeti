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

import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class InterfaceType extends AbstractFixedASTNode{
    public static final String NAME = "name";
    public static final String TYPES = "types";
    
    public InterfaceType(){
        super( "InterfaceType", "name", "types" );
    }
    
    public InterfaceType( ASTNode name, ASTNode types ){
        this();
        setField( NAME, name );
        setField( TYPES, types );
    }
    
    public InterfaceType( Identifier name, TypeList types ){
        this();
        setName( name );
        setTypes( types );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            Identifier name = getName();
            if( name != null ){
                ModelNode node = stack.getInterface( name.getName() );
                IDeclaration declaration = null;
                if( node == null ){
                    declaration = stack.getDeclarationResolver().resolve( name.getName(), stack.requestProgressMonitor(), Kind.INTERFACE );    
                }
                
                if( declaration == null && node == null ){
                    stack.error( "missing declaration for interface '" + name.getName() + "'", name );
                }
                
                if( node == null && declaration != null ){
                    node = stack.getDeclarationResolver().resolve( declaration, stack.requestProgressMonitor() );
                    stack.checkCancellation();
                }
                
                if( node instanceof InterfaceModelNode ){
                    InterfaceModelNode interfaze = (InterfaceModelNode)node;

                    GenericType[] generics = interfaze.getGenerics();
                    TypeList types = getTypes();

                    if( generics == null || generics.length == 0 ){
                        if( types != null && types.getChildrenCount() > 0 ){
                            stack.error( "interface does not need generic types", types );
                        }
                    }
                    else{
                        if( types == null ){
                            stack.error( "interface '" + name.getName() + "' requires " + generics.length + " generic types", name );
                        }
                        else if( types.getChildrenCount() != generics.length ){
                            stack.error( "interface '" + name.getName() + "' requires " + generics.length + " generic types", types );
                        }
                    }
                }
            }
        }
    }
    
    public void setName( Identifier name ){
        setField( 0, name );
    }
    public Identifier getName(){
        return (Identifier)getNoError( 0 );
    }
    
    public void setTypes( TypeList types ){
        setField( 1, types );
    }
    public TypeList getTypes(){
        return (TypeList)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Identifier ) )
                throw new ASTException( node, "Must be an Identifier" );
        }
        if( index == 1 ) {
            if( !( node instanceof TypeList ) )
                throw new ASTException( node, "Must be a TypeList" );
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
