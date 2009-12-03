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
package tinyos.yeti.nesc12.ep.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import tinyos.yeti.Debug;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.reference.ASTReference;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Generic;
import tinyos.yeti.nesc12.parser.ast.elements.LazyRangeDescription;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.elements.NesCWire;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.Type.Label;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionMap;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Connection;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Endpoint;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Wire;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Wire.Direction;

public class ConnectionModelNode extends StandardModelNode{
    public static final IGenericFactory<ConnectionModelNode> FACTORY = new ReferenceFactory<ConnectionModelNode>( StandardModelNode.FACTORY ){
        public ConnectionModelNode create(){
            return new ConnectionModelNode();
        }
        @Override
        public void write( ConnectionModelNode value, IStorage storage ) throws IOException{
            super.write( value, storage );
            storage.out().writeBoolean( value.implicit );

            if( value.direction == null ){
                storage.out().writeInt( -1 );
            }
            else{
                switch( value.direction ){
                    case ASSIGN:
                        storage.out().writeInt( 0 );
                        break;
                    case LEFT_TO_RIGHT:
                        storage.out().writeInt( 1 );
                        break;
                    case RIGHT_TO_LEFT:
                        storage.out().writeInt( 2 );
                        break;
                }
            }
        }

        @Override
        public ConnectionModelNode read( ConnectionModelNode value, IStorage storage ) throws IOException{
            super.read( value, storage );
            value.implicit = storage.in().readBoolean();

            int kind = storage.in().readInt();
            switch( kind ){
                case 0:
                    value.direction = Wire.Direction.ASSIGN;
                    break;
                case 1:
                    value.direction = Wire.Direction.LEFT_TO_RIGHT;
                    break;
                case 2:
                    value.direction = Wire.Direction.RIGHT_TO_LEFT;
                    break;
                case -1:
                    break;
                default:
                    throw new IOException( "unknown kind of wire: " + kind );
            }

            return value;
        }
    };

    private Wire.Direction direction;
    private boolean implicit;

    protected ConnectionModelNode(){
        // nothing
    }

    /**
     * Creates a new model node
     * @param identifier the unique name
     */
    public ConnectionModelNode( String identifier ){
        super( identifier, false, Tag.CONNECTION, Tag.NO_BASE_EXPANSION );
    }

    public Wire.Direction getDirection() {
        return direction;
    }

    public boolean isImplicit(){
        return implicit;
    }

    public NesCWire resolve( BindingResolver bindings ){
        Binding result = bindings.getBinding( getPath(), getIdentifier() );
        if( result == null ){
            result = new NesCWire( this, bindings );
            bindings.putBinding( getPath(), getIdentifier(), result );
        }

        if( result instanceof NesCWire )
            return (NesCWire)result;

        return null;
    }

    public EndpointModelConnection getLeft(){
        if( getConnectionCount() != 2 )
            return null;

        return (EndpointModelConnection)getConnection( 0 );
    }

    public EndpointModelConnection getRight(){
        if( getConnectionCount() != 2 )
            return null;

        return (EndpointModelConnection)getConnection( 1 );
    }

