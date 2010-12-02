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
package tinyos.yeti.nesc12.parser.ast.nodes.definition;

import static tinyos.yeti.nesc12.parser.ast.elements.types.TypeUtility.function;
import static tinyos.yeti.nesc12.parser.ast.elements.types.TypeUtility.result;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.declarations.FieldDeclaration;
import tinyos.yeti.nesc12.ep.nodes.FieldModelConnection;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.ep.nodes.ModuleModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.elements.CombinedName;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterface;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.elements.NesCModule;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleField;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.types.FunctionType;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Flag;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclarationSpecifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.IdentifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ParameterDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Interface;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Module;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.LabeledStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.Statement;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;
import tinyos.yeti.nesc12.parser.ast.util.ModifierValidator;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusherFactory;
import tinyos.yeti.nesc12.parser.ast.util.validators.FunctionBodyValidator;
import tinyos.yeti.nesc12.parser.ast.util.validators.FunctionParameterValidator;

public class FunctionDefinition extends AbstractFixedASTNode implements ExternalDeclaration, NesCExternalDefinition{
	public static final Key<Type> RESULT_TYPE = new Key<Type>( "resulting type of function" );
	public static final Flag IN_FUNCTION = new Flag( "currently resolving function" );
	public static final Flag IN_FUNCTION_BODY = new Flag( "currently resolving body of function" );

    public static final String SPECIFIERS = "specifiers";
    public static final String DECLARATOR = "declarator";
    public static final String DECLARATIONS = "declarations";
    public static final String BODY = "body";
	
	public FunctionDefinition(){
		super( "FunctionDefinition", SPECIFIERS, DECLARATOR, DECLARATIONS, BODY );
	}

	public FunctionDefinition( DeclarationSpecifierList specifiers, Declarator declarator, DeclarationList declarations, CompoundStatement body ){
		this();
		setSpecifiers( specifiers );
		setDeclarator( declarator );
		setDeclarations( declarations );
		setBody( body );
	}

	public Type resolveResultType(){
		Type type = resolveType();
		if( type == null )
			return null;

		if( type.asFunctionType() == null )
			return null;

		return type.asFunctionType().getResult();
	}

	public Type resolveType(){
		return resolveType( null );
	}

	public Type resolveType( AnalyzeStack stack ){
		if( isResolved( "type" ))
			return resolved( "type" );

		DeclarationSpecifierList specifiers = getSpecifiers();
		Declarator declarator = getDeclarator();

		if( specifiers == null || declarator == null )
			return resolved( "type", null );

		Type base = specifiers.resolveType();
		if( base == null )
			return resolved( "type", null );

		return resolved( "type", declarator.resolveType( base, stack ) );
	}

	public Modifiers resolveModifiers(){
		if( isResolved( "modifiers" ))
			return resolved( "modifiers" );

		DeclarationSpecifierList specifiers = getSpecifiers();
		if( specifiers == null )
			return resolved( "modifiers", null );

		return resolved( "modifiers", specifiers.resolveModifiers() );
	}

	public Name resolveName(){
		if( isResolved( "name" ))
			return resolved( "name" );

		Declarator decl = getDeclarator();
		if( decl == null )
			return resolved( "name", null );
		return resolved( "name", decl.resolveName() );
	}
	
	public ModelAttribute[] resolveAttributes(){
		Declarator declarator = getDeclarator();
		if( declarator == null )
			return null;
		return declarator.resolveAttributes();
	}
	
	public FieldModelNode resolveNode(){
		return resolved( "node" );
	}
	
	private Field resolveField( AnalyzeStack stack ){
		if( isResolved( "field" ))
			return resolved( "field" );

		Name name = resolveName();
		if( name == null )
			return resolved( "field", null );

		Modifiers modifiers = resolveModifiers();
		Type type = resolveType();
		ModelAttribute[] attributes = resolveAttributes();
		
		SimpleField field = stack.getDeclarationStack().set( modifiers, type, name, attributes, null, null );
		Declarator declarator = getDeclarator();

		FunctionDeclarator function = declarator == null ? null : declarator.getFunction();

		if( function != null ){
			field.setArgumentNames( function.resolveArgumentNames( stack ));
		}
		if( declarator != null ){
			field.setIndices( declarator.resolveIndices() );
		}

		return resolved( "field", field );
	}

