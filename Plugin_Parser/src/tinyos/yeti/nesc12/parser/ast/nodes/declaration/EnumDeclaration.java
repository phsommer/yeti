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

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.EnumType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AttributeList;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

public class EnumDeclaration extends AbstractFixedASTNode implements TypeSpecifier {
	public static final String NAME = "name";
	public static final String ATTRIBUTES = "attributes";
	public static final String CONSTANTS = "constants";

	public EnumDeclaration(){
		super( "EnumDeclaration", NAME, ATTRIBUTES, CONSTANTS );
	}

	public EnumDeclaration( ASTNode name, ASTNode attributes, ASTNode constants ){
		this();
		setField( NAME, name );
		setField( ATTRIBUTES, attributes );
		setField( CONSTANTS, constants );
	}

	public EnumDeclaration( Identifier name, AttributeList attributes, EnumConstantList constants ){
		this();
		setName( name );
		setAttributes( attributes );
		setConstants( constants );
	}

	public boolean isStorageClass() {
		return false;
	}

	public boolean isSpecifier() {
		return true;
	}

	public boolean isDataObject() {
		return false;
	}

	public boolean isEnum() {
		return true;
	}

	public boolean isTypedefName() {
		return false;
	}

	public boolean isPrimitive() {
		return false;
	}

	public boolean isAttribute(){
		return false;
	}

	public ModelAttribute[] resolveAttributes(){
		AttributeList list = getAttributes();
		if( list == null )
			return null;
		return list.resolveModelAttributes();
	}
	
	public Type resolveType(){
		if( isResolved( "type" ))
			return resolved( "type" );

		Identifier name = getName();

		if( name == null ){
			return resolved( "type", new EnumType( null, resolveConstants() ));
		}
		else{
			return resolved( "type", new EnumType( name.getName(), resolveConstants() ));
		}
	}

	public String[] resolveConstants(){
		if( isResolved( "constants" ))
			return resolved( "constants" );

		EnumConstantList list = getConstants();
		if( list == null )
			return resolved( "constants", null );

		List<String> result = new ArrayList<String>();
		for( int i = 0, n = list.getChildrenCount(); i<n; i++ ){
			EnumConstant constant = list.getTypedChild( i );
			if( constant != null ){
				Identifier name = constant.getName();
				if( name != null ){
					result.add( name.getName() );
				}
			}
		}

		return resolved( "constants", result.toArray( new String[ result.size() ] ));
	}

	@Override
	public void resolve( AnalyzeStack stack ) {
		NodeStack nodes = stack.getNodeStack();
		if( stack.isCreateModel() ){
			nodes.pushNode( null );
			nodes.preventChildrenClose( 1 );
			nodes.setRange( getRange() );
		}

		stack.getDeclarationStack().push();

		super.resolve( stack );
		stack.checkCancellation();

		Identifier name = getName();
		Type type = null;
		String id = null;

		if( isIncomplete() ){
			if( name != null ){
				type = stack.getTypeTag( name.getName() );
				boolean resolved = false;

				if( type != null ){
					if( checkValidComplete( stack, type ) ){
						resolved( "type", type );
						resolved = true;
					}
					if( stack.isCreateReferences() ){
						ASTModelPath path = null;
						ModelNode node = stack.getTypeTagModel( type.id( false ) );
						if( node != null )
							path = node.getPath();
						if( path == null )
							path = stack.getTypeTagPath( name.getName() );
						stack.reference( this, path );
					}
				}

				if( !resolved ){
					type = resolveType();
					if( type != null ){
						stack.putTypeTag( name, type, resolveAttributes(), name.getRange().getRight() );
					}
				}
			}
		}
		else{
			type = resolveType();
			if( type != null ){
				id = type.id( false );
				stack.getDeclarationStack().set( id );
			}

			if( type != null && name != null ){
				stack.putTypeTag( name, type, resolveAttributes(), getRange().getRight() );
			}
		}

		if( isIncomplete() ){
			if( stack.isCreateModel() ){
				nodes.popNode( null );
			}
		}
		else{
			if( type == null )
				type = resolveType();

			if( type != null ){
				if( id == null )
					id = type.id( false );

				if( stack.isCreateModel() ){
					StandardModelNode node = new StandardModelNode( id, false, NesC12ASTModel.ENUMERATION, NesC12ASTModel.TYPE, Tag.IDENTIFIABLE );
					node.setDocumentation( getComments() );
					node.setAttributes( getAttributes() );
					if( name == null )
						node.setLabel( "enum" );
					else{
						node.setLabel( name.getName() );
						node.setNodeName( name.getName() );
					}

					if( name != null ){
						nodes.addLocation( name );
					}
					nodes.addLocation( this );

					nodes.include( node, this );
					nodes.outline( node, 1 );
					nodes.popNode( node );
					nodes.addChild( node, this );
					stack.putTypeTagModel( id, node );
				}
				
				if( stack.isCreateDeclarations() ){
					if( name != null ){
						stack.getDeclarationStack().set( Kind.ENUMERATION, type, name.getName(), TagSet.get( NesC12ASTModel.TYPE ) );
					}
				}
			}
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

	private boolean checkValidComplete( AnalyzeStack stack, Type type ){
		EnumType enumeration = type.asEnumType();
		if( enumeration == null ){
			if( stack.isReportErrors() ){
				stack.error( "existing tag '" + getName().getName() + "' is of wrong kind", getName() );
			}
			return false;
		}

		return true;
	}

	public boolean isIncomplete(){
		return getConstants() == null;
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

	public void setConstants( EnumConstantList constants ){
		setField( 2, constants );
	}

	public EnumConstantList getConstants(){
		return (EnumConstantList)getNoError( 2 );
	}

	@Override
	protected void checkField( int index, ASTNode node ) throws ASTException {
		if( index == 0 ){
			if( !(node instanceof Identifier )) 
				throw new ASTException( node, "Must be an Identifier" );
		}
		if( index == 1 ){
			if( !(node instanceof AttributeList ))
				throw new ASTException( node, "Must be a NesCAttributeList" );
		}
		if( index == 2 ){
			if( !(node instanceof EnumConstantList ))
				throw new ASTException( node, "Must be an EnumConstantList" );
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