    /**
     * Resolves this node and finds the children of it. Reports errors
     * if <code>stack</code> is not <code>null</code>
     * @param configuration the configuration in which this connection lies
     * @param connection the connection itself
     * @param stack used to report errors
     */
    public void resolve( ConfigurationModelNode configuration, Connection connection, AnalyzeStack stack ){        
        Endpoint left = connection.getLeftEndpoint();
        Endpoint right = connection.getRightEndpoint();

        if( left == null || right == null )
            return;

        EndpointResolver leftResolver = new EndpointResolver( stack, left );
        EndpointResolver rightResolver = new EndpointResolver( stack, right );

        Wire wire = connection.getWire();
        if( wire != null ){
            direction = wire.getDirection();
        }

        if( direction == null ){
            setLabel( leftResolver.getLabel() + " ? " + rightResolver.getLabel() );
        }
        else{
            switch( direction ){
                case ASSIGN:
                    setLabel( leftResolver.getLabel() + " = " + rightResolver.getLabel() );
                    getTags().add( Tag.CONNECTION_BOTH );
                    break;
                case LEFT_TO_RIGHT:
                    setLabel( leftResolver.getLabel() + " -> " + rightResolver.getLabel() );
                    getTags().add( Tag.CONNECTION_RIGHT );
                    break;
                case RIGHT_TO_LEFT:
                    setLabel( leftResolver.getLabel() + " <- " + rightResolver.getLabel() );
                    getTags().add( Tag.CONNECTION_LEFT );
                    break;
            }
        }

        boolean leftGuessed = leftResolver.guessMeaning( configuration );
        boolean rightGuessed = rightResolver.guessMeaning( configuration );
        
        leftResolver.reference();
        rightResolver.reference();
        
        if( !leftGuessed || !rightGuessed )
            return;

        if( leftResolver.isImplicit() && rightResolver.isImplicit() ){
            if( stack != null && stack.isReportErrors() ){
                stack.error( "can't have two implicit endpoints", connection );
            }
            return;
        }

        boolean leftWasImplicit = false;
        boolean rightWasImplicit = false;

        if( leftResolver.isImplicit() ){
            leftWasImplicit = true;
            implicit = true;
            boolean shouldBeUsed = wire != null && wire.getDirection() == Direction.LEFT_TO_RIGHT;
            boolean shouldBeProvided = wire != null && wire.getDirection() == Direction.RIGHT_TO_LEFT;

            if( !leftResolver.resolveImplicit( rightResolver, connection, shouldBeUsed, shouldBeProvided ) )
                return;
        }
        else if( rightResolver.isImplicit() ){
            rightWasImplicit = true;
            implicit = true;
            boolean shouldBeUsed = wire != null && wire.getDirection() == Direction.RIGHT_TO_LEFT;
            boolean shouldBeProvided = wire != null && wire.getDirection() == Direction.LEFT_TO_RIGHT;

            if( !rightResolver.resolveImplicit( leftResolver, connection, shouldBeUsed, shouldBeProvided ) )
                return;
        }

        // now both resolver point to valid constructs (which might be used false or have wrong types)
        leftResolver.copyConnection();
        rightResolver.copyConnection();

        if( stack.isReportErrors() ){
            // create a message telling the user what is connected to what
            wireMessage( leftResolver, rightResolver, connection, stack );

            // check types
            MissingIndexCollection missing = new MissingIndexCollection();
            missing.setSide( true );
            leftResolver.checkIndices( stack, missing );
            missing.setSide( false );
            rightResolver.checkIndices( stack, missing );
            missing.check( stack );

            if( leftResolver.interfaze != null && rightResolver.interfaze != null ){
                if( checkSameInterfaces( leftResolver.interfaze, rightResolver.interfaze, stack, connection )){
                	List<Type> mismatches = new ArrayList<Type>();
                	Match match = typesMatch( 
                            leftResolver.interfaze, leftResolver.getReplacements(),
                            rightResolver.interfaze, rightResolver.getReplacements(),
                            mismatches );
                	
                    if( Match.FULL_MATCH != match ){
                        if( leftWasImplicit )
                            stack.error( reportWrongTypes( leftResolver.interfaze, leftResolver.getReplacements() ), connection );
                        else if( rightWasImplicit )
                            stack.error( reportWrongTypes( rightResolver.interfaze, rightResolver.getReplacements() ), connection );
                        else if( leftResolver.isInternal() ^ rightResolver.isInternal() ){
                            if( leftResolver.isInternal() )
                                stack.error( reportWrongTypes( rightResolver.interfaze, rightResolver.getReplacements() ), connection );
                            else
                                stack.error( reportWrongTypes( leftResolver.interfaze, leftResolver.getReplacements() ), connection );    
                        }
                        else{
                        	StringBuilder message = new StringBuilder();
                        	if( match == Match.NO_MATCH ){
                        		message.append( "the generic parameters of the interfaces do not match" );
                        	}
                        	else{
                        		message.append( "the generic parameters of the interfaces are suspicious: they have the same declaration, but different names" );
                        	}
                        	
                        	if( mismatches.size() > 0 ){
                        		message.append( ": " );
                        		for( int i = 0, n = mismatches.size(); i<n; i += 2 ){
                        			if( i > 0 ){
                        				message.append( ", " );
                        			}
                        			
                        			message.append( "\'" );
                        			message.append( mismatches.get( i ).toLabel( null, Label.SMALL ));
                        			message.append( "\'" );
                        			
                        			message.append( " vs. \'" );
                        			message.append( mismatches.get( i+1 ).toLabel( null, Label.SMALL ));
                        			message.append( "\'" );
                        		}
                        	}
                        	
                            stack.error( message.toString(), connection );
                        }
                    }
                }
            }
            else if( leftResolver.function != null && rightResolver.function != null ){
                if( !ConversionTable.instance().equals( leftResolver.function.getType(), rightResolver.function.getType(), false ) ){
                    stack.error( "incompatible types", connection );
                }
            }
            else if( (leftResolver.function != null && rightResolver.interfaze != null) || 
                    (leftResolver.interfaze != null && rightResolver.function != null )){
                stack.error( "cannot wire interface with function", connection );
            }
            else{
                stack.error( "can't resolve connection", connection );
            }

            // check usage
            if( wire != null ){
                // they might however not be in the correct uses/provides order
                switch( wire.getDirection() ){
                    case LEFT_TO_RIGHT:
                        if( !leftResolver.used ){
                            stack.error( "in a wiring like '->', the left side must be 'used'", connection.getLeftEndpoint() );
                        }
                        if( !rightResolver.provided ){
                            stack.error( "in a wiring like '->', the right side must be 'provided'", connection.getRightEndpoint() );
                        }
                        break;
                    case RIGHT_TO_LEFT:
                        if( !leftResolver.provided ){
                            stack.error( "in a wiring like '<-', the left side must be 'provided'", connection.getLeftEndpoint() );
                        }
                        if( !rightResolver.used ){
                            stack.error( "in a wiring like '<-', the right side must be 'used'", connection.getRightEndpoint() );
                        }
                        break;
                    case ASSIGN:
                        if( leftResolver.isInternal() ^ rightResolver.isInternal() ){
                            if( leftResolver.used != rightResolver.used ||
                                    leftResolver.provided != rightResolver.provided ){
                                stack.error( 
                                        "in a wiring like '=', when one side is internal and the other external, then either both sides must be 'used' or 'provided'",
                                        connection );
                            }
                        }
                        else if( !leftResolver.isInternal() && !leftResolver.isInternal() ){
                            if( leftResolver.used == rightResolver.used ||
                                    leftResolver.provided == rightResolver.provided ){
                                stack.error( 
                                        "in a wiring like '=', when both sides are external, then one side must be 'used' and the other 'provided'",
                                        connection );
                            }
                        }
                        else if( leftResolver.isInternal() && rightResolver.isInternal() ){
                            if( leftResolver.used == rightResolver.used ||
                                    leftResolver.provided == rightResolver.provided ){
                                stack.error( 
                                        "in a wiring like '=', when a 'pass through wiring' is attempted (both sides are internal), then  one side must be 'used' and the other 'provided'",
                                        connection );
                            }
                        }
                        break;
                }
            }
        }
    }

