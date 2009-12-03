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

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.inspection.INesCBinaryComponent;
import tinyos.yeti.ep.parser.inspection.INesCComponent;
import tinyos.yeti.ep.parser.inspection.INesCConfiguration;
import tinyos.yeti.ep.parser.inspection.INesCField;
import tinyos.yeti.ep.parser.inspection.INesCFunction;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCInterface;
import tinyos.yeti.ep.parser.inspection.INesCModule;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.inspection.INesCTypedef;
import tinyos.yeti.ep.parser.inspection.ITranslationUnit;
import tinyos.yeti.ep.parser.inspection.InspectionKey;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.nodes.ComponentModelNode;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceModelNode;
import tinyos.yeti.nesc12.ep.nodes.TypedefModelNode;
import tinyos.yeti.nesc12.ep.nodes.UnitModelNode;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypedefType;

/**
 * Represents a while file that got parsed.
 * @author Benjamin Sigg
 */
public class Unit extends AbstractNesCBinding implements ITranslationUnit{
    private UnitModelNode unit;
    private ModelNode[] nodes;
    private BindingResolver bindings;
    
    private InspectionKey<?>[] keys = {
    		COMPONENT, MODULE, CONFIGURATION, BINARY_COMPONENT,
    		INTERFACE,
    		FIELD, FUNCTION, TYPEDEF
    };
    
    private NesCComponent[] components;
    private NesCInterface[] interfaces;
    private Field[] fields;
    private TypedefType[] typedefs;
    
    public Unit( UnitModelNode unit, BindingResolver bindings ){
        super( "Unit" );
        this.bindings = bindings;
        this.unit = unit;
    }
    
    public IASTModelNodeConnection asConnection(){
    	return null;
    }
    
    public IASTModelNode asNode(){
    	return unit;
    }
    
    public int getReferenceKindCount(){
    	return keys.length;
    }
    
    public InspectionKey<?> getReferenceKind( int index ){
    	return keys[ index ];
    }
    
