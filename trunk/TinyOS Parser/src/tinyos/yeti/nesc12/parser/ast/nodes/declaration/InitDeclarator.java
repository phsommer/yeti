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

import java.util.HashSet;
import java.util.Set;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.StandardModelConnection;
import tinyos.yeti.nesc12.ep.declarations.FieldDeclaration;
import tinyos.yeti.nesc12.ep.declarations.TypedDeclaration;
import tinyos.yeti.nesc12.ep.nodes.FieldModelConnection;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.ep.nodes.TypedefModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleField;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeUtility;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypedefType;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Flag;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Access;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Interface;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ASMCall;
import tinyos.yeti.nesc12.parser.ast.util.DeclarationStack;
import tinyos.yeti.nesc12.parser.ast.util.InitializerCounter;
import tinyos.yeti.nesc12.parser.ast.util.ModifierValidator;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.preprocessor.comment.IDocTag;
import tinyos.yeti.nesc12.parser.preprocessor.comment.NesCDocComment;
import tinyos.yeti.nesc12.parser.preprocessor.comment.ParamDocTag;

public class InitDeclarator extends AbstractFixedASTNode {
	/** if set, then a declarator redefines untyped fields. */
	public static final Flag REDECLARATION_OF_UNTYPED_FIELDS = new Flag( "init declarator redeclars untyped fields" );

	/** if set, then the declarator stores typedefs */
	public static final Flag TYPEDEF = new Flag( "init declarator stores typedefs" );

	/** key for a set of modifiers */
	public static final Key<Modifiers> MODIFIERS = new Key<Modifiers>( "init declarator modifiers" );

	public static final String DECLARATOR = "declarator";
	public static final String ASM = "asm";
	public static final String INITIALIZER = "initializer";

	public InitDeclarator(){
		super( "InitDeclarator", DECLARATOR, ASM, INITIALIZER );
	}

	public InitDeclarator( ASTNode declarator, ASTNode asm, ASTNode initializer ){
		this();
		setField( DECLARATOR, declarator );
		setField( ASM, asm );
		setField( INITIALIZER, initializer );
	}

	public InitDeclarator( Declarator declarator, ASMCall asm, Initializer initializer ){
		this();
		setDeclarator( declarator );
		setASM( asm );
		setInitializer( initializer );
	}

	/**
	 * Tries to find out what type the variable of this {@link InitDeclarator}
	 * will have. 
	 * @param name the base type of the variable
	 * @return the type of the variable or <code>null</code>
	 */
	public Type resolveType( Type name, AnalyzeStack stack ){
		if( isResolved( "type" ))
			return resolved( "type" );

		Declarator decl = getDeclarator();
		if( decl == null )
			return resolved( "type", null );

		return resolved( "type", decl.resolveType( name, stack ) );
	}

	/**
	 * Tries to find out what name the variable of this {@link InitDeclarator}
	 * has.
	 * @return the name or <code>null</code>
	 */
	public Name resolveName(){
		if( isResolved( "name" ))
			return resolved( "name" );

		Declarator decl = getDeclarator();
		if( decl == null )
			return resolved( "name", null );

		return resolved( "name", decl.resolveName() );
	}
	
	public Field resolveField(){
		return resolved( "field" );
	}