	public NesCInterfaceReference resolveInterface() {
		return resolved( "interface" );
	}
	
	private NesCInterfaceReference resolveInterface( AnalyzeStack stack ){
		if( isResolved( "interface" ))
			return resolved( "interface" );
		
		Field field = resolveField( stack );
		
		String[] names = Name.segments( field.getName() );
		if( names.length != 2 )
			return resolved( "interface", null );

		ModuleModelNode node = stack.get( Module.MODULE );
		if( node == null )
			return resolved( "interface", null );

		NesCModule module = node.resolve( stack.getBindingResolver() );
		if( module == null )
			return resolved( "interface", null );

		NesCInterfaceReference reference = module.getInterfaceReference( names[0] );
		return resolved( "interface", reference );
	}

	@Override
	public void resolve( AnalyzeStack stack ) {
		cleanResolved();

		stack.folding( this );

		boolean global = stack.isGlobal();
		stack.pushScope( FieldPusherFactory.STANDARD );

		LabeledStatement.LabelResolver labelResolver = null;
		if( stack.isReportErrors() ){
			labelResolver = new LabeledStatement.LabelResolver( stack );
			stack.put( LabeledStatement.LABEL_RESOLVER, labelResolver );
		}

		if( stack.isCreateModel() ){
			NodeStack nodes = stack.getNodeStack();
			nodes.pushNode( null );
			nodes.setRange( getRange() );
		}

		resolveComments( stack );
		
		stack.getDeclarationStack().push();

		stack.put( ParameterDeclaration.STORE_FIELDS );
		stack.put( IdentifierList.STORE_UNTYPED_FIELDS );
		stack.put( InitDeclarator.REDECLARATION_OF_UNTYPED_FIELDS );
		stack.put(  IN_FUNCTION );
		if( stack.isReportErrors() )
			stack.put( ModifierValidator.MODIFIER_VALIDATOR, new FunctionParameterValidator() );
		resolve( 0, stack );
		resolve( 1, stack );
		stack.put( RESULT_TYPE, resolveResultType() );
		resolve( 2, stack );
		createDefinitions( stack, global );
		stack.remove( ParameterDeclaration.STORE_FIELDS );
		stack.remove( IdentifierList.STORE_UNTYPED_FIELDS );
		stack.remove( InitDeclarator.REDECLARATION_OF_UNTYPED_FIELDS );
		stack.put( IN_FUNCTION_BODY );
		if( stack.isReportErrors() )
			stack.put( ModifierValidator.MODIFIER_VALIDATOR, new FunctionBodyValidator() );
		stack.put( CompoundStatement.NO_PUSH );
		resolve( 3, stack );
		stack.remove( CompoundStatement.NO_PUSH );
		stack.remove( IN_FUNCTION_BODY );
		stack.remove( IN_FUNCTION );
		stack.remove( RESULT_TYPE );
		
		if( stack.isReportErrors() ){
			Declarator declarator = getDeclarator();
			if( declarator instanceof FunctionDeclarator ){
				((FunctionDeclarator)declarator).checkNotIncomplete( stack );
			}

			stack.remove( LabeledStatement.LABEL_RESOLVER );
			labelResolver.finish();
			checkType( stack );
			checkControlFlow( stack );

			Name name = resolveName();
			if( name != null ){
				if( stack.present( Interface.INTERFACE )){
					stack.error( "non forward declaration of function '"+ name.toIdentifier() +"' in interface", name.getRange() );
				}
			}
		} 

		if( stack.isCreateModel() ){
			stack.getNodeStack().popNode( null );
		}
		
		if( stack.isCreateReferences() ){
			createReferences( stack );
		}

		stack.getDeclarationStack().pop();

		stack.popScope( getRight() );
	}

	private void checkType( AnalyzeStack stack ){
		DeclarationSpecifierList list = getSpecifiers();
		if( list == null )
			return;

		list.checkResolvesType( stack );

		ModifierValidator checker = stack.get( ModifierValidator.MODIFIER_VALIDATOR );
		if( checker != null ){
			checker.check( stack, this );
		}

		Type type = resolveType();
		if( type == null )
			return;

		Name name = resolveName();
		if( name == null )
			return;

		if( type.asFunctionType() == null ){
			stack.error( "function '" + name.toIdentifier() + "' has non function type", getDeclarator() );
		}
	}

