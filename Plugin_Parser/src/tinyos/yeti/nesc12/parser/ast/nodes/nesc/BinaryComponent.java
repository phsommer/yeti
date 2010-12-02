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

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.ep.nodes.BinaryComponentModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.ExternalDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusherFactory;

public class BinaryComponent extends AbstractFixedASTNode implements ExternalDeclaration{
    public static final String NAME = "name";
    public static final String ATTRIBUTES = "attributes";
    public static final String CONNECTIONS = "connections";
    
    public BinaryComponent(){
        super( "BinaryComponent", NAME, ATTRIBUTES, CONNECTIONS );
    }
    
    public BinaryComponent( ASTNode name, ASTNode attributes, ASTNode connections ){
        this();
        setField( NAME, name );
        setField( ATTRIBUTES, attributes );
        setField( CONNECTIONS, connections );
    }
    
    public BinaryComponent( Identifier name, AttributeList attributes, AccessList connections ){
        this();
        setName( name );
        setAttributes( attributes );
        setConnections( connections );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        stack.folding( this );
        
        stack.pushScope( FieldPusherFactory.COMPONENT );
        
        Identifier id = getName();
        if( id == null ){
            super.resolve( stack );
            stack.checkCancellation();
        }
        else{
            stack.getDeclarationStack().push( id.getName() );
            
            if( stack.isCreateDeclarations() ){    	
                stack.getDeclarationStack().set( Kind.BINARY_COMPONENT, id.getName(), TagSet.get( Tag.BINARY_COMPONENT, Tag.COMPONENT ) );
            }
            
            if( stack.isCreateModel() ){
                NodeStack nodes = stack.getNodeStack();
                
                BinaryComponentModelNode node = new BinaryComponentModelNode( stack.name( id ));
                node.setDocumentation( getComments() );
                node.setAttributes( getAttributes() );
                nodes.include( node, this );
                nodes.addChild( node, this );
                nodes.pushNode( node );
                nodes.setRange( getRange() );
                nodes.addLocation( id );
                nodes.addLocation( this );
                
                stack.put( Access.COMPONENT, node );
                super.resolve( stack );
                stack.checkCancellation();
                
                stack.put( Access.COMPONENT, null );
                stack.putComponent( node );
                
                nodes.popNode( null );
            }
            else{
                super.resolve( stack );
            }
            
            stack.getDeclarationStack().pop();
            
            if( stack.isReportErrors() ){
                String name = id.getName();
                if( !stack.isParseFileName( name, "nc" ) ){
                    stack.warning( "Component '" + name + "' should be defined in a file '" + name + ".nc'", id );
                }
            }
        }
        
        stack.pop( getRange().getRight() );
    }
    
    @Override
    public Range getCommentAnchor(){
	    Identifier name = getName();
	    if( name == null )
	    	return null;
	    return name.getRange();
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
    
    public void setConnections( AccessList connections ){
        setField( 2, connections );
    }
    public AccessList getConnections(){
        return (AccessList)getNoError( 2 );
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
        if( index == 2 ) {
            if( !( node instanceof AccessList ) )
                throw new ASTException( node, "Must be an AccessList" );
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