    private void wireMessage( EndpointResolver leftResolver, EndpointResolver rightResolver, Connection connection, AnalyzeStack stack ){
        // a small message telling the user what gets connected with what

        StringBuilder builder = new StringBuilder();
        wireMessage( leftResolver, builder );

        Wire wire = connection.getWire();
        if( wire == null ){
            builder.append( " " );
        }
        else{
            switch( wire.getDirection() ){
                case ASSIGN:
                    builder.append( " = " );
                    break;
                case LEFT_TO_RIGHT:
                    builder.append( " -> " );
                    break;
                case RIGHT_TO_LEFT:
                    builder.append( " <- " );
                    break;
            }
        }

        wireMessage( rightResolver, builder );
        stack.message( builder.toString(), connection );
    }

    private void wireMessage( EndpointResolver resolver, StringBuilder builder ){
        boolean dot = false;
        if( resolver.component != null ){
            builder.append( resolver.component.getReference() );
            dot = true;
        }

        if( resolver.interfaze != null ){
            if( dot )
                builder.append( "." );

            builder.append( resolver.interfaze.getReference().toIdentifier() );
        }
        else if( resolver.function != null ){
            if( dot )
                builder.append( "." );

            Type type = resolver.function.getType();
            String name = resolver.function.getName();

            if( type == null ){
                builder.append( name );
            }
            else{
                builder.append( type.toLabel( name, Type.Label.EXTENDED ) );
            }
        }
    }

    private String reportWrongTypes( InterfaceReferenceModelConnection found, Map<GenericType, Type> generics ){
        Type[] parameters = found.getParameters();
        if( parameters == null || parameters.length == 0 ){
            return "can't resolve implicit connection: implicit interface '" + found.getName() + "' was defined without generic parameters";
        }
        else{
            StringBuilder builder = new StringBuilder();
            builder.append( "can't resolve implicit connection: implicit interface '" );
            builder.append( found.getName() );
            builder.append( "' was defined with generic parameters: " );

            boolean first = true;
            for( Type type : parameters ){
                if( first )
                    first = false;
                else
                    builder.append( ", " );

                builder.append( "'" );
                if( type != null && generics != null )
                    type = type.replace( generics );

                if( type == null )
                    builder.append( "?" );
                else
                    builder.append( type.toLabel( null, Type.Label.SMALL ) );

                builder.append( "'" );
            }

            return builder.toString();    
        }
    }

    private boolean checkSameInterfaces( InterfaceReferenceModelConnection left, InterfaceReferenceModelConnection right, AnalyzeStack stack, Connection connection ){
        ModelNode leftNode = stack.getDeclarationResolver().resolve( left, stack.getCancellationMonitor().getProgressMonitor() );
        stack.checkCancellation();

        ModelNode rightNode = stack.getDeclarationResolver().resolve( right, stack.getCancellationMonitor().getProgressMonitor() );
        stack.checkCancellation();

        if( leftNode == null && rightNode == null )
            return false;

        if( leftNode != null && rightNode != null ){
            ASTModelPath leftPath = leftNode.getPath();
            ASTModelPath rightPath = rightNode.getPath();

            if( leftPath.equals( rightPath ))
                return true;
        }

        stack.error( "left interface '" + left.getName() + "' does not match right interface '" + right.getName() + "'", connection );

        return false;
    }