    @SuppressWarnings("unchecked")
	public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector ){
    	if( COMPONENT.equals( key ) ){
    		resolveComponents();
    		INesCComponent[] result = new INesCComponent[ components.length ];
    		System.arraycopy( components, 0, result, 0, components.length );
    		return (K[])result;
    	}
    	if( MODULE.equals( key )){
    		resolveComponents();
    		List<INesCModule> result = new ArrayList<INesCModule>();
    		for( NesCComponent component : components ){
    			if( component instanceof INesCModule ){
    				result.add( (INesCModule)component );
    			}
    		}
    		return (K[])result.toArray( new INesCModule[ result.size() ] );
    	}
    	if( CONFIGURATION.equals( key )){
    		resolveComponents();
    		List<INesCConfiguration> result = new ArrayList<INesCConfiguration>();
    		for( NesCComponent component : components ){
    			if( component instanceof INesCConfiguration ){
    				result.add( (INesCConfiguration)component );
    			}
    		}
    		return (K[])result.toArray( new INesCConfiguration[ result.size() ] );    		
    	}
    	if( BINARY_COMPONENT.equals( key )){
    		resolveComponents();
    		List<INesCBinaryComponent> result = new ArrayList<INesCBinaryComponent>();
    		for( NesCComponent component : components ){
    			if( component instanceof INesCBinaryComponent ){
    				result.add( (INesCBinaryComponent)component );
    			}
    		}
    		return (K[])result.toArray( new INesCBinaryComponent[ result.size() ] );
    	}
    	if( INTERFACE.equals( key )){
    		resolveInterfaces();
    		INesCInterface[] result = new INesCInterface[ interfaces.length ];
    		System.arraycopy( interfaces, 0, result, 0, interfaces.length );
    		return (K[])result;
    	}
    	if( FIELD.equals( key )){
    		resolveFields();
    		List<INesCField> result = new ArrayList<INesCField>();
    		for( Field field : fields ){
    			if( field.isField() ){
    				result.add( field );
    			}
    		}
    		return (K[])result.toArray( new INesCField[ result.size() ] );
    	}
    	if( FUNCTION.equals( key )){
    		resolveFields();
    		List<INesCFunction> result = new ArrayList<INesCFunction>();
    		for( Field field : fields ){
    			if( field.isFunction() ){
    				result.add( field );
    			}
    		}
    		return (K[])result.toArray( new INesCFunction[ result.size() ] );
    	}
    	if( TYPEDEF.equals( key )){
    		resolveTypedefs();
    		INesCTypedef[] result = new INesCTypedef[ typedefs.length ];
    		System.arraycopy( typedefs, 0, result, 0, typedefs.length );
    		return (K[])result;
    	}
    	return null;
    }
    
    public int getSegmentCount() {
        return 4;
    }
    
    public String getSegmentName( int segment ) {
        switch( segment ){
            case 0: return "Components";
            case 1: return "Interfaces";
            case 2: return "Fields";
            case 3: return "Typedefs";
            default: return null;
        }
    }
    
    public int getSegmentSize( int segment ) {
        switch( segment ){
            case 0: return getComponentCount();
            case 1: return getInterfaceCount();
            case 2: return getFieldCount();
            case 3: return getTypedefCount();
            default: return 0;
        }
    }
    
    public Binding getSegmentChild( int segment, int index ) {
        switch( segment ){
            case 0: return getComponent( index );
            case 1: return getInterface( index );
            case 2: return getField( index );
            case 3: return getTypedef( index );
            default: return null;
        }
    }
    
    private void resolveNodes(){
        if( nodes == null ){
            List<ModelNode> nodes = new ArrayList<ModelNode>();
            ICancellationMonitor monitor = bindings.getCancellationMonitor();
            for( ModelConnection connection : unit.getConnections() ){
                ModelNode node = unit.getDeclarationResolver().resolve( connection, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                
                if( node != null ){
                    nodes.add( node );
                }
            }
            this.nodes = nodes.toArray( new ModelNode[ nodes.size() ] );
        }
    }
    
    private void resolveComponents(){
        if( components == null ){
            resolveNodes();
            List<NesCComponent> list = new ArrayList<NesCComponent>();
            if( nodes != null ){
                for( IASTModelNode node : nodes ){
                    if( node instanceof ComponentModelNode ){
                        NesCComponent component = ((ComponentModelNode)node).resolve( bindings );
                        if( component != null )
                            list.add( component );
                    }
                }
            }
            components = list.toArray( new NesCComponent[ list.size() ] );
        }
    }
    
    public int getComponentCount(){
        resolveComponents();
        return components.length;
    }
    
    public NesCComponent getComponent( int index ){
        resolveComponents();
        return components[ index ];
    }
    
    
    private void resolveInterfaces(){
        if( interfaces == null ){
            resolveNodes();
            List<NesCInterface> list = new ArrayList<NesCInterface>();
            if( nodes != null ){
                for( IASTModelNode node : nodes ){
                    if( node instanceof InterfaceModelNode ){
                        NesCInterface interfaze = ((InterfaceModelNode)node).resolve( bindings );
                        if( interfaze != null )
                            list.add( interfaze );
                    }
                }
            }
            interfaces = list.toArray( new NesCInterface[ list.size() ] );
        }
    }
    
    public int getInterfaceCount(){
        resolveInterfaces();
        return interfaces.length;
    }
    
    public NesCInterface getInterface( int index ){
        resolveInterfaces();
        return interfaces[ index ];
    }
    
    private void resolveFields(){
        if( fields == null ){
            resolveNodes();
            List<Field> list = new ArrayList<Field>();
            if( nodes != null ){
                for( IASTModelNode node : nodes ){
                    if( node instanceof FieldModelNode ){
                        list.add( (FieldModelNode)node );
                    }
                }
            }
            fields = list.toArray( new Field[ list.size() ] );
        }
    }
    
    public int getFieldCount(){
        resolveFields();
        return fields.length;
    }
    
    public Field getField( int index ){
        resolveFields();
        return fields[ index ];
    }
    
    private void resolveTypedefs(){
        if( typedefs == null ){
            resolveNodes();
            List<TypedefType> list = new ArrayList<TypedefType>();
            if( nodes != null ){
                for( IASTModelNode node : nodes ){
                    if( node instanceof TypedefModelNode ){
                        list.add( ((TypedefModelNode)node).getType() );
                    }
                }
            }
            typedefs = list.toArray( new TypedefType[ list.size() ] );
        }
    }
    
    public int getTypedefCount(){
        resolveTypedefs();
        return typedefs.length;
    }
    
    public TypedefType getTypedef( int index ){
        resolveTypedefs();
        return typedefs[ index ];
    }
}