	private void checkControlFlow( AnalyzeStack stack ){
		Statement body = getBody();
		if( body == null )
			return;

		/*List<Statement> unreachable = flow.checkNonReachableStatements();
        if( unreachable.size() > 0 ){
            // sort by depth
            Collections.sort( unreachable, new Comparator<ASTNode>(){
                public int compare( ASTNode a, ASTNode b ){
                    if( a.isAncestor( b ))
                        return 1;

                    if( b.isAncestor( a ))
                        return -1;

                    return 0;
                }
            });

            // collect elements
            List<Statement> roots = new ArrayList<Statement>();
            loop: for( Statement next : unreachable ){
                for( Statement parent : roots ){
                    if( next.isAncestor( parent ))
                        continue loop;
                }

                roots.add( next );
            }

            // report
            for( Statement statement : roots ){
                stack.error( "unreachable statement", statement );
            }
        }*/

		Name name = resolveName();
		if( name == null )
			return;

		Type result = result( function( resolveType() ) );
		if( BaseType.VOID != result ){
			ControlFlow flow = new ControlFlow( body );
			if( !flow.checkReturnsAlways() ){
				if( flow.checkReturnsSometimes() ){
					stack.warning( "might be missing a return statement", name.getRange() );
				}
				else{
					stack.error( "missing return statement", name.getRange() );
				}
			}
		}
	}

	private void createDefinitions( AnalyzeStack stack, boolean global ){
		Type type = resolveType( stack );
		Name name = resolveName();
		if( type != null && name != null ){
			Modifiers modifiers = resolveModifiers();
			Field field = null;
			String id = null;

			if( stack.isCreateModel() ){
				Field predefined = stack.getFieldScope( name, 1 );

				boolean newNode = false;
				boolean replaceNode = false;

				if( predefined == null ){
					newNode = true;
				}
				else if( predefined.asNode() == null || predefined.asNode().getTags().contains( NesC12ASTModel.COMPLETE_FUNCTION )){
					newNode = true;
				}
				else if( !ConversionTable.instance().equals( predefined.getType(), type )){
					newNode = true;
				}
				else if( !predefined.asNode().getTags().contains( NesC12ASTModel.COMPLETE_FUNCTION )){
					replaceNode = true;
				}

				if( newNode || replaceNode ){
					FieldModelNode node = FieldModelNode.toNode( resolveField( stack ) );
					resolved( "node", node );

					if( node != null ){
						id = node.getIdentifier();
						node.getTags().add( NesC12ASTModel.COMPLETE_FUNCTION );
						node.setDocumentation( getComments() );
						
						if( stack.isReportErrors() ){
							checkInterfaceTemplate( node, stack );
							checkFieldTemplate( node, stack );
							checkTask( node, stack );
						}

						NodeStack nodes = stack.getNodeStack();
						nodes.include( node, this );
						nodes.outline( node, 1 );
						FieldModelConnection connection = new FieldModelConnection( node, this );
						connection.setReference( false );
						nodes.setOverride( replaceNode );
						if( replaceNode ){
							nodes.removeChild( node.getIdentifier(), 1 );
						}
						nodes.addConnection( connection, 1 );
						nodes.setNode( node );
						nodes.addLocation( name.getRange() );
						nodes.addLocation( this );

						field = node;
					}
				}
			}

			if( field == null ){
				field = resolveField( stack );
			}

			if( field != null ){
				stack.putField( field, name.getRange().getRight(), false, 1 );
			}

			if( type.asFunctionType() != null ){
				if( id == null ){
					id = FieldModelNode.fieldId( name, type );
				}

				if( stack.isCreateDeclarations() && global ){
					FieldDeclaration declaration = new FieldDeclaration(
							Kind.FUNCTION,
							modifiers,
							type,
							name,
							null,
							null,
							stack.getParseFile(),
							null,
							TagSet.get( Tag.FUNCTION )
					);

					stack.getDeclarationStack().set( declaration, id );
				}
				else{
					stack.getDeclarationStack().set( id );
				}
			}
		}
	}
	
