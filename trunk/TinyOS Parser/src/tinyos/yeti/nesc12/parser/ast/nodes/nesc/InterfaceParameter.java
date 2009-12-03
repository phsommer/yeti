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

import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class InterfaceParameter extends AbstractFixedASTNode{
    public static final String NAME = "name";
    public static final String ATTRIBUTES = "attributes";

    public InterfaceParameter(){
        super( "InterfaceParameter", NAME, ATTRIBUTES );
    }

    public InterfaceParameter( ASTNode name, ASTNode attributes ){
        this();
        setField( NAME, name );
        setField( ATTRIBUTES, attributes );
    }

    public InterfaceParameter( Identifier name, AttributeList attributes ){
        this();
        setName( name );
        setAttributes( attributes );
    }

    public GenericType resolveGenericType(){
        if( isResolved( "type" ))
            return resolved( "type" );

        Identifier name = getName();
        if( name == null )
            return resolved( "type", null );

        return resolved( "type", new GenericType( name.getName() ) );
    }

    public ModelAttribute[] resolveAttributes(){
    	AttributeList list = getAttributes();
    	if( list == null )
    		return null;
    	return list.resolveModelAttributes();
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        stack.checkCancellation();

        GenericType type = resolveGenericType();
        if( type != null ){
            // check whether interface is in an error list
            int top = 0;

            ASTNode parent = getParent();
            if( parent instanceof InterfaceParameterList ){
                parent = parent.getParent();
                if( parent instanceof Interface ){
                    parent = parent.getParent();
                    if( parent instanceof ErrorASTNode ){
                        top = 1;
                    }
                }
            }

            // type is null if name is null
            stack.putTypedef( stack.name( getName() ), type, resolveAttributes(), null, top );
        }
    }

    public void setName( Identifier name ){
        setField( 0, name );
    }
    public Identifier getName(){
        return (Identifier)getNoError( 0 );
    }

    public void setAttributes( AttributeList attributes ){
        setField( 1, attributes );
    }
    public AttributeList getAttributes(){
        return (AttributeList)getNoError( 1 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Identifier ) )
                throw new ASTException( node, "Must be an Identifier" );
        }
        if( index == 1 ) {
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
