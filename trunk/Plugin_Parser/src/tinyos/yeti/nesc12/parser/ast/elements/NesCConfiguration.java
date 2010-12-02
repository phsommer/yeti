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
import java.util.List;
import java.util.Map;

import tinyos.yeti.ep.parser.inspection.INesCComponentReference;
import tinyos.yeti.ep.parser.inspection.INesCConfiguration;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.inspection.INesCWiring;
import tinyos.yeti.ep.parser.inspection.InspectionKey;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.ConfigurationModelNode;
import tinyos.yeti.nesc12.ep.nodes.ConnectionModelNode;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;

public class NesCConfiguration extends NesCGenericComponent implements INesCConfiguration{
	private ConfigurationModelNode raw;

    private NesCComponentReference[] components;
    private NesCWire[] wiring;

    private InspectionKey<?>[] keys = {
    	COMPONENT,
    	WIRING
    };
    
    public NesCConfiguration( ConfigurationModelNode raw, BindingResolver bindings ){
        super( "Configuration", raw, bindings );
        this.raw = raw;
    }

    public NesCConfiguration( ConfigurationModelNode raw, BindingResolver bindings, Map<GenericType, Type> replacements ){
        super( "Configuration", raw, bindings, replacements );
        this.raw = raw;
    }
    
    @Override
    public int getReferenceKindCount(){
    	return super.getReferenceKindCount() + keys.length;
    }
    
    @Override
    public InspectionKey<?> getReferenceKind( int index ){
    	if( index < keys.length )
    		return keys[ index ];
    	
    	return super.getReferenceKind( index - keys.length );
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector ){
    	if( COMPONENT.equals( key )){
    		resolveComponents();
    		INesCComponentReference[] result = new INesCComponentReference[ components.length ];
    		System.arraycopy( components, 0, result, 0, result.length );
    		return (K[])result;
    	}
    	if( WIRING.equals( key )){
    		resolveWiring();
    		INesCWiring[] result = new INesCWiring[ wiring.length ];
    		System.arraycopy( wiring, 0, result, 0, result.length );
    		return (K[])result;
    	}
    	
    	return super.getReferences( key, inspector );
    }
    
    @Override
    public int getSegmentCount() {
        return super.getSegmentCount() + 2;
    }
    
    @Override
    public String getSegmentName( int segment ) {
        int count = super.getSegmentCount();
        if( segment < count )
            return super.getSegmentName( segment );
        else
            segment -= count;
        
        switch( segment ){
            case 0: return "Components";
            case 1: return "Wiring";
            default: return null;
        }
    }
    
    @Override
    public int getSegmentSize( int segment ) {
        int count = super.getSegmentCount();
        if( segment < count )
            return super.getSegmentSize( segment );
        else
            segment -= count;
        
        switch( segment ){
            case 0: return getComponentCount();
            case 1: return getWireCount();
            default: return 0;
        }
    }
    
    @Override
    public Binding getSegmentChild( int segment, int index ) {
        int count = super.getSegmentCount();
        if( segment < count )
            return super.getSegmentChild( segment, index );
        else
            segment -= count;
        
        switch( segment ){
            case 0: return getComponent( index );
            case 1: return getWire( index );
            default: return null;
        }
    }

    private void resolveWiring(){
        if( wiring == null ){
            List<NesCWire> wiring = new ArrayList<NesCWire>();
            ICancellationMonitor monitor = bindings.getCancellationMonitor();
            
            for( ModelConnection connection : raw.getWiring( monitor ) ){
                ModelNode node = raw.getDeclarationResolver().resolve( connection, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                
                if( node instanceof ConnectionModelNode ){
                    NesCWire wire = ((ConnectionModelNode)node).resolve( bindings );
                    if( wire != null ){
                        wiring.add( wire );
                    }
                }
            }
            
            this.wiring = wiring.toArray( new NesCWire[ wiring.size() ] );
        }
    }
    
    /**
     * Gets the number of wires, statements of the form "x -&gt y", this configuration
     * has.
     * @return the number of wires
     */
    public int getWireCount(){
        resolveWiring();
        return wiring.length;
    }
    
    /**
     * Gets the index'th wire of this configuration
     * @param index the index of the wire
     * @return the wire
     */
    public NesCWire getWire( int index ){
        resolveWiring();
        return wiring[index];
    }
    
    private void resolveComponents(){
        if( components == null ){
            List<NesCComponentReference> references = new ArrayList<NesCComponentReference>();
            for( ComponentReferenceModelConnection connection : raw.listComponents( bindings.getCancellationMonitor() ) ){
                NesCComponentReference ref = connection.resolve( bindings );
                if( ref != null ){
                    if( replacements != null )
                        ref = ref.replace( replacements );
                        
                    references.add( ref );
                }
            }
            
            components = references.toArray( new NesCComponentReference[ references.size() ] );
        }
    }
    
    /**
     * Gets the number of components which are included in the body
     * through the keyword "components".
     * @return the number of components
     */
    public int getComponentCount(){
        resolveComponents();
        return components.length;
    }
    
    /**
     * Gets a component which is included in the body using the keyword
     * "components".
     * @param index the index of the component
     * @return the component itself
     */
    public NesCComponentReference getComponent( int index ){
        resolveComponents();
        return components[ index ];
    }
    
    @Override
    public NesCConfiguration replace( Map<GenericType, Type> generics ){
        NesCComponent component = super.replace( generics );
        if( component instanceof NesCConfiguration )
            return (NesCConfiguration)component;
        
        return null;
    }
    
    @Override
    protected NesCComponent createComponent( Map<GenericType, Type> generics ){
        return new NesCConfiguration( raw, bindings, generics );
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "configuration [" );
        builder.append( "components={" );
        for( int i = 0, n = getComponentCount(); i<n; i++ ){
            if( i > 0 )
                builder.append( ", " );
            NesCComponentReference ref = getComponent( i );
            if( ref == null || ref.getName() == null )
                builder.append( "null" );
            else
                builder.append( ref.getName() );
        }
        builder.append( "}, wiring={" );
        for( int i = 0, n = getWireCount(); i<n; i++ ){
            if( i > 0 )
                builder.append( ", " );
            builder.append( getWire( i ));
        }
        builder.append( "}]" );
        return builder.toString();
    }
}