	@Override
	public void resolve( AnalyzeStack stack ) {
		NodeStack nodes = stack.getNodeStack();
		if( stack.isCreateModel() ){
			nodes.pushNode( null );
			nodes.setRange( getRange() );
		}
		
		Declaration declaration = stack.get( Declaration.DECLARATION );
		if( declaration != null ){
			setComments( declaration.getComments() );
		}

		
		stack.getDeclarationStack().push();

		boolean redeclaration = stack.presentLevel( REDECLARATION_OF_UNTYPED_FIELDS );
		boolean typedef = stack.presentLevel( TYPEDEF );

		if( redeclaration )
			stack.remove( REDECLARATION_OF_UNTYPED_FIELDS );
		if( typedef )
			stack.remove( TYPEDEF );

		Field field = resolveDeclaration( stack, redeclaration, typedef );
		resolved( "field", field );
		resolve( 1, stack );
		resolveInitializer( stack, field, typedef );

		if( redeclaration ) 
			stack.put( REDECLARATION_OF_UNTYPED_FIELDS );
		if( typedef )
			stack.put( TYPEDEF );

		if( stack.isReportErrors() ){
			ModifierValidator checker = stack.get( ModifierValidator.MODIFIER_VALIDATOR );
			if( checker != null )
				checker.check( stack, null, this );
		}

		if( nodes != null ){
			if( stack.present( Declaration.SPECIFIERS_WITH_ERROR ) ){
				nodes.putErrorFlag();
			}

			if( stack.present( Declaration.SPECIFIERS_WITH_WARNING ) ){
				nodes.putWarningFlag();
			}
		}

		resolveComments( stack );
		if( stack.isCreateModel() )
			nodes.popNode( null );

		stack.getDeclarationStack().pop();
	}

	private Field resolveDeclaration( AnalyzeStack stack, boolean redeclaration, boolean typedef ){
		Declarator declarator = getDeclarator();
		Initializer initializer = getInitializer();

		Field field = null;

		ModelNode node = null;
		NodeStack nodes = stack.getNodeStack();

		if( declarator != null ){
			declarator.resolve( stack );
			stack.checkCancellation();

			Type base = stack.get( Initializer.BASE_TYPE );
			Type oldBase = base;
			if( base != null )
				base = declarator.resolveType( base, stack );

			Name name = declarator.resolveName();
			ModelAttribute[] attributes = declarator.resolveAttributes();

			if( name != null ){
				if( typedef ){
					if( base != null ){
						TypedefType typedefType = new TypedefType( name.toIdentifier(), base ); 
						base = typedefType;

						if( !stack.present( FunctionDefinition.IN_FUNCTION_BODY ) && stack.isCreateDeclarations() && stack.isGlobal() ){
							TypedDeclaration declaration = new TypedDeclaration(
									Kind.TYPEDEF, 
									base, 
									name.toIdentifier(),
									TypeUtility.toAstNodeLabel( base ),
									stack.getParseFile(),
									null,
									TagSet.get( NesC12ASTModel.TYPE, NesC12ASTModel.TYPEDEF ) );
							declaration.setFileRegion( stack.getRegion( name.getRange() ) );
							stack.getDeclarationStack().set( declaration );
						}
						else{
							stack.getDeclarationStack().set( name.toIdentifier() );
						}
						if( (!stack.present( FunctionDefinition.IN_FUNCTION_BODY ) && stack.isCreateModel()) || stack.isCreateFullModel() ){
							node = new TypedefModelNode( name, typedefType );
							node.setDocumentation( getComments() );
							node.setAttributes( attributes );

							nodes.include( node, this );
							nodes.outline( node, 1 );
							nodes.addLocation( name.getRange() );
							nodes.addLocation( this );
							nodes.addChild( node, 1, this );

							reference( nodes, oldBase );
						}

						stack.putTypedef( name, base, attributes, node );
					}
				}
				else{
					boolean put = true;
					if( redeclaration ){
						put = false;
						field = stack.getField( name );
						if( stack.isReportErrors() ){
							if( field == null ){
								stack.error( "declaration for parameter '" + name + "' put no so parameter", name.getRange() );
							}
							else {
								stack.error( "redefinition of '" + name + "'", name.getRange(),  field.getRange() );
							}
						}
					}
					else if( stack.isCreateModel() && !stack.present( FunctionDefinition.IN_FUNCTION )){
						Field predefined = stack.getFieldScope( name, 0 );
						if( predefined == null || !ConversionTable.instance().equals( predefined.getType(), base ) ){
							Modifiers modifiers = stack.get( MODIFIERS );

							FieldModelNode fieldNode = new FieldModelNode( name, attributes, modifiers, base, null );
							fieldNode.setDocumentation( getComments() );
							if( name != null ){
								fieldNode.addRegion( stack.getRegion( name.getRange() ) );
							}
							fieldNode.addRegion( stack.getRegion( this ) );

							nodes.include( fieldNode, this );
							nodes.outline( fieldNode, 1 );
							nodes.addLocation( fieldNode.getRange() );

							if( stack.isCreateFullModel() ){
								if( base != null ){
									nodes.addType( base );
								}
							}

							field = fieldNode;
							node = fieldNode;
							if( stack.present( Access.ACCESS_USES ))
								node.getTags().add( Tag.USES );
							if( stack.present( Access.ACCESS_PROVIDES ))
								node.getTags().add( Tag.PROVIDES );

							if( nodes.size() > 1 ){
								FieldModelConnection connection = new FieldModelConnection( fieldNode, this );
								nodes.setConnection( connection );
								nodes.addConnection( connection, 1 );
							}
						}
						else{
							field = createField( stack, base, name );
						}
					}
					else{
						field = createField( stack, base, name );
					}

					if( field != null ){
						FunctionDeclarator function = declarator.getFunction();

						if( function != null ){
							Name[] arguments = function.resolveArgumentNames( stack );
							field.setArgumentNames( arguments );
						}

						if( stack.isReportErrors() ){
							FunctionDefinition.checkTask( field, stack );
							Modifiers modifiers = stack.get( MODIFIERS );

							if( modifiers != null && stack.present( Interface.INTERFACE )){
								if( modifiers.isCommand() || modifiers.isEvent() ){
									if( declarator instanceof FunctionDeclarator ){
										((FunctionDeclarator)declarator).checkNotIncomplete( stack );
									}
								}
							}
							
							if( function != null ){
								checkParametersDocumented( field.getArgumentNames(), stack );
							}
						}

						if( base != null ){
							field.setType( base );

							String id = FieldModelNode.fieldId( field.getName(), field.getType() );
							if( stack.isGlobal() && stack.isCreateDeclarations() ){
								FieldDeclaration declaration = new FieldDeclaration(
										Kind.FIELD,
										field.getModifiers(),
										field.getType(),
										field.getName(),
										field.getAttributes(),
										field.getInitialValue(),
										stack.getParseFile(),
										null,
										TagSet.get( NesC12ASTModel.FIELD ) );

								stack.getDeclarationStack().set( declaration, id );
							}
							else{
								stack.getDeclarationStack().set( id );
							}
						}

						if( put ){
							if( stack.present( FunctionDefinition.IN_FUNCTION ) || stack.present( Interface.INTERFACE ))
								stack.putField( field, getRange().getRight(), false );
							else
								stack.putField( field, getRange().getRight(), initializer == null );
						}
					}
				}
			}
		}
		else{
			resolveError( 0, stack );
		}

		if( stack.isCreateModel() && node != null )
			nodes.setNode( node );

		return field;
	}
	
