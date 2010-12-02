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
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCInterface;
import tinyos.yeti.ep.parser.inspection.INesCInterfaceReference;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.inspection.InspectionKey;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.declarations.BaseDeclaration;
import tinyos.yeti.nesc12.ep.nodes.InterfaceModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedInterface;

/**
 * The reference to an interface or array of interfaces as they could stand
 * in a uses/provides clause: "uses interface raw &lt;parameters&gt; as name[ index ]"
 * @author Benjamin Sigg
 */
public class NesCInterfaceReference extends AbstractNesCBinding implements INesCInterfaceReference{
	public static final InspectionKey<INesCInterface> INTERFACE = new InspectionKey<INesCInterface>( INesCInterface.class, "reference" );
	
    private InterfaceReferenceModelConnection reference;
    private ParameterizedInterface astReference;
    private BindingResolver bindings;
    
    private NesCInterface raw;
    private NesCInterface parameterized;
    
    private boolean parametersResolved = false;
    private Type[] parameters;
    private Map<GenericType, Type> replacements;
    
    private DeclarationResolver resolver;
    
    public NesCInterfaceReference( DeclarationResolver resolver, ParameterizedInterface astReference, InterfaceReferenceModelConnection reference, BindingResolver bindings ){
        this( resolver, astReference, reference, bindings, null );
    }

    public NesCInterfaceReference( DeclarationResolver resolver, ParameterizedInterface astReference, InterfaceReferenceModelConnection reference, BindingResolver bindings, Map<GenericType, Type> replacements ){
        super( "Interface Reference" );
        this.resolver = resolver;
        this.astReference = astReference;
        this.reference = reference;
        this.bindings = bindings;
        this.replacements = replacements;
    }
    
    public DeclarationResolver getDeclarationResolver(){
    	return resolver;
    }
    
    /**
     * Returns a simple declaration of this reference.
     * @return the declaration
     */
    public IDeclaration toDeclaration(){
    	String name = getName().toIdentifier();
    	
    	TagSet set;
    	if( isUsed() )
    		set = TagSet.get( Tag.INTERFACE, Tag.USES );
    	else if( isProvided() )
    		set = TagSet.get( Tag.INTERFACE, Tag.PROVIDES );
    	else
    		set = TagSet.get( Tag.INTERFACE );
    	
    	return new BaseDeclaration(  Kind.INTERFACE, name, name, null, null, set );
    }
    
    @Override
    public String getBindingValue() {
        Name name = getName();
        if( name == null )
            return null;
        return name.toIdentifier();
    }
    
    public int getSegmentCount() {
        return 5;
    }
    
    public String getSegmentName( int segment ) {
         switch( segment ){
             case 0: return "Raw";
             case 1: return "Parameterized";
             case 2: return "Parameters";
             case 3: return "Indices";
             case 4: return "Attributes";
             default: return null;
         }
    }
    
    public int getSegmentSize( int segment ) {
        switch( segment ){
            case 0: return 1;
            case 1: return 1;
            case 2: return getParameters() == null ? 0 : getParameters().length;
            case 3: return getIndices() == null ? 0 : getIndices().length;
            case 4: return getAttributes() == null ? 0 : getAttributes().length;
            default: return 0;
        }
    }
    
    public Binding getSegmentChild( int segment, int index ) {
        switch( segment ){
            case 0: return getRawReference();
            case 1: return getParameterizedReference();
            case 2: return getParameters()[ index ];
            case 3: return getIndices()[ index ];
            case 4: return getAttributes()[ index ];
            default: return null;
        }
    }
    
    public IASTModelNodeConnection asConnection(){
	    return reference;
    }
    
    public IASTModelNode asNode(){
    	return null;
    }
    
    public int getReferenceKindCount(){
    	return 1;
    }
    
    public InspectionKey<?> getReferenceKind( int index ){
    	return INTERFACE;
    }
    
