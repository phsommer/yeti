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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.inspection.INesCComponent;
import tinyos.yeti.ep.parser.inspection.INesCFunction;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCInterfaceReference;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.inspection.InspectionKey;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.nodes.ComponentModelNode;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;

public abstract class NesCComponent extends AbstractNesCBinding implements INesCComponent{
    private NesCInterfaceReference[] usedInterfaces;
    private NesCInterfaceReference[] providedInterfaces;
    private Field[] usedFunctions;
    private Field[] providedFunctions;
    
    private Binding[] interfacesAndFunctions;

    private ComponentModelNode raw;
    protected BindingResolver bindings;
    
    protected Map<GenericType, Type> replacements;
    
    private InspectionKey<?>[] keys = {
    		PROVIDED_FUNCTION,
    		PROVIDED_INTERFACE,
    		USED_FUNCTION,
    		USED_INTERFACE,
    };
    
    /**
     * Creates a new component
     * @param type what kind of component this is
     * @param raw reference to the real component
     * @param bindings resolver to analyze bindings 
     */
    public NesCComponent( String type, ComponentModelNode raw, BindingResolver bindings ){
        this( type, raw, bindings, null );
    }
    
    /**
     * Creates a new component
     * @param type what kind of component this is
     * @param raw reference to the real component
     * @param bindings resolver to analyze bindings 
     * @param replacements types used to replace any type in subelements of this component
     */
    public NesCComponent( String type, ComponentModelNode raw, BindingResolver bindings, Map<GenericType, Type> replacements ){
        super( type );
        this.raw = raw;
        this.bindings = bindings;
        this.replacements = replacements;
    }
    
    public IASTModelNodeConnection asConnection(){
    	return null;
    }
    
    public IASTModelNode asNode(){
    	return raw;
    }
    
    public int getReferenceKindCount(){
    	return keys.length;
    }
    
    public InspectionKey<?> getReferenceKind( int index ){
    	return keys[ index ];
    }
    