    private static enum Match{
    	/** the interfaces are not compatible */
    	NO_MATCH,
    	
    	/** the types are exactly the same */
    	FULL_MATCH, 
    	
    	/** the types are nearly the same. In practical use this would work, but in theory this is false */
    	SUSPICIOUS_MATCH
    }
    
    private Match typesMatch( 
            InterfaceReferenceModelConnection expected, Map<GenericType, Type> expectedGenerics,
            InterfaceReferenceModelConnection found, Map<GenericType, Type> foundGenerics,
            List<Type> mismatches ){

        Type[] expectedParameters = expected.getParameters();
        Type[] foundParameters = found.getParameters();
        
        if( (expectedParameters == null || expectedParameters.length == 0) && 
                (foundParameters == null || foundParameters.length == 0 )){
            return Match.FULL_MATCH;
        }
        else if( expectedParameters != null && foundParameters != null ){
            if( expectedParameters.length == foundParameters.length ){
                ConversionTable conversion = ConversionTable.instance();

                Match result = Match.FULL_MATCH;
                
                for( int i = 0, n = expectedParameters.length; i<n; i++ ){
                    Type expectedType = expectedParameters[i];
                    if( expectedType != null && expectedGenerics != null )
                        expectedType = expectedType.replace( expectedGenerics );

                    Type foundType = foundParameters[i];
                    if( foundType != null && foundGenerics != null )
                        foundType = foundType.replace( foundGenerics );

                    boolean equalExact = conversion.equals( expectedType, foundType, true );
                    boolean equalSuspicious = !equalExact && conversion.equals( expectedType, foundType, false );
                    
                    if( !equalExact ){
                    	if( equalSuspicious ){
                    		if( result != Match.NO_MATCH ){
                    			result = Match.SUSPICIOUS_MATCH;
                    		}
                    	}
                    	else{
                    		result = Match.NO_MATCH;
                    	}
                    	
                    	if( mismatches == null ){
                    		return result;
                    	}
                    	
                    	mismatches.add( expectedType );
                    	mismatches.add( foundType );
                    }
                }

                return result;
            }
        }

        return Match.NO_MATCH;
    }

    /**
     * Used to resolve the exact meaning of an endpoint.
     * @author Benjamin Sigg
     */
    private class EndpointResolver{
        private AnalyzeStack stack;

        private String componentName;
        private String specificationName;

        /** the component to which this endpoint might point */
        public ComponentReferenceModelConnection component;

        /** the resolved {@link #component} */
        private ComponentModelNode componentNode;
        private boolean componentResolved = false;
        
                private Map<GenericType, Type> replacements;
        private boolean replacementsResolved = false;

        /** the interface to which this endpoint might point */
        public InterfaceReferenceModelConnection interfaze;

        private InterfaceModelNode interfaceNode;
        private boolean interfaceResolved = false;
        
        /** the function to which this endpoint might point */
        public FieldModelConnection function;
        
        private FieldModelNode functionNode;
        private boolean functionResolved = false;

        public boolean used;
        public boolean provided;

        private Endpoint endpoint;

        private boolean implicit;

        public EndpointResolver( AnalyzeStack stack, Endpoint endpoint ){
            this.stack = stack;
            this.endpoint = endpoint;

            ParameterizedIdentifier component = endpoint.getComponent();
            if( component != null && component.getIdentifier() != null )
                this.componentName = component.getIdentifier().getName();

            ParameterizedIdentifier specification = endpoint.getSpecification();
            if( specification != null ){
                Identifier id = specification.getIdentifier();
                if( id != null ){
                    this.specificationName = id.getName();
                }
            }
        }

        /**
         * Copies either function or interface and adds it to this node
         */
        public void copyConnection(){
            ModelConnection old;

            if( function != null ){
                old = function;
            }
            else if( interfaze != null ){
                old = interfaze;
            }
            else{
                return;
            }

            EndpointModelConnection connection = new EndpointModelConnection(
                    old, 
                    endpoint,
                    componentName, specificationName,
                    endpoint.resolveParameters(), implicit, isInternal() );

            connection.setLabel( old.getLabel() );
            connection.setParseFile( getParseFile() );
            connection.setReference( true );
            connection.setTags( old.getTags().copy() );
            connection.addRegion( stack.getRegion( endpoint ) );

            addChild( connection );
        }