	private void createReferences( AnalyzeStack stack ){
		Field field = resolveField( stack );
		if( field == null )
			return;
		
		Name name = field.getName();
		if( !(name instanceof CombinedName ))
			return;
		
		if( ((CombinedName)name).getNameCount() != 2 )
			return;
		
		NesCInterfaceReference reference = resolveInterface( stack );
		if( reference == null )
			return;
		
		
		NesCInterface interfaze = reference.getRawReference();
		if( interfaze != null ){
			stack.reference( ((CombinedName)name).getName( 0 ).getRange(), interfaze.getNode().getPath() );
		
			Field referenced = interfaze.getField( ((CombinedName)name).getName( 1 ).toIdentifier() );
			if( referenced != null ){
				stack.reference( ((CombinedName)name).getName( 1 ).getRange(), referenced.getPath() );
			}
		}
	}

	/**
	 * If this field has a name of the form "x.y", then this method searches
	 * for the interface which is implemented by this field and checks the
	 * correctness of the types, events and commands
	 * @param field this field
	 * @param stack the stack used for analysis
	 */
	private void checkInterfaceTemplate( Field field, AnalyzeStack stack ){
		String[] name = Name.segments( field.getName() );
		if( name.length != 2 )
			return;

		if( field.getType() == null ){
			return;
		}

		if( field.getType().asFunctionType() == null ){
			stack.error( "type must be a function type", getDeclarator() );
		}

		Name[] names = new Name[2];
		if( field.getName() instanceof CombinedName ){
			names[0] = ((CombinedName)field.getName()).getName( 0 );
			names[1] = ((CombinedName)field.getName()).getName( 1 );
		}
		else{
			return;
		}

		NesCInterfaceReference reference = resolveInterface( stack );
		if( reference == null ){
			stack.error( "interface '" + name[0] + "' not found", names[0].getRange() );
			return;
		}

		NesCInterface interfaze = reference.getParameterizedReference();
		if( interfaze == null ){
			return;
		}

		Field template = interfaze.getField( name[1] );
		if( template == null ){
			stack.error( "field '" + name[1] + "' not declared in '" + name[0] + "'", names[1].getRange() );
			return;
		}

		if( template.getType() == null || template.getType().asFunctionType() == null ){
			return;
		}

		Modifiers templateModifiers = template.getModifiers();
		if( templateModifiers == null || (templateModifiers.isCommand() && templateModifiers.isEvent() )){
			return;
		}

		// check type
		if( !field.getType().equals( template.getType() )){
			stack.error( "type must equal '" + template.getType().toLabel( null, Type.Label.SMALL ) + "'", field.getName().getRange() );
		}

		// check indices
		Field[] expected = reference.getIndices();
		Field[] found = field.getIndices();

		int expectedCount = expected == null ? 0 : expected.length;
		int foundCount = found == null ? 0 : found.length;
		for( int i = 0, n = Math.min( expectedCount, foundCount ); i<n; i++ ){
			if( found[i] != null && expected[i] != null ){
				if( found[i].getType() != null && expected[i].getType() != null ){
					if( !ConversionTable.instance().equals( found[i].getType(), expected[i].getType() ) ){
						stack.error( "Expected type '" + expected[i].getType() + "' but found '" + found[i].getType() + "'", found[i].getRange() );
					}
				}
			}
		}

		for( int i = foundCount; i < expectedCount; i++ ){
			if( expected[i] != null ){
				stack.error( "Missing index nr. " + (i+1)  + ": '" + expected[i].getDeclaration( null ) + "'", field.getRange() );
			}
		}

		for( int i = expectedCount; i < foundCount; i++ ){
			if( found[i] != null ){
				stack.error( "Index not required", found[i].getRange() );
			}
		}

		// check modifiers
		Modifiers modifiers = field.getModifiers();
		if( (modifiers == null || !modifiers.isEvent()) && templateModifiers.isEvent()){
			stack.error( "must be an 'event'", field.getRange() );
		}
		if( (modifiers == null || !modifiers.isCommand()) && templateModifiers.isCommand()){
			stack.error( "must be a 'command'", field.getRange() );
		}
		if( (modifiers == null || !modifiers.isAsync()) && templateModifiers.isAsync() ){
			stack.error( "must be 'async'", field.getRange() );
		}
		else if( modifiers != null && modifiers.isAsync() && !templateModifiers.isAsync() ){
			stack.error( "must not be 'async'", field.getRange() );
		}

		if( modifiers != null && !modifiers.isDefault() ){
			if( templateModifiers.isEvent() && reference.isProvided() ){
				stack.warning( "since '" + reference.getName().toIdentifier() + "' is provided, there is no need to implement its events", names[1].getRange() );
			}
			if( templateModifiers.isCommand() && reference.isUsed() ){
				stack.warning( "since '" + reference.getName().toIdentifier() + "' is used, there is no need to implement its commands", names[1].getRange() );
			}
		}
	}

