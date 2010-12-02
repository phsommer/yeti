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
import tinyos.yeti.nesc12.ep.nodes.GenericTypeModelConnection;
import tinyos.yeti.nesc12.ep.nodes.GenericTypeModelNode;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleField;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.AttributedDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclarationSpecifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.StorageClass;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

public class TemplateParameter extends AbstractFixedASTNode{
    public static final String SPECIFIERS = "specifiers";
    public static final String DECLARATOR = "declarator";
    
    public TemplateParameter(){
        super( "TemplateParameter", SPECIFIERS, DECLARATOR );
    }
    
    public TemplateParameter( ASTNode specifiers, ASTNode declarator ){
        this();
        setField( SPECIFIERS, specifiers );
        setField( DECLARATOR, declarator );
    }
    
    public TemplateParameter( Token typedef, Identifier name, AttributeList attributes ){
        this();
        setSpecifiers( new DeclarationSpecifierList( new StorageClass( typedef, StorageClass.Storage.TYPEDEF )));
        Declarator declarator = new DeclaratorName( name );
        if( attributes != null )
            declarator = new AttributedDeclarator( declarator, attributes );

        setDeclarator( declarator );
    }
    
    public TemplateParameter( DeclarationSpecifierList specifiers, Declarator declarator ){
        this();
        setSpecifiers( specifiers );
        setDeclarator( declarator );
    }
    
    /**
     * Tells whether this parameter has the form "typedef x".
     * @return <code>true</code> if this parameter defines a new type
     */
    public boolean isTypedef(){
        if( isResolved( "typedef" ))
            return resolved( "typedef" );
        
        DeclarationSpecifierList list = getSpecifiers();
        if( list == null )
            return resolved( "typedef", false );
        
        if( list.getChildrenCount() != 1 )
            return resolved( "typedef", false );
        
        Declarator decl = getDeclarator();
        if( decl == null )
            return resolved( "typedef", false );
        
        Modifiers modifiers = list.resolveModifiers();
        if( modifiers == null )
            return resolved( "typedef", false );
            
        if( decl instanceof AttributedDeclarator ){
            decl = ((AttributedDeclarator)decl).getDeclarator();
            if( decl == null )
                return resolved( "typedef", false );
        }
        
        // TODO decl could be attributed
        return resolved( "typedef", decl instanceof DeclaratorName && modifiers.isTypedef() );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        
        if( stack.isCreateModel() ){
        	NodeStack nodes = stack.getNodeStack();
            nodes.pushNode( null );
            nodes.setRange( getRange() );
        }
        
        stack.getDeclarationStack().push();
        
        super.resolve( stack );
        stack.checkCancellation();

        Declarator decl = getDeclarator();
        if( decl != null ){
            Name name = decl.resolveName();
            if( name != null ){
                if( isTypedef() ){
                    DeclarationSpecifierList list = getSpecifiers();
                    if( list != null ){
                        list.checkModifiers( 
                                stack, 
                                null,
                                Modifiers.TYPEDEF, 
                                Modifiers.ALL & ~Modifiers.TYPEDEF,
                                null,
                                true,
                                null );
                    }
                    
                    GenericTypeModelNode node = null;
                    
                    if( stack.isCreateModel() ){
                        node = new GenericTypeModelNode( name.toIdentifier() );
                        node.setAttributes( decl.resolveAttributes() );
                        NodeStack nodes = stack.getNodeStack();
                        
                        nodes.include( node, this );
                        nodes.setNode( node );
                        nodes.addLocation( this );
                        nodes.addConnection( new GenericTypeModelConnection( node, decl ), 1 );
                    }
                    
                    stack.getDeclarationStack().set( name.toIdentifier() );
                    stack.putTypedef( name, new GenericType( name.toIdentifier() ), decl.resolveAttributes(), node );
                }
                else{
                    DeclarationSpecifierList list = getSpecifiers();
                    if( list != null ){
                        list.checkResolvesType( stack );
                        list.checkModifiers(
                                stack,
                                null,
                                Modifiers.ALL_TYPE_QUALIFIER,
                                Modifiers.ALL_NESC | Modifiers.ALL_REMAINING | Modifiers.ALL_STORAGE_CLASSES, 
                                null,
                                true,
                                null );
                        
                        Type type = list.resolveType();
                        if( type != null ){
                        	type = decl.resolveType( type, stack );
                            if( type != null ){
                            	stack.getDeclarationStack().set( FieldModelNode.fieldId( name, type ) );
                            	if( stack.isCreateModel() ){
                                    NodeStack nodes = stack.getNodeStack();
                                    Modifiers modifiers = list.resolveModifiers();
                                    ModelAttribute[] attributes = decl.resolveAttributes();
                                    
                                    FieldModelNode node = new FieldModelNode( name, attributes, modifiers, type, new UnknownValue( type ), false );
                                    nodes.setNode( node );
                                    nodes.addLocation( node.getRange() );
                                    nodes.include( node, this );
                                    nodes.addChild( node, 1, this );
                                    nodes.addType( type );
                                    stack.putField( node, getRange().getRight(), false );
                                }
                                else{
                                    SimpleField field = stack.getDeclarationStack().set( list.resolveModifiers(), type, name, null, new UnknownValue( type ), null );
                                    stack.putField( field, getRange().getRight(), false );
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if( stack.isCreateModel() ){
            stack.getNodeStack().popNode( null );
        }
        
        stack.getDeclarationStack().pop();
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
    public Declarator getDeclarator(){
        return (Declarator)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof DeclarationSpecifierList ) )
                throw new ASTException( node, "Must be a DeclarationSpecifierList" );
        }
        if( index == 1 ) {
            if( !( node instanceof Declarator ) )
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