        public void checkIndices( AnalyzeStack stack, MissingIndexCollection collection ){
            Value[] values = endpoint.resolveParameters();

            if( interfaze != null ){
                LazyRangeDescription[] ranges = endpoint.resolveParameterRanges( stack );

                NesCInterfaceReference reference = interfaze.resolve( stack.getBindingResolver() );
                if( reference == null )
                    return;

                Field[] indices = reference.getIndices();

                int valueCount = values == null ? 0 : values.length;
                int indexCount = indices == null ? 0 : indices.length;

                for( int i = 0, n = Math.min( valueCount, indexCount ); i<n; i++ ){
                    if( indices[i] != null && indices[i].getType() != null ){
                        if( values[i] != null ){
                            ConversionTable.instance().check( values[i].getType(), indices[i].getType(),
                                    ConversionMap.assignment( stack, ranges[i], values[i] ) );
                        }
                    }
                }

                ASTNode nameNode = endpoint.getSpecification();
                if( nameNode == null )
                    nameNode = endpoint.getComponent();
                if( nameNode == null )
                    nameNode = endpoint;

                for( int i = valueCount; i < indexCount; i++ ){
                    collection.add( i+1 , indices[i], nameNode );
                }

                for( int i = indexCount; i < valueCount; i++ ){
                    if( ranges[i] != null ){
                        stack.error( "parameter nr. " + (i+1) + " not required", ranges[i].getRange() );
                    }
                }
            }

            if( !endpoint.isTwoComponent() && isImplicit() ){
                if( values != null && values.length > 0 ){
                    stack.error( "parameters present but endpoint is not an interface", endpoint.getParametersSource() );
                }
            }
        }

        public String getLabel(){
            if( specificationName == null )
                return componentName;
            else
                return componentName + "." + specificationName;
        }

        public Map<GenericType, Type> getReplacements(){
            if( replacementsResolved )
                return replacements;
            replacementsResolved = true;

            if( isInternal() )
                return null;

            Generic[] arguments = component.getArguments();
            if( arguments == null )
                return null;

            ComponentModelNode component = getComponent();
            if( component == null )
                return null;

            if( !(component instanceof GenericComponentModelNode) )
                return null;

            GenericComponentModelNode generic = (GenericComponentModelNode)component;
            ModelConnection[] parameters = generic.listParameters( stack.getCancellationMonitor() );

            if( parameters == null )
                return null;

            replacements = new HashMap<GenericType, Type>();
            for( int i = 0, n = Math.min( parameters.length, arguments.length ); i<n; i++ ){
                if( parameters[i] != null && arguments[i] != null ){
                    ModelConnection parameter = parameters[i];
                    if( parameter instanceof GenericTypeModelConnection ){
                        GenericType type = ((GenericTypeModelConnection)parameter).resolve();
                        Generic argument = arguments[i];
                        if( argument.asType() != null ){
                            replacements.put( type, argument.asType() );
                        }
                    }
                }
            }

            return replacements;
        }

        public ComponentModelNode getComponent(){
            if( componentResolved )
                return componentNode;

            componentResolved = true;
            if( component != null ){
                ModelNode node = getDeclarationResolver().resolve( component, stack.getCancellationMonitor().getProgressMonitor() );
                stack.checkCancellation();
                if( node instanceof ComponentModelNode ){
                    componentNode = (ComponentModelNode)node;
                }
            }
            return componentNode;
        }
        
        public InterfaceModelNode getInterface(){
        	if( interfaceResolved )
        		return interfaceNode;

            interfaceResolved = true;
            if( interfaze != null ){
                ModelNode node = getDeclarationResolver().resolve( interfaze, stack.getCancellationMonitor().getProgressMonitor() );
                stack.checkCancellation();
                if( node instanceof InterfaceModelNode ){
                    interfaceNode = (InterfaceModelNode)node;
                }
            }
            
            return interfaceNode;
        }
        
        public FieldModelNode getFunction(){
        	if( functionResolved )
        		return functionNode;
        	
        	functionResolved = true;
        	
        	if( function != null ){
        		ModelNode node = getDeclarationResolver().resolve( function, stack.getCancellationMonitor().getProgressMonitor() );
        		stack.checkCancellation();
        		if( node instanceof FieldModelNode ){
        			functionNode = (FieldModelNode)node;
        		}
        	}
        	
			return functionNode;
		}

        public boolean isInternal(){
            return component == null;
        }

        /**
         * Tells whether this endpoint is implicit, meaning that it points to
         * a component.
         * @return <code>true</code> if the endpoint points to a component but not to
         * an interface or function
         */
        public boolean isImplicit(){
            return component != null && interfaze == null && function == null;
        }