	/**
	 * If this field is a task, then ensures that the result is void.
	 * @param field this field
	 * @param stack the stack used for analysis
	 */
	public static void checkTask( Field field, AnalyzeStack stack ){
		Modifiers modifiers = field.getModifiers();
		if( modifiers == null || !modifiers.isTask() )
			return;

		FunctionType function = function( field.getType() );

		if( function == null )
			return;

		if( function.getArgumentCount() > 0 ){
			stack.error( "task must not have parameters", field.getRange() );
		}

		Type type = result( function );
		if( type == null )
			return;

		if( type.asBase() != BaseType.VOID ){
			stack.error( "every task must return 'void'", field.getRange() );
		}
	}

	/**
	 * If this field has a name of the form "x", then this method searches
	 * for the command or event which is implemented by this field and checks the
	 * correctness of the types
	 * @param field this field
	 * @param stack the stack used for analysis
	 */
	private void checkFieldTemplate( Field field, AnalyzeStack stack ){
		String[] name = Name.segments( field.getName() );
		if( name == null || name.length != 1 )
			return;

		if( field.getType() == null ){
			return;
		}

		ModuleModelNode node = stack.get( Module.MODULE );
		if( node == null )
			return;

		NesCModule module = node.resolve( stack.getBindingResolver() );

		Modifiers modifiers = field.getModifiers();
		Field template = module.getProvidesFunction( name[0] );
		if( template != null ){
			// must be a command
			if( modifiers == null || !modifiers.isCommand() ){
				stack.error( "must be a command", field.getRange() );
			}
		}
		else{
			template = module.getUsesFunction( name[0] );
			if( template != null ){
				// must be an event
				if( modifiers == null || !modifiers.isEvent() ){
					stack.error( "must be an event", field.getRange() );
				}
			}
		}

		if( template == null ){
			if( modifiers != null ){
				if( modifiers.isEvent() ){
					stack.warning( "no event '" + field.getName() + "' declared in uses list", field.getName().getRange() );
				}
				if( modifiers.isCommand() ){
					stack.warning( "no command '" + field.getName() + "' declared in provides list", field.getName().getRange() );
				}
			}

			return;
		}

		if( template.getType() == null )
			return;

		if( !template.getType().equals( field.getType() ))
			stack.error( "must be of type '" + template.getType().toLabel( null, Type.Label.SMALL ) + "'", field.getName().getRange() );
	}
	
	@Override
	public Range getCommentAnchor(){
	 	Declarator declarator = getDeclarator();
	 	if( declarator == null )
	 		return null;
	 	
	 	FunctionDeclarator function = declarator.getFunction();
	 	if( function == null )
	 		return null;
	 	
	 	declarator = function.getDeclarator();
	 	if( declarator == null )
	 		return null;
	 	
	 	return declarator.getRange();
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

	public void setDeclarations( DeclarationList declarations ){
		setField( 2, declarations );
	}

	public DeclarationList getDeclarations(){
		return (DeclarationList)getNoError( 2 );
	}

	public void setBody( CompoundStatement body ){
		setField( 3, body );
	}

	public CompoundStatement getBody(){
		return (CompoundStatement)getNoError( 3 );
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
		if( index == 2 ) {
			if( !( node instanceof DeclarationList ) )
				throw new ASTException( node, "Must be a DeclarationList" );
		}
		if( index == 3 ) {
			if( !( node instanceof CompoundStatement ) )
				throw new ASTException( node, "Must be a CompoundStatement" );
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
