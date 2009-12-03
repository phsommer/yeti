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

import java.util.List;

import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.ep.nodes.DataObjectTypeModelNode;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.NesCAttribute;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Flag;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DataObjectSpecifier.Specifier;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AttributeList;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusherFactory;

public class DataObjectDeclaration extends AbstractFixedASTNode implements TypeSpecifier{
	public static final Flag IN_DATA_OBJECT_DECLARATION = new Flag( "in DataObjectDeclaration" );
	
    public static final String SPECIFIER = "specifier";
    public static final String NAME = "name";
    public static final String ATTRIBUTES = "attributes";
    public static final String FIELDS = "fields";

    public DataObjectDeclaration(){
        super( "DataObjectDeclaration", SPECIFIER, NAME, ATTRIBUTES, FIELDS );
    }

    public DataObjectDeclaration( ASTNode specifier, ASTNode name, ASTNode attributes, ASTNode fields ){ 
        this();
        setField( SPECIFIER, specifier );
        setField( NAME, name );
        setField( ATTRIBUTES, attributes );
        setField( FIELDS, fields );
    }

    public DataObjectDeclaration( DataObjectSpecifier specifier, Identifier name, AttributeList attributes, DataObjectFieldDeclarationList fields ){
        this();
        setSpecifier( specifier );
        setName( name );
        setAttributes( attributes );
        setFields( fields );
    }

    public DataObjectType resolveType(){
        if( isResolved( "type" ))
            return resolved( "type" );

        DataObjectSpecifier specifierNode = getSpecifier();
        DataObjectFieldDeclarationList declarations = getFields();

        if( specifierNode == null )
            return resolved( "type", null );

        DataObjectSpecifier.Specifier specifier = specifierNode.getSpecifier();
        if( specifier == null )
            return resolved( "type", null );

        switch( specifier ){
            case NX_STRUCT:
                return resolved( "type", DataObjectType.nxstruct( getName(), declarations ));
            case STRUCT:
                return resolved( "type",  DataObjectType.struct( getName(), declarations ));
            case NX_UNION:
                return resolved( "type", DataObjectType.nxunion( getName(), declarations ));
            case UNION:
                return resolved( "type", DataObjectType.union( getName(), declarations ));
        }

        return resolved( "type", null );
    }

    public List<Field> resolveFields(){
        DataObjectFieldDeclarationList decl = getFields();
        if( decl == null )
            return null;
        return decl.resolveFields();
    }
    
    public NesCAttribute[] resolveAttributes(){
    	AttributeList list = getAttributes();
    	if( list == null )
    		return null;
    	return list.resolveAttributes();
    }
    
    public ModelAttribute[] resolveModelAttributes(){
    	AttributeList list = getAttributes();
    	if( list == null )
    		return null;
    	return list.resolveModelAttributes();
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        stack.push( FieldPusherFactory.STANDARD );
        stack.put( IN_DATA_OBJECT_DECLARATION );
        
        StandardModelNode node = null;
        
        stack.getDeclarationStack().push();
        
        if( stack.isCreateModel() ){
        	if( getComments() == null ){
        		Declaration declaration = stack.get( Declaration.DECLARATION );
        		if( declaration != null ){
        			setComments( declaration.getComments() );
        		}
        	}
        	
            Identifier name = getName();
            NodeStack nodes = stack.getNodeStack();
            nodes.pushNode( null );
            nodes.setRange( getRange() );
            
            super.resolve( stack );
            stack.checkCancellation();
            
            Type type = resolveType();
            if( type != null ){
            	if( stack.isCreateFullModel() || !stack.present( FunctionDefinition.IN_FUNCTION_BODY )){
            		node = new DataObjectTypeModelNode( name == null ? null : name.getName(), type );
            		node.setDocumentation( getComments() );
            		node.setAttributes( getAttributes() );
            		
            		nodes.include( node, this );
            		nodes.outline( node, 1 );
            		
            		if( name != null ){
            			nodes.addLocation( name );
            		}
            		nodes.addLocation( this );
            		
            		nodes.setNode( node );
            		nodes.addChild( node, 1, name );
            	}
            }
        }
        else{
            super.resolve( stack );
            stack.checkCancellation();
        }

        if( stack.isReportErrors() ){
            checkIncompleteTypes( stack );
        }

        stack.remove( IN_DATA_OBJECT_DECLARATION );
        stack.pop( getRight() );

        Identifier name = getName();
        Type type = resolveType();
        
        if( type != null ){
            if( node != null ){
                stack.putTypeTagModel( type.id( true ), node );
            }
            
            if( name != null ){
                stack.putTypeTag( name, type, resolveModelAttributes(), getRange().getRight() );
            }
            
            if( type.asDataObjectType() != null ){
            	stack.getDeclarationStack().set( type.id( false ) );
            	
            	if( stack.isCreateDeclarations() ){
            		Kind kind = null;
            		if( type.asDataObjectType().isUnion() )
            			kind = Kind.UNION;
            		else if( type.asDataObjectType().isStruct() )
            			kind = Kind.STRUCT;

            		if( kind != null && name != null ){
            			stack.getDeclarationStack().set( kind, type, name.getName(), DataObjectTypeModelNode.getTags( type ) );
            		}
            	}
            }
        }

        if( stack.isCreateModel() ){
            stack.getNodeStack().popNode( null );
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

        list.checkIncompleteTypes( stack, specifier.getSpecifier() == Specifier.NX_STRUCT || specifier.getSpecifier() == Specifier.STRUCT );
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
        return true;
    }

    public boolean isEnum() {
        return false;
    }

    public boolean isTypedefName() {
        return false;
    }
    
    public boolean isAttribute(){
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
            if( !( node instanceof AttributeList ))
                throw new ASTException( node, "Must be a NesCAttributeList" );
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