        /**
         * Finds as much as possible about this endpoint without
         * looking at the other side of the connection.
         * @param node the analysed node
         * @return <code>true</code> if everything went smoothly,
         * <code>false</code> if an error was found
         */
        public boolean guessMeaning( ConfigurationModelNode node ){
            boolean error = false;

            if( specificationName == null ){
                // component = node.getComponent( componentName, stack.getCancellationMonitor() );
                component = endpoint.resolveReference( stack );

                if( component == null ){
                    interfaze = node.getUsesProvides( componentName, stack.getCancellationMonitor() );
                    if( interfaze == null ){
                        function = node.getUsesProvidesFunction( componentName, stack.getCancellationMonitor() );
                        if( function == null ){
                            error = true;
                            if( stack.isReportErrors() ){
                                stack.error( "missing declaration for '" + componentName + "'", endpoint.getComponent() == null ? endpoint : endpoint.getComponent() );
                            }
                        }
                        else{
                            used = function.getTags().contains( Tag.USES );
                            provided = function.getTags().contains( Tag.PROVIDES );
                        }
                    }
                    else{
                        used = interfaze.getTags().contains( Tag.USES );
                        provided = interfaze.getTags().contains( Tag.PROVIDES );
                    }
                }
            }
            else{
                // component = node.getComponent( componentName, stack.getCancellationMonitor() );
                component = endpoint.resolveReference( stack );

                if( component == null ){
                    error= true;
                    if( stack.isReportErrors() ){
                        stack.error( "missing component for '" + componentName + "'", endpoint.getComponent() == null ? endpoint : endpoint.getComponent() );
                    }
                }
                else{
                    ModelNode resolvedNode = getComponent();
                    if( resolvedNode == null || !(resolvedNode instanceof ComponentModelNode)){
                        error = true;
                    }
                    else{
                        ComponentModelNode componentNode = (ComponentModelNode)resolvedNode;
                        ModelConnection result = componentNode.getUsesProvidesConnection( specificationName, stack.getCancellationMonitor() );

                        if( result == null ){
                            error = true;
                            if( stack.isReportErrors() ){
                                stack.error( "missing declaration for '" + specificationName + "'", endpoint.getSpecification().getIdentifier() );
                            }
                        }
                        else{
                            used = result.getTags().contains( Tag.USES );
                            provided = result.getTags().contains( Tag.PROVIDES );

                            if( result.getTags().contains( Tag.INTERFACE ))
                                interfaze = (InterfaceReferenceModelConnection)result;
                            else if( result.getTags().contains( Tag.FUNCTION ))
                                function = (FieldModelConnection)result;
                            else{
                                error = true;
                                if( stack.isReportErrors() ){
                                    stack.error( "pointing neither to interface nor function", endpoint.getSpecification().getIdentifier() );
                                }
                            }
                        }
                    }
                }
            }

            implicit = isImplicit();
            return !error;
        }
        
        /**
         * Builds and connects {@link ASTReference}s.
         */
        public void reference(){
        	ComponentModelNode componentNode = getComponent();
        	if( componentNode != null ){
        		stack.reference( endpoint.getComponent(), componentNode.getPath() );
        	}
        	
        	ASTNode second = getSecond();
        	if( second != null ){
	        	InterfaceModelNode interfaze = getInterface();
	        	if( interfaze != null ){
		        	stack.reference( second, interfaze.getPath() );
	        	}
	        	
	        	FieldModelNode function = getFunction();
	        	if( function != null ){
		        	stack.reference( second, function.getPath() );
	        	}
        	}
        }
        
        private ASTNode getSecond(){
        	ParameterizedIdentifier identifier = endpoint.getSpecification();
        	if( identifier == null )
        		return endpoint.getComponent();
        	
        	return identifier.getIdentifier();
        }
        