	private SimpleField createField( AnalyzeStack stack, Type base, Name name ){
		Modifiers modifiers = stack.get( MODIFIERS );
		
		DeclarationStack declarations = stack.getDeclarationStack();
		declarations.push( FieldModelNode.fieldId( name, base ) );
		SimpleField field = declarations.set( modifiers, base, name, null, null, null );
		declarations.pop();
		return field;
	}

	private void resolveInitializer( AnalyzeStack stack, Field field, boolean typedef ){
		Initializer initializer = getInitializer();

		if( initializer != null ){
			if( field == null || field.getType() == null || !stack.isReportErrors() ){
				initializer.resolve( stack );
				stack.checkCancellation();
			}
			else if( initializer.isAssignmentable() ){
				initializer.resolve( stack );
				stack.checkCancellation();
				
				if( field != null ){
					Value value = initializer.resolveValue();
					field.setInitialValue( value );
				
					if( stack.isReportErrors() && field.getType() != null ){
						Type valueType = initializer.resolveType();
						if( valueType != null ){
							InitializerCounter.checkScalarForNonScalar( field.getType(), valueType, value, initializer, stack );
						}
					}
				}
			}
			else{
				boolean nonStatic = stack.present( FunctionDefinition.IN_FUNCTION_BODY ) &&
				(field.getModifiers() == null || !field.getModifiers().isStatic() );

				InitializerCounter counter = new InitializerCounter( stack, !nonStatic, field.getType() );
				stack.put( Initializer.INITIALIZER_COUNTER, counter );
				initializer.resolve( stack );
				stack.remove( Initializer.INITIALIZER_COUNTER );
				stack.checkCancellation();
				field.setInitialValue( counter.result( initializer ) );
			}

			if( stack.isReportErrors() ){
				if( field != null && field.getType() != null ){
					if( field.getType().isIncomplete() ){
						stack.error( "can't initialize field '" + Name.toIdentifier( field.getName() ) + "' of incomplete type", field.getRange() );
					}
				}

				if( typedef ){
					if( field == null ){
						stack.error( "field with modifier 'typedef' must not be initialized", initializer );
					}
					else{
						stack.error( "field with modifier 'typedef' must not be initialized", field.getRange() );
					}
				}
			}
		}
		else{
			resolveError( 2, stack );
		}
	}
	
