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
package tinyos.yeti.nesc12.parser.ast.elements;

import java.util.HashMap;
import java.util.Map;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.inspection.INesCComponent;
import tinyos.yeti.ep.parser.inspection.INesCComponentReference;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.inspection.InspectionKey;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.nodes.ComponentModelNode;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;

/**
 * A reference to a {@link NesCComponent}. A reference can either just
 * rename another component, or use the "new component( arguments ) as name"
 * syntax to create new components.
 * @author Benjamin Sigg
 *
 */
public class NesCComponentReference extends AbstractNesCBinding implements INesCComponentReference{
	public static final InspectionKey<INesCComponent> COMPONENT = new InspectionKey<INesCComponent>( INesCComponent.class, "reference" );
	
    private ComponentReferenceModelConnection raw;
    
    private BindingResolver bindings;
    
    private boolean componentResolved = false;
    private NesCComponent component;
    
    private boolean componentParameterized = false;
    private NesCComponent parameterizedComponent;
    
    private boolean argumentsResolved = false;
    private Generic[] arguments;
    
    private Map<GenericType, Type> replacements;
    
    /**
     * Creates a new reference.
     * @param raw the raw information
     * @param bindings to resolve binding information
     */
    public NesCComponentReference( ComponentReferenceModelConnection raw, BindingResolver bindings ){
        super( "Component Reference" );
        this.raw = raw;
        this.bindings = bindings;
    }
    
    /**
     * Creates a new reference.
     * @param raw the raw information
     * @param bindings to resolve binding information
     * @param replacements information how to replace some types
     */
    public NesCComponentReference( ComponentReferenceModelConnection raw, BindingResolver bindings, Map<GenericType, Type> replacements ){
        super( "Component Reference" );
        this.raw = raw;
        this.bindings = bindings;
        this.replacements = replacements;
    }
    
    public IASTModelNodeConnection asConnection(){
    	return raw;
    }
    
    public IASTModelNode asNode(){
    	return null;
    }
    
    @Override
    public String getBindingValue() {
        String result;
        if( isNewComponent() )
            result = "new ";
        else
            result = "";
        
        String name = getName();
        return result + name;
    }
    
    public int getSegmentCount() {
        return 3;
    }
    
    public String getSegmentName( int segment ) {
        switch( segment ){
            case 0: return "raw component";
            case 1: return "parameterized component";
            case 2: return "arguments";
            default: return null;
        }
    }
    
    public int getSegmentSize( int segment ) {
        switch( segment ){
            case 0: return 1;
            case 1: return 1;
            case 2: getArgumentCount();
            default: return 0;
        }
    }
    
    public Binding getSegmentChild( int segment, int index ) {
        switch( segment ){
            case 0: return getRawComponent();
            case 1: return getParameterizedComponent();
            case 2: return getArgument( index );
            default: return null;
        }
    }
    
    public int getReferenceKindCount(){
    	return 1;
    }
    
    public InspectionKey<?> getReferenceKind( int index ){
    	return COMPONENT;
    }
    