    @SuppressWarnings("unchecked")
	public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector ){
    	if( INTERFACE.equals( key )){
    		NesCInterface reference = getParameterizedReference();
    		if( reference == null )
    			return (K[])new INesCInterface[]{};
    		else
    			return (K[])new INesCInterface[]{ reference };
    	}
    	return null;
    }
    
    /**
     * Gets the model behind this object.
     * @return the model node behind
     */
    public InterfaceReferenceModelConnection getModel(){
        return reference;
    }
    
    /**
     * Gets the name under which the interface is known in the component.
     * @return either the renamed name or if not present the original name
     * of the interface
     */
    public Name getName() {
        return reference.getName();
    }
    
    public String getVisibleInterfaceName(){
    	Name name = getName();
    	if( name == null )
    		return null;
    	return name.toIdentifier();
    }
    
    public String getReferencedInterfaceName(){
        return reference.getIdentifier();
    }
    
    private void resolveRaw(){
        if( raw == null ){
            IDeclaration declaration = getDeclarationResolver().resolve( getReferencedInterfaceName(), 
            		bindings.getCancellationMonitor().getProgressMonitor(), Kind.INTERFACE );
            if( declaration != null ){
                ICancellationMonitor monitor = bindings.getCancellationMonitor();
                ModelNode node = reference.getDeclarationResolver().resolve( declaration, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                
                if( node instanceof InterfaceModelNode ){
                    raw = ((InterfaceModelNode)node).resolve( bindings );
                }
            }
        }
    }
    
    public NesCInterface getRawReference() {
        resolveRaw();
        return raw;
    }
    
    public INesCInterface getReference(){
    	return getParameterizedReference();
    }
    
    public NesCInterface getParameterizedReference(){
        NesCInterface raw = getRawReference();
        if( raw == null )
            return null;
        
        if( parameterized == null ){
            Type[] parameters = getParameters();
            parameterized = raw.replace( getIndices(), parameters );
        }
        
        return parameterized;
    }
    
    public boolean isUsed() {
        return reference.isUsed();
    }
    
    public boolean isProvided() {
        return reference.isProvided();
    }
    
    /**
     * Gets the types of this interface reference array. This result makes
     * only sense if the use/provides clause had parameters in rectangular 
     * brackets.
     * @return the types of <code>null</code> if no parameters were specified
     */
    public Type[] getParameters(){
        if( parametersResolved )
            return parameters;
        
        parametersResolved = true;
        parameters = reference.getParameters();
        if( replacements != null && !replacements.isEmpty() ){
            Type[] replaced = new Type[ parameters.length ];
            for( int i = 0, n = parameters.length; i<n; i++ ){
                if( parameters[i] != null )
                    replaced[i] = parameters[i].replace( replacements );
            }
            parameters = replaced;
        }
        
        return parameters;
    }
    
    /**
     * Gets the indices used for this reference. This array may be <code>null</code>
     * or empty. It may contain <code>null</code> entries for erroneous
     * fields.
     * @return the list of indices
     */
    public Field[] getIndices(){
    	return reference.getIndex();
    }
    
    /**
     * Gets the attributes which are associated with this reference.
     * @return the attributes, may be <code>null</code>
     */
    public NesCAttribute[] getAttributes(){
    	return astReference.resolveAttributes();
    }
    
    /**
     * Creates a new reference to an interface where all types get replaced
     * by <code>generics</code>.
     * @param generics the replacement for various types
     * @return the new reference
     */
    public NesCInterfaceReference replace( Map<GenericType, Type> generics ){
        Map<GenericType, Type> map = generics;
        if( replacements != null ){
            map = new HashMap<GenericType, Type>( replacements );
            map.putAll( generics );
        }   
        
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append( "parameterized interface" );
        Type[] parameters = getParameters();
        if( parameters != null ){
            idBuilder.append( "<" );
            for( int i = 0, n = parameters.length; i<n; i++ ){
                if( i > 0 )
                    idBuilder.append( "," );
                if( parameters[i] != null )
                    idBuilder.append( parameters[i].id( false ) );
            }
            idBuilder.append( ">" );
        }
        
        String id = idBuilder.toString();
        Binding binding = bindings.getBinding( reference.getFullPath(), id, map );
        if( binding == null ){
            binding = new NesCInterfaceReference( getDeclarationResolver(), astReference, reference, bindings, map );
            bindings.putBinding( reference.getPath(), id, map, binding );
        }
        
        if( binding instanceof NesCInterfaceReference )
            return (NesCInterfaceReference)binding;
        
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( getRawReference() );
        if( !getName().equals( getRawReference().getName() )){
            builder.append( " as " );
            builder.append( getName().toIdentifier() );
        }
        Type[] parameters = getParameters();
        if( parameters != null ){
            builder.append( "[" );
            for( int i = 0, n = parameters.length; i<n; i++ ){
                if( i > 0 )
                    builder.append( ", " );
                builder.append( parameters[i] );
            }
            builder.append( "]" );
        }
        return builder.toString();
    }
}