	private void checkParametersDocumented( Name[] names, AnalyzeStack stack ){
		if( names == null || names.length == 0 )
			return;
		
		NesCDocComment[] comments = getComments();
		if( comments == null || comments.length == 0 )
			return;
		
		NesCDocComment comment = comments[comments.length-1];
		if( comment == null )
			return;
		
		IDocTag[] tags = comment.getTags();
		
		Set<String> documented = new HashSet<String>();
		for( IDocTag tag : tags ){
			if( tag instanceof ParamDocTag ){
				String param = ((ParamDocTag)tag).getParameterName();
				if( param != null ){
					documented.add( param );
				}
			}
		}
		
		for( Name name : names ){
			if( name != null ){
				if( !documented.contains( name.toIdentifier() )){
					stack.warning( "Parameter '" + name.toIdentifier() + "' not documented", name.getRange() );
				}
			}
		}
	}

	private void reference( NodeStack nodes, Type base ){
		String baseId = base.id( true );
		ModelNode model = nodes.getAnalyzeStack().getTypeTagModel( baseId );

		if( model != null ){
			nodes.addReference( model, this );
		}
		else{
			StandardModelConnection connection = new StandardModelConnection( baseId, this );

			connection.setLabel( TypeUtility.toAstNodeLabel( base ) );

			TagSet tags = TagSet.get( NesC12ASTModel.TYPE, Tag.AST_CONNECTION_LABEL_RESOLVE );

			if( base.asTypedefType() != null ){
				tags.add( NesC12ASTModel.TYPEDEF );
			}
			else{
				if( base.asEnumType() != null ){
					tags.add( NesC12ASTModel.ENUMERATION );
				}

				DataObjectType data = base.asDataObjectType();
				if( data != null ){
					if( data.isStruct() )
						tags.add( Tag.STRUCT );
					else if( data.isUnion() )
						tags.add( Tag.UNION );
				}
			}

			connection.setTags( tags );

			connection.setReference( true );

			nodes.addConnection( connection );
		}
	}

	public Declarator getDeclarator(){
		return (Declarator)getNoError( 0 );
	}    

	public void setDeclarator( Declarator declarator ){
		setField( 0, declarator );
	}

	public ASMCall getASM(){
		return (ASMCall)getNoError( 1 );
	}

	public void setASM( ASMCall asm ){
		setField( 1, asm );
	}

	public Initializer getInitializer(){
		return (Initializer)getNoError( 2 );
	}

	public void setInitializer( Initializer initializer ){
		setField( 2, initializer );
	}

	@Override
	protected void checkField( int index, ASTNode node ) throws ASTException {
		if( index == 0 ){
			if( !(node instanceof Declarator ))
				throw new ASTException( node, "Must be a Declarator" );
		}

		if( index == 1 ){
			if( !(node instanceof ASMCall ))
				throw new ASTException( node, "Must be an ASMCall" );
		}

		if( index == 2 ){
			if( !(node instanceof Initializer ))
				throw new ASTException( node, "Must be an Initializer" );
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
