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

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.ep.declarations.TypedDeclaration;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AttributeList;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusherFactory;

public class AttributeDeclaration extends AbstractFixedASTNode implements TypeSpecifier{
    public static final String SPECIFIER = "specifier";
    public static final String NAME = "name";
    public static final String ATTRIBUTES = "attributes";
    public static final String FIELDS = "fields";
    
    public AttributeDeclaration(){
        super( "AttributeDeclaration", SPECIFIER, NAME, ATTRIBUTES, FIELDS );
    }
    
    public AttributeDeclaration( ASTNode specifier, ASTNode name, ASTNode attributes, ASTNode fields ){
        this();
        setField( SPECIFIER, specifier );
        setField( NAME, name );
        setField( ATTRIBUTES, attributes );
        setField( FIELDS, fields );
    }
    
    public AttributeDeclaration( DataObjectSpecifier specifier, Identifier name, AttributeList attributes, DataObjectFieldDeclarationList fields ){
        this();
        setSpecifier( specifier );
        setName( name );
        setAttributes( attributes );
        setFields( fields );
    }
    
    public ModelAttribute[] resolveModelAttributes(){
    	AttributeList list = getAttributes();
    	if( list == null )
    		return null;
    	return list.resolveModelAttributes();
    }

    /**
     * Resolves the type of this attribute declaration using a normal struct.
     * @return the contents of this declaration
     */
    public DataObjectType resolveType(){
        if( isResolved( "type" ))
            return resolved( "type" );
        
        DataObjectSpecifier specifier = getSpecifier();
        if( specifier == null )
            return resolved( "type", null );
        if( specifier.getSpecifier() != DataObjectSpecifier.Specifier.STRUCT )
            return resolved( "type", null );
        
        Identifier name = getName();
        if( name == null )
            return resolved( "type", null );
        
        DataObjectFieldDeclarationList fields = getFields();
        DataObjectType type = DataObjectType.attribute( name, fields );
        return resolved( "type", type );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        NodeStack nodes = stack.getNodeStack();
        
        if( nodes != null && stack.isCreateModel() ){
            nodes.pushNode( null );
            nodes.setRange( getRange() );
        }
        
        stack.getDeclarationStack().push();
        
        stack.push( FieldPusherFactory.STANDARD );
        
        super.resolve( stack );
        
        stack.checkCancellation();

        if( stack.isReportErrors() ){
            DataObjectSpecifier specifier = getSpecifier();
            if( specifier != null ){
                if( specifier.getSpecifier() != DataObjectSpecifier.Specifier.STRUCT ){
                    stack.error( "specifier must be a 'struct'", specifier );
                }
            }
            
            checkIncompleteTypes( stack );
        }
        
        stack.checkCancellation();
        
        if( getName() != null ){
        	String name = getName().getName();
        	
        	StandardModelNode node = null;
            
            if( stack.isCreateDeclarations() ){
                DataObjectType type = resolveType();
                if( type != null ){
                    
                    TypedDeclaration declaration = new TypedDeclaration(
                            Kind.ATTRIBUTE, 
                            type,
                            name, 
                            name,
                            stack.getParseFile(),
                            null,
                            TagSet.get( Tag.ATTRIBUTE ));
                    declaration.setFileRegion( stack.getRegion( getName() ) );
                    stack.getDeclarationStack().set( declaration );
                }
            }
            else{
            	stack.getDeclarationStack().set( getName().getName() );
            }
            
            if( nodes != null && stack.isCreateModel() ){
                node = new StandardModelNode( getName().getName(), false, Tag.ATTRIBUTE, Tag.IDENTIFIABLE );
                node.setDocumentation( getComments() );
                node.setAttributes( getAttributes() );
                node.setLabel( node.getIdentifier() );
                node.setNodeName( node.getIdentifier() );
                
                nodes.include( node, this );
                nodes.outline( node, 1 );
                nodes.addLocation( getName() );
                nodes.addLocation( this );
                
                nodes.addChild( node, 1, getName() );
                nodes.setNode( node );
            }

            stack.putAttribute( this, node );
        }
        
        
        stack.pop( getRight() );
        
        if( nodes != null && stack.isCreateModel() ){
            nodes.popNode( null );
        }
        
        stack.getDeclarationStack().pop();
    }
    
    @Override
    public Range getCommentAnchor(){
    	Identifier name = getName();
    	if( name == null )
    		return null;
    	return name.getRange();
    }
    
    private void checkIncompleteTypes( AnalyzeStack stack ){
        DataObjectFieldDeclarationList list = getFields();
        if( list == null )
            return;
        
        DataObjectSpecifier specifier = getSpecifier();
        if( specifier == null )
            return;

        list.checkIncompleteTypes( stack, true );
    }
    
    public boolean isSpecifier() {
        return true;
    }
    
    public boolean isStorageClass() {
        return false;
    }
    
    public boolean isPrimitive() {
        return false;
    }
    
    public boolean isDataObject() {
        return false;
    }
    
    public boolean isAttribute(){
        return true;
    }
    
    public boolean isEnum() {
        return false;
    }
    
    public boolean isTypedefName() {
        return false;
    }

    public DataObjectSpecifier getSpecifier(){
        return (DataObjectSpecifier)getNoError( 0 );
    }
    
    public void setSpecifier( DataObjectSpecifier specifier ){
        setField( 0, specifier );
    }
    
    public Identifier getName(){
        return (Identifier)getNoError( 1 );
    }
    
    public void setName( Identifier name ){
        setField( 1, name );
    }
    
    public AttributeList getAttributes(){
    	return (AttributeList)getNoError( 2 );
    }
    
    public void setAttributes( AttributeList attributes ){
    	setField( 2, attributes );
    }
    
    public DataObjectFieldDeclarationList getFields(){
        return (DataObjectFieldDeclarationList)getNoError( 3 );
    }
    
    public void setFields( DataObjectFieldDeclarationList fields ){
        setField( 3, fields );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !( node instanceof DataObjectSpecifier ))
                throw new ASTException( node, "Must be a DataObjectSpecifier" );
        }
        if( index == 1 ){
            if( !( node instanceof Identifier ))
                throw new ASTException( node, "Must be an Identifier" );
        }
        if( index == 2 ){
        	if( !(node instanceof AttributeList ))
        		throw new ASTException( node, "Must be an AttributeList" );
        }
        if( index == 3 ){
            if( !(node instanceof DataObjectFieldDeclarationList ))
                throw new ASTException( node, "DataObjectFieldDeclarationList" );
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