    @SuppressWarnings("unchecked")
	public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector ){
    	if( COMPONENT.equals( key )){
    		NesCComponent component = getParameterizedComponent();
    		if( component == null )
    			return (K[])new INesCComponent[]{};
    		else
    			return (K[])new INesCComponent[]{ component };
    	}
    	return null;
    }
    
    public String getComponentName() {
        return raw.getReference();
    }
    
    public String getRenamed(){
        return raw.getRename();
    }
    
    public String getName(){
        return raw.getName();
    }
    
    public INesCComponent getRawReference(){
    	return getRawComponent();
    }
    
    public INesCComponent getReference(){
    	return getParameterizedComponent();
    }
    
    public String getReferencedComponentName(){
    	return getName();
    }
    
    public String getVisibleComponentName(){
    	String rename = getRenamed();
    	if( rename != null )
    		return rename;
    	return getName();
    }
    
    public NesCComponent getRawComponent() {
        if( !componentResolved ){
            componentResolved = true;
            IDeclaration declaration = raw.getDeclarationResolver().resolve( 
                    getComponentName(), bindings.getCancellationMonitor().getProgressMonitor(), Kind.BINARY_COMPONENT, Kind.CONFIGURATION, Kind.MODULE );
            if( declaration != null ){
                ICancellationMonitor monitor = bindings.getCancellationMonitor();
                ModelNode node = raw.getDeclarationResolver().resolve( declaration, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                
                if( node instanceof ComponentModelNode ){
                    component = ((ComponentModelNode)node).resolve( bindings );
                }
            }
        }
        return component;
    }
    
    public NesCComponent getParameterizedComponent(){
        if( !componentParameterized ){
            componentParameterized = true;
            NesCComponent raw = getRawComponent();
            parameterizedComponent = raw;

            if( raw instanceof NesCGenericComponent ){
                NesCGenericComponent generic = (NesCGenericComponent)raw;
                if( !generic.isGeneric() )
                    return parameterizedComponent;
                
                int parameterCount = generic.getParameterCount();
                int argumentCount = getArgumentCount();
                
                Map<GenericType, Type> generics = new HashMap<GenericType, Type>();
                for( int i = 0, n = Math.min( parameterCount, argumentCount ); i<n; i++ ){
                    Binding parameter = generic.getParameter( i );
                    Generic argument = getArgument( i );
                    
                    if( parameter instanceof Type ){
                        GenericType genericType = ((Type)parameter).asGenericType();
                        if( genericType != null ){
                            Type type = argument.asType();
                            if( type != null ){
                                generics.put( genericType, type );
                            }
                        }
                    }
                }

                parameterizedComponent = generic.replace( generics );
            }
        }

        return parameterizedComponent;
    }
    
    public boolean isNewComponent() {
        return raw.isNewComponent();
    }
    
    private void resolveArguments(){
        if( !argumentsResolved ){
            argumentsResolved = true;
            arguments = raw.getArguments();
            if( arguments != null && replacements != null ){
                Generic[] replacedArguments = new Generic[ arguments.length ];
                for( int i = 0, n = arguments.length; i<n; i++ ){
                    Generic argument = arguments[i];
                    if( argument != null && argument.asType() != null ){
                        argument = argument.asType().replace( replacements );
                    }
                    replacedArguments[i] = argument;
                }
                arguments = replacedArguments;
            }
        }
    }
    
    public boolean hasArguments(){
        resolveArguments();
        return arguments != null;
    }
    
    public int getArgumentCount(){
        resolveArguments();
        
        if( hasArguments() )
            return arguments.length;
        else
            return 0;
    }
    
    public Generic getArgument( int index ){
        resolveArguments();
        return arguments[ index ];
    }
        
    public NesCComponentReference replace( Map<GenericType, Type> generics ){
        Map<GenericType, Type> map = generics;
        if( replacements != null ){
            map = new HashMap<GenericType, Type>( replacements );
            map.putAll( generics );
        }   
        
        Binding binding = bindings.getBinding( raw.getPath(), "parameterized reference", map );
        if( binding == null ){
            binding = new NesCComponentReference( raw, bindings, map );
            bindings.putBinding( raw.getPath(), "parameterized reference", map, binding );
        }
        
        if( binding instanceof NesCComponentReference )
            return (NesCComponentReference)binding;
        
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        if( isNewComponent() )
            builder.append( "new " );
        
        builder.append( raw.getReference() );
        
        if( hasArguments() ){
            builder.append( "(" );
            for( int i = 0, n = getArgumentCount(); i<n; i++ ){
                if( i > 0 )
                    builder.append( ", " );
                
                builder.append( getArgument( i ) );
            }
            builder.append( ")" );
        }
        
        if( raw.getRename() != null ){
            builder.append( " as " );
            builder.append( raw.getRename() );
        }
        
        return builder.toString();
    }
}