    @SuppressWarnings("unchecked")
	public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector ){
    	if( PROVIDED_FUNCTION.equals( key )){
    		resolveProvidesFunction();
    		if( providedFunctions == null )
    			return (K[])new INesCFunction[]{};
    		INesCFunction[] result = new INesCFunction[ providedFunctions.length ];
    		System.arraycopy( providedFunctions, 0, result, 0, result.length );
    		return (K[])result;
    	}
    	if( PROVIDED_INTERFACE.equals( key )){
    		resolveProvides();
    		if( providedInterfaces == null )
    			return (K[])new INesCInterfaceReference[]{};
    		INesCInterfaceReference[] result = new INesCInterfaceReference[ providedInterfaces.length ];
    		System.arraycopy( providedInterfaces, 0, result, 0, result.length );
    		return (K[])result;
    	}
    	if( USED_FUNCTION.equals( key )){
    		resolveUsesFunction();
    		if( usedFunctions == null )
    			return (K[])new INesCFunction[]{};
    		INesCFunction[] result = new INesCFunction[ usedFunctions.length ];
    		System.arraycopy( usedFunctions, 0, result, 0, result.length );
    		return (K[])result;
    	}
    	if( USED_INTERFACE.equals( key )){
    		resolveUses();
    		if( usedInterfaces == null )
    			return (K[])new INesCInterfaceReference[]{};
    		INesCInterfaceReference[] result = new INesCInterfaceReference[ usedInterfaces.length ];
    		System.arraycopy( usedInterfaces, 0, result, 0, result.length );
    		return (K[])result;
    	}
    	return null;
    }
    
    @Override
    public String getBindingValue() {
        return getName();
    }
    
    public int getSegmentCount() {
        return 4;
    }
    
    public String getSegmentName( int segment ) {
        switch( segment ){
            case 0: return "uses";
            case 1: return "uses";
            case 2: return "provides";
            case 3: return "provides";
            default: return null;
        }
    }
    
    public int getSegmentSize( int segment ) {
        switch( segment ){
            case 0: return getUsesCount();
            case 1: return getUsesFunctionCount();
            case 2: return getProvidesCount();
            case 3: return getProvidesFunctionCount();
            default: return 0;
        }
    }
    
    public Binding getSegmentChild( int segment, int index ) {
        switch( segment ){
            case 0: return getUses( index );
            case 1: return getUsesFunction( index );
            case 2: return getProvides( index );
            case 3: return getProvidesFunction( index );
            default: return null;
        }
    }
    
    private void resolveInterfacesAndFunctions(){
        if( interfacesAndFunctions == null ){
            List<Binding> result = new ArrayList<Binding>();

            ICancellationMonitor monitor = bindings.getCancellationMonitor();
            
            for( ModelConnection connection : raw.listUsesProvides( monitor ) ){
                monitor.checkCancellation();
                
                if( connection instanceof InterfaceReferenceModelConnection ){
                    NesCInterfaceReference reference = ((InterfaceReferenceModelConnection)connection).resolve( bindings );
                    if( reference != null ){
                        if( replacements != null )
                            reference = reference.replace( replacements );
                        
                        result.add( reference );
                    }
                }
                if( connection.getTags().contains( Tag.FUNCTION )){
                    ModelNode node = raw.getDeclarationResolver().resolve( connection, monitor.getProgressMonitor() );
                    monitor.checkCancellation();
                    
                    if( node instanceof FieldModelNode ){
                        if( replacements != null ){
                            result.add( ((Field)node).replace( null, replacements ));
                        }
                        else{
                            result.add( (Field)node );
                        }
                    }
                }
            }
            
            interfacesAndFunctions = result.toArray( new Binding[ result.size() ] );
        }
    }
    
    public int getUsesProvidesCount(){
        resolveInterfacesAndFunctions();
        return interfacesAndFunctions.length;
    }
    
    /**
     * Gets a {@link Field} or a {@link NesCInterfaceReference} that is used or provided
     * @param index the index of the used/provided element
     * @return the element
     */
    public Binding getUsesProvides( int index ){
        resolveInterfacesAndFunctions();
        return interfacesAndFunctions[ index ];
    }
    
    /**
     * Searches the used or provided interface reference <code>name</code>.
     * @param name the name of some interface
     * @return the reference or <code>null</code>
     */
    public NesCInterfaceReference getInterfaceReference( String name ){
        resolveInterfacesAndFunctions();
        for( int i = 0, n = interfacesAndFunctions.length; i<n; i++ ){
            if( interfacesAndFunctions[i] instanceof NesCInterfaceReference ){
                NesCInterfaceReference reference = (NesCInterfaceReference)interfacesAndFunctions[i];
                if( reference.getName().toIdentifier().equals( name )){
                    return reference;
                }
            }
        }
        
        return null;
    }
    
    private void resolveUses(){
        if( usedInterfaces == null ){
            List<NesCInterfaceReference> result = new ArrayList<NesCInterfaceReference>();
            
            ICancellationMonitor monitor = bindings.getCancellationMonitor();
            
            for( ModelConnection connection : raw.listUsesProvides( monitor ) ){
                monitor.checkCancellation();
                if( connection.getTags().contains( Tag.USES ) && connection.getTags().contains( Tag.INTERFACE )){
                    if( connection instanceof InterfaceReferenceModelConnection ){
                        NesCInterfaceReference reference = ((InterfaceReferenceModelConnection)connection).resolve( bindings );
                        if( reference != null ){
                            if( replacements != null )
                                reference = reference.replace( replacements );
                            
                            result.add( reference );
                        }
                    }
                }
            }
            
            usedInterfaces = result.toArray( new NesCInterfaceReference[ result.size() ]);
        }
    }
    
    /**
     * Gets the number of interfaces this component uses.
     * @return the number used interfaces
     */
    public int getUsesCount(){
        resolveUses();
        return usedInterfaces.length;
    }
    
    /**
     * Gets the index'th interface this component uses.
     * @param index the index of the interface
     * @return the interface
     */
    public NesCInterfaceReference getUses( int index ){
        resolveUses();
        return usedInterfaces[ index ];
    }

    public NesCInterfaceReference getUses( String name ){
        resolveUses();
        for( NesCInterfaceReference reference : usedInterfaces ){
            if( reference.getName().toIdentifier().equals( name ))
                return reference;
        }
        return null;
    }

    private void resolveUsesFunction(){
        if( usedFunctions == null ){
            List<Field> result = new ArrayList<Field>();
            ICancellationMonitor monitor = bindings.getCancellationMonitor();
            for( ModelConnection connection : raw.listUsesProvides( monitor ) ){
                if( connection.getTags().contains( Tag.USES ) && connection.getTags().contains( Tag.FUNCTION )){
                    ModelNode node = raw.getDeclarationResolver().resolve( connection, monitor.getProgressMonitor() );
                    monitor.checkCancellation();
                    
                    if( node instanceof FieldModelNode ){
                        if( replacements != null ){
                            result.add( ((Field)node).replace( null, replacements ));
                        }
                        else{
                            result.add( (Field)node );
                        }
                    }
                }
            }
            
            usedFunctions = result.toArray( new Field[ result.size() ]);
        }
    }
    
    /**
     * Gets the number of functions this component uses.
     * @return the number used functions
     */
    public int getUsesFunctionCount(){
        resolveUsesFunction();
        return usedFunctions.length;
    }
    
    /**
     * Gets the index'th function this component uses.
     * @param index the index of the function
     * @return the function
     */
    public Field getUsesFunction( int index ){
        resolveUsesFunction();
        return usedFunctions[ index ];
    }
    
    public Field getUsesFunction( String name ){
        resolveUsesFunction();
        for( Field field : usedFunctions ){
            if( name.equals( Name.toIdentifier( field.getName() ) ) )
                return field;
        }
        return null;
    }
    
    private void resolveProvides(){
        if( providedInterfaces == null ){
            List<NesCInterfaceReference> result = new ArrayList<NesCInterfaceReference>();
            
            ICancellationMonitor monitor = bindings.getCancellationMonitor();
            for( ModelConnection connection : raw.listUsesProvides( monitor ) ){
                monitor.checkCancellation();
                if( connection.getTags().contains( Tag.PROVIDES ) && connection.getTags().contains( Tag.INTERFACE )){
                    if( connection instanceof InterfaceReferenceModelConnection ){
                        NesCInterfaceReference reference = ((InterfaceReferenceModelConnection)connection).resolve( bindings );
                        if( reference != null ){
                            if( replacements != null ){
                                reference = reference.replace( replacements );
                            }
                            
                            result.add( reference );
                        }
                    }
                }
            }
            
            providedInterfaces = result.toArray( new NesCInterfaceReference[ result.size() ]);
        }
    }
    
    /**
     * Gets the number of interfaces this component provides.
     * @return the number provided interfaces
     */
    public int getProvidesCount(){
        resolveProvides();
        return providedInterfaces.length;
    }
    
    /**
     * Gets the index'th interface this component provides.
     * @param index the index of the interface
     * @return the interface
     */
    public NesCInterfaceReference getProvides( int index ){
        resolveProvides();
        return providedInterfaces[ index ];
    }

    public NesCInterfaceReference getProvides( String name ){
        resolveProvides();
        for( NesCInterfaceReference reference : providedInterfaces ){
            if( reference.getName().toIdentifier().equals( name )){
                return reference;
            }
        }
        return null;
    }
    
    private void resolveProvidesFunction(){
        if( providedFunctions == null ){
            List<Field> result = new ArrayList<Field>();
            
            ICancellationMonitor monitor = bindings.getCancellationMonitor();
            for( ModelConnection connection : raw.listUsesProvides( monitor ) ){
                if( connection.getTags().contains( Tag.PROVIDES ) && connection.getTags().contains( Tag.FUNCTION )){
                    ModelNode node = raw.getDeclarationResolver().resolve( connection, monitor.getProgressMonitor() );
                    monitor.checkCancellation();
                    
                    if( node instanceof FieldModelNode ){
                        if( replacements != null ){
                            result.add( ((Field)node).replace( null, replacements ));
                        }
                        else{
                            result.add( (Field)node );
                        }
                    }
                }
            }
            
            providedFunctions = result.toArray( new Field[ result.size() ]);
        }
    }
    
    /**
     * Gets the number of functions this component provides.
     * @return the number provided functions
     */
    public int getProvidesFunctionCount(){
        resolveProvidesFunction();
        return providedFunctions.length;
    }
    
    /**
     * Gets the index'th function this component provides.
     * @param index the index of the function
     * @return the function
     */
    public Field getProvidesFunction( int index ){
        resolveProvidesFunction();
        return providedFunctions[ index ];
    }

    public Field getProvidesFunction( String name ){
        resolveProvidesFunction();
        for( Field field : providedFunctions ){
            if( name.equals( Name.toIdentifier( field.getName() ) ) )
                return field;
        }
        return null;
    }
    
    public NesCComponent replace( Map<GenericType, Type> generics ){
        Map<GenericType, Type> map = generics;
        if( replacements != null ){
            map = new HashMap<GenericType, Type>( replacements );
            map.putAll( generics );
        }   
        
        Binding binding = bindings.getBinding( raw.getPath(), "parameterized NesCComponent", map );
        if( binding == null ){
            binding = createComponent( map );
            bindings.putBinding( raw.getPath(), "parameterized NesCComponent", map, binding );
        }
        
        if( binding instanceof NesCComponent )
            return (NesCComponent)binding;
        
        return null;
    }
    
    protected abstract NesCComponent createComponent( Map<GenericType, Type> generics );
    
    /**
     * Gets the name of this component
     * @return the name
     */
    public String getName(){
        return raw.getIdentifier();
    }
}