        /**
         * Tries to find out to which element this implicit endpoint points.
         * @param other the other, explicit endpoint
         * @param astConnection the connection which is resolved
         * @param shouldBeUsed whether the syntax of the wire hints that this endpoint should point to a used interface/function
         * @param shouldBeProvided whether the syntax of the wire hints that this endpoint should point to a used interface/function
         * @return <code>true</code> if the element could be found, <code>false</code>
         * if not
         */
        public boolean resolveImplicit( EndpointResolver other, Connection astConnection, boolean shouldBeUsed, boolean shouldBeProvided ){
            ComponentModelNode node = (ComponentModelNode)getDeclarationResolver().resolve( component, stack.getCancellationMonitor().getProgressMonitor() );
            stack.checkCancellation();
            if( node == null )
                return false;

            if( !shouldBeProvided && !shouldBeUsed ){
                if( other.isInternal() == isInternal() ){
                    shouldBeUsed = other.provided;
                    shouldBeProvided = other.used;
                }
                else{
                    shouldBeUsed = other.used;
                    shouldBeProvided = other.provided;
                }
            }

            if( other.interfaze != null ){
                // pointing to an interface, check the name
                Name name = other.interfaze.getReference();

                // list of all interfaces with the correct name
                List<InterfaceReferenceCheck> allInterfaces = new ArrayList<InterfaceReferenceCheck>();


                for( ModelConnection connection : node.listUsesProvides( stack.getCancellationMonitor() ) ){
                    stack.checkCancellation();

                    if( name.toIdentifier().equals( connection.getIdentifier() ) && connection.getTags().contains( Tag.INTERFACE )){
                        InterfaceReferenceModelConnection interfaceConnection = (InterfaceReferenceModelConnection)connection;

                        Match typesCorrect = typesMatch( 
                                other.interfaze, other.getReplacements(),
                                interfaceConnection, getReplacements(), null );

                        boolean usageCorrect = 
                            (shouldBeUsed && connection.getTags().contains( Tag.USES )) || 
                            (shouldBeProvided && connection.getTags().contains( Tag.PROVIDES ));

                        allInterfaces.add( new InterfaceReferenceCheck( interfaceConnection, typesCorrect, usageCorrect ) );
                    }
                }

                putBest( name, allInterfaces, astConnection );

                if( interfaze != null )
                    return true;

                return false;
            }
            else if( other.function != null ){
                Type type = other.function.getType();

                FieldModelConnection wrongFunction = null;
                boolean wrongUsed = false;
                boolean wrongProvided = false;

                // pointing to event or command, check the type
                for( ModelConnection connection : node.listUsesProvides( stack.getCancellationMonitor() ) ){
                    stack.checkCancellation();

                    if( connection.getTags().contains( Tag.FUNCTION )){
                        if( ((FieldModelConnection)connection).getType().equals( type ) ){
                            if( (shouldBeUsed && connection.getTags().contains( Tag.USES )) || 
                                    (shouldBeProvided && connection.getTags().contains( Tag.PROVIDES ))){
                                if( function == null ){
                                    function = (FieldModelConnection)connection;
                                    used = connection.getTags().contains( Tag.USES );
                                    provided = connection.getTags().contains( Tag.PROVIDES );
                                }
                                else{
                                    if( stack.isReportErrors() ){
                                        stack.error( "can't resolve implicit connection: more than one function of type '" + type + "' in uses/provides list", astConnection );
                                    }
                                    return false;
                                }
                            }
                            else{
                                if( wrongFunction == null ){
                                    wrongFunction = (FieldModelConnection)connection;
                                    wrongUsed = connection.getTags().contains( Tag.USES );
                                    wrongProvided = connection.getTags().contains( Tag.PROVIDES );
                                }
                            }
                        }
                    }
                }

                if( function == null ){
                    function = wrongFunction;
                    used = wrongUsed;
                    provided = wrongProvided;
                }

                if( function != null )
                    return true;

                if( stack != null && stack.isReportErrors() ){
                    stack.error( "can't resolve implicit connection: missing function of type '" + type + "' in uses/provides list", astConnection );
                }

                return false;
            }
            else{
                if( stack.isReportErrors() ){
                    stack.warning( "internal error", astConnection );
                }
                Debug.error( "trying to resolve unresolvable implicit connection" );
                return false;
            }
        }

        private void putBest( Name name, List<InterfaceReferenceCheck> interfaces, Connection astConnection ){
            if( interfaces.size() == 0 ){
                if( stack.isReportErrors() ){
                    stack.error( "can't resolve implicit connection: no interface of type '" + name + "' found", astConnection );
                }
                return;
            }

            // search for correct solution
            int count = select( interfaces, Match.FULL_MATCH, true );
            if( count >= 1 ){
                if( count > 1 && stack.isReportErrors() ){
                    stack.error( "can't resolve implicit connection: more than one interface of type '" + name + "' matches", astConnection );
                }
                return;
            }
            
            // check those interfaces whose usage is correct
            count = select( interfaces, Match.SUSPICIOUS_MATCH, true );
            if( count >= 1 ){
            	if( count > 1 && stack.isReportErrors() ){
            		stack.error( "can't resolve implicit connection: there are interfaces of type '" + name + "', their generic types have the same declaration, but different names", astConnection );
            	}
            	return;
            }

            count = select( interfaces, Match.NO_MATCH, true );
            if( count >= 1 ){
            	if( count > 1 && stack.isReportErrors() ){
            		stack.error( "can't resolve implicit connection: there are interfaces of type '" + name + "', but their generic types do not match", astConnection );
            	}
            	return;
            }

            // check those interfaces whose types are correct
            count = select( interfaces, Match.FULL_MATCH, false );
            if( count >= 1 ){
            	if( count > 1 && stack.isReportErrors() ){
            		stack.error( "can't resolve implicit connection: there are interfaces of type '" +
                            name + "' but their used/provided flag does not match", astConnection );
            	}
            	return;
            }
            
            count = select( interfaces, Match.SUSPICIOUS_MATCH, false );
            if( count >= 1 ){
            	if( count > 1 && stack.isReportErrors() ){
            		stack.error( "can't resolve implicit connection: there are interfaces of type '" +
                            name + "' but their used/provided flag does not match and their generic types seem to be inconsistent", astConnection );
            	}
            	return;
            }
            
            // now check everything that is left
            count = select( interfaces, Match.NO_MATCH, false );
            if( count >= 1 ){
            	if( count > 1 && stack.isReportErrors() ){
            		stack.error( "can't resolve implicit connection: there are interfaces of type '" + name + "' but neither usage nor generic types match", astConnection );
            	}
            	return;
            }
        }
        
