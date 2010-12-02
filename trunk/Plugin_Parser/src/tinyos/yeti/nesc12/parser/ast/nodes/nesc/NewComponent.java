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
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.declarations.IgnoreDeclaration;
import tinyos.yeti.nesc12.ep.nodes.ComponentModelNode;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Generic;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCComponent;
import tinyos.yeti.nesc12.parser.ast.elements.NesCGenericComponent;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionMap;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

public class NewComponent extends AbstractFixedASTNode implements Component{
    public static final String NAME = "name";
    public static final String ARGUMENTS = "arguments";
    public static final String RENAME = "rename";
    
    public NewComponent(){
        super( "NewComponent", NAME, ARGUMENTS, RENAME );
    }
    
    public NewComponent( ASTNode name, ASTNode arguments, ASTNode rename ){
        this();
        setField( NAME, name );
        setField( ARGUMENTS, arguments );
        setField( RENAME, rename );
    }
    
    public NewComponent( Identifier name, GenericArgumentList arguments, Identifier rename ){
        this();
        setName( name );
        setArguments( arguments );
        setRename( rename );
    }
    
    /**
     * Tries to find the arguments of this new component.
     * @return an array containing the arguments, can contain <code>null</code> for
     * erroneous arguments. Is <code>null</code> if no arguments were
     * specified at all.
     */
    public Generic[] resolveArguments(){
        if( isResolved( "arguments" ))
            return resolved( "arguments" );
        
        GenericArgumentList list = getArguments();
        if( list == null )
            return resolved( "arguments", null );
        
        return resolved( "arguments", list.resolveGenericArguments() );
    }
    
    public ModelConnection resolveConnection() {
    	return resolved( "model" );
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        if( stack.isCreateModel() ){
        	NodeStack nodes = stack.getNodeStack();
            nodes.pushNode( null );
            nodes.setRange( getRange() );
        }
        
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isCreateModel() ){
        	Identifier name = getName();
            if( name != null ){
                Identifier rename = getRename();

                ComponentReferenceModelConnection ref = new ComponentReferenceModelConnection( 
                        name.getName(), rename == null ? null : rename.getName(), name,
                                true, resolveArguments() );

                stack.getNodeStack().setConnection( ref );
                stack.getNodeStack().addConnection( ref, 1 );
                stack.getNodeStack().addLocation( name );
                stack.getNodeStack().addLocation( this );
                resolved( "model", ref );
                
                Integer implementationStart = stack.get( ConfigurationDeclarationList.IMPLEMENTATION_START );
                if( implementationStart != null ){
                	stack.putComponentReference( ref, implementationStart );
                }
                
                stack.reference( this, ref );
                
                if( stack.isReportErrors() ){
                    checkValidity( ref, stack );
                }
            }
        }
        
        if( stack.isCreateModel() ){
            stack.getNodeStack().popNode( null );
        }
    }
    
    private void checkValidity( ComponentReferenceModelConnection reference, AnalyzeStack stack ){
    	// check existence
    	IDeclaration declaration = stack.resolveDeclaration( getName().getName(), true, Kind.MODULE, Kind.BINARY_COMPONENT, Kind.CONFIGURATION );

        if( declaration == null ){
            stack.error( "missing declaration for component '" + reference.getReference() + "'", getName() );
            return;
        }
        else if( declaration instanceof IgnoreDeclaration ){
            // ignore this, there are no more information but it is correct anyway
            return;
        }
        
        ModelNode node = stack.getDeclarationResolver().resolve( declaration, stack.requestProgressMonitor() );
        stack.checkCancellation();
        
        if( !(node instanceof ComponentModelNode )){
            stack.error( "missing declaration for component '" + reference.getReference() + "'", getName() );
            return;
        }
        
        NesCComponent component = ((ComponentModelNode)node).resolve( stack.getBindingResolver() );
        if( component == null ){
            return;
        }
        if( !(component instanceof NesCGenericComponent )){
            stack.error( "component '" + getName().getName() + "' is not generic", getName() );
            return;
        }
        
        NesCGenericComponent genericComponent = (NesCGenericComponent)component;
        if( !genericComponent.isGeneric() ){
            stack.error( "component '" + getName().getName() + "' is not generic", getName() );
            return;
        }
        
        // check arguments and parameters
        int parameterCount = genericComponent.getParameterCount();
        Generic[] arguments = resolveArguments();
        if( arguments == null )
            arguments = new Generic[]{};
        int argumentCount = arguments.length;
        
        if( parameterCount != argumentCount ){
            stack.error( "generic component '" + getName().getName() + "' requires " + parameterCount + " parameters, not " + argumentCount, getName() );
        }
        
        GenericArgumentList list = getArguments();
        
        for( int i = 0, n = Math.min( parameterCount, argumentCount ); i<n; i++ ){
            Binding parameter = genericComponent.getParameter( i );
            Generic argument = arguments[i];
            
            if( argument != null ){
                if( parameter instanceof Type ){
                    if( argument.asType() == null ){
                        stack.error( "require a type as argument", list.getChild( i ));
                    }
                }
                else if( parameter instanceof Field ){
                    if( argument.asType() != null ){
                        stack.error( "require a constant value as argument, not a type", list.getChild( i ) );
                    }
                    else if( argument.asValue() == null ){
                        stack.error( "require a constant value as argument", list.getChild( i ) );
                    }
                    else{
                        Field field = (Field)parameter;
                        Value value = argument.asValue();
                        if( field.getType() != null && value.getType() != null ){
                            ConversionTable.instance().check(
                                    value.getType(), field.getType(),
                                    ConversionMap.assignment( stack, list.getChild( i ), value ) );
                        }
                    }
                }
            }
        }
        for( int i = argumentCount; i < parameterCount; i++ ){
            Binding parameter = genericComponent.getParameter( i );
            if( parameter instanceof Type ){
                Type type = (Type)parameter;
                stack.error( "missing argument type: '" + type.toLabel( null, Type.Label.EXTENDED ) + "'", getName() );
            }
            else if( parameter instanceof Field ){
                Field field = (Field)parameter;
                if( field.getType() == null )
                    stack.error( "missing argument: '" + field.getName() + "'", getName() );
                else
                    stack.error( "missing argument: '" + field.getType().toLabel( Name.toIdentifier( field.getName() ), Type.Label.EXTENDED ) + "'", getName() );
            }
        }
    }
    
    public String getFinalName() {
    	Identifier name = getFinalNameNode();
    	if( name != null )
    		return name.getName();
    	return null;
    }
    
    public Identifier getFinalNameNode() {
    	Identifier name = getRename();
    	if( name != null )
    		return name;
    	return getName();
    }
    
    public void setName( Identifier name ){
        setField( 0, name );
    }
    public Identifier getName(){
        return (Identifier)getNoError( 0 );
    }
    
    public void setArguments( GenericArgumentList arguments ){
        setField( 1, arguments );
    }
    public GenericArgumentList getArguments(){
        return (GenericArgumentList)getNoError( 1 );
    }
    
    public void setRename( Identifier rename ){
        setField( 2, rename );
    }
    public Identifier getRename(){
        return (Identifier)getNoError( 2 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 || index == 2 ) {
            if( !( node instanceof Identifier ) )
                throw new ASTException( node, "Must be an Identifier" );
        }
        if( index == 1 ) {
            if( !( node instanceof GenericArgumentList ) )
                throw new ASTException( node, "Must be a GenericArgumentList" );
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