        private int select( List<InterfaceReferenceCheck> interfaces, Match typesCorrect, boolean useageCorrect ){
        	int count = count( interfaces, typesCorrect, useageCorrect );
        	if( count == 1 ){
        		nail( interfaces, typesCorrect, useageCorrect );
        	}
        	return count;
        }
        
        private int count( List<InterfaceReferenceCheck> interfaces, Match typesCorrect, boolean useageCorrect ){
        	int count = 0;
        	
        	for( InterfaceReferenceCheck check : interfaces ){
                if( check.typesCorrect == typesCorrect && check.usageCorrect == useageCorrect ){
                    count++;
                }
            }
        	
        	return count;
        }
        
        private void nail( List<InterfaceReferenceCheck> interfaces, Match typesCorrect, boolean useageCorrect ){
            for( InterfaceReferenceCheck check : interfaces ){
                if( check.typesCorrect == typesCorrect && check.usageCorrect == useageCorrect ){
                    nail( check );
                    return;
                }
            }
        }

        private void nail( InterfaceReferenceCheck check ){
        	interfaze = check.interfaze;
        	used = check.interfaze.isUsed();
        	provided = check.interfaze.isProvided();
        }
    }
    
    private static class InterfaceReferenceCheck{
        public InterfaceReferenceModelConnection interfaze;
        public Match typesCorrect;
        public boolean usageCorrect;

        public InterfaceReferenceCheck(
                InterfaceReferenceModelConnection interfaze,
                Match typesCorrect, boolean usageCorrect ){
            this.interfaze = interfaze;
            this.typesCorrect = typesCorrect;
            this.usageCorrect = usageCorrect;
        }
    }

    private static class MissingIndexCollection{
        private SortedMap<Integer, Missing> leftSide = new TreeMap<Integer, Missing>();
        private SortedMap<Integer, Missing> rightSide = new TreeMap<Integer, Missing>();

        private boolean side = true;

        public void setSide( boolean side ){
            this.side = side;
        }

        public void add( int index, Field value, ASTNode nameNode ){
            if( side ){
                leftSide.put( index, new Missing( value, nameNode ) );
            }
            else{
                rightSide.put( index, new Missing( value, nameNode ) );
            }
        }

        public void check( AnalyzeStack stack ){
            // stack.error( "missing parameter nr. " + (i+1) + ": '" + indices[i].getDeclaration( null ) + "'", nameNode );

            
            while( leftSide.size() > 0 && rightSide.size() > 0 ){
                Integer leftEnd = leftSide.lastKey();
                Integer rightEnd = rightSide.lastKey();
                
                Missing leftMissing = leftSide.remove( leftEnd );
                Missing rightMissing = rightSide.remove( rightEnd );

                Type leftType = leftMissing.value == null ? null : leftMissing.value.getType();
                Type rightType = rightMissing.value == null ? null : rightMissing.value.getType();

                if( leftType != null && rightType != null ){
                    if( !ConversionTable.instance().equals( leftType, rightType, false )){
                        stack.error( "left parameter nr. " + leftEnd + " of type '" + leftType.toLabel( null, Type.Label.SMALL ) + 
                                "' does not match right parameter nr. " + rightEnd + " of type '" +
                                rightType.toLabel( null, Type.Label.SMALL ) + "'", leftMissing.nameNode, rightMissing.nameNode );
                    }
                }
            }

            for( Map.Entry<Integer, Missing> entry : leftSide.entrySet() ){
                if( entry.getValue().value != null ){
                    stack.error( "missing parameter nr. " + entry.getKey() + ": '" + 
                            entry.getValue().value.getDeclaration( null ) + "'",
                            entry.getValue().nameNode );
                }
            }
            
            for( Map.Entry<Integer, Missing> entry : rightSide.entrySet() ){
                if( entry.getValue().value != null ){
                    stack.error( "missing parameter nr. " + entry.getKey() + ": '" + 
                            entry.getValue().value.getDeclaration( null ) + "'",
                            entry.getValue().nameNode );
                }
            }
        }

        private class Missing{
            public Field value;
            public ASTNode nameNode;

            public Missing( Field value, ASTNode nameNode ){
                this.value = value;
                this.nameNode = nameNode;
            }
        }
    }
}
