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

import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.NesCEndpoint;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.values.ValueFactory;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

/**
 * A connection pointing to an endpoint.
 * @author Benjamin Sigg
 */
public class EndpointModelConnection extends ModelConnection{
    public static final IGenericFactory<EndpointModelConnection> FACTORY = new ReferenceFactory<EndpointModelConnection>( ModelConnection.FACTORY ){
        public EndpointModelConnection create(){
            return new EndpointModelConnection();
        }
        
        @Override
        public void write( EndpointModelConnection value, IStorage storage ) throws IOException{
            super.write( value, storage );
            
            storage.write( value.component );
            storage.write( value.reference );
            
            storage.writeString( value.componentName );
            storage.writeString( value.specificatonName );
            
            if( value.index == null ){
                storage.out().writeInt( -1 );
            }
            else{
                int size = value.index.length;
                storage.out().writeInt( size );
                for( int i = 0; i < size; i++ ){
                    ValueFactory.write( value.index[i], storage );
                }
            }
            
            storage.out().writeBoolean( value.implicit );
            storage.out().writeBoolean( value.intern );
        }
        
        @Override
        public EndpointModelConnection read( EndpointModelConnection value, IStorage storage ) throws IOException{
            super.read( value, storage );
            
            value.component = storage.read();
            value.reference = storage.read();
            
            value.componentName = storage.readString();
            value.specificatonName = storage.readString();
            
            int size = storage.in().readInt();
            if( size >= 0 ){
                value.index = new Value[ size ];
                for( int i = 0; i < size; i++ ){
                    value.index[i] = ValueFactory.read( storage );
                }
            }
            
            value.implicit = storage.in().readBoolean();
            value.intern = storage.in().readBoolean();
            
            return value;
        }
    };
    
    private ModelConnection component;
    private ModelConnection reference;
    
    private String componentName;
    private String specificatonName;
    
    private Value[] index;
    private boolean implicit;
    private boolean intern;
    
    protected EndpointModelConnection(){
        // nothing
    }
    
    /**
     * Creates a new connection
     * @param component the component to which this connection points, may be <code>null</code>
     * @param reference the connection to which this connection points indirectly
     * @param node the node which created this connection
     * @param componentName in case of an endpoint of the form "x.y", "x"
     * @param specificationName in case of an endpoint of the form "x.y", "y". Otherwise <code>null</code>.
     * @param index parameters for arrayed references
     * @param implicit whether this endpoint was implicit or not
     * @param intern whether this endpoint refers to an element from the uses/provides list or not
     */
    public EndpointModelConnection( 
    		ModelConnection component,
            ModelConnection reference,
            ASTNode node,
            String componentName, String specificationName, 
            Value[] index, boolean implicit, boolean intern ){
        
        super( reference.getIdentifier(), node );
        if( specificationName == null )
            setLabel( componentName );
        else 
            setLabel( componentName + "." + specificationName );
        
        this.component = component;
        this.reference = reference;
        this.componentName = componentName;
        this.specificatonName = specificationName;
        this.index = index;
        this.implicit = implicit;
        this.intern = intern;
    }
    
    @Override
    public void setDeclarationResolver( DeclarationResolver declarationResolver ){
    	super.setDeclarationResolver( declarationResolver );
    	if( reference != null ){
    		reference.setDeclarationResolver( declarationResolver );
    	}
    }
    
    @Override
    protected void resolveNameRanges(){
        if( index != null ){
            for( Value value : index )
                if( value != null )
                    value.resolveNameRanges();
        }
    }
    
    public ModelConnection getReference() {
        return reference;
    }
    
    public ModelConnection getComponentReference(){
    	return component;
    }
    
    public String getComponent() {
        return componentName;
    }
    
    public String getSpecificaton() {
        return specificatonName;
    }
    
    public Value[] getIndex() {
        return index;
    }
    
    public boolean isImplicit(){
        return implicit;
    }
    
    public boolean isIntern(){
        return intern;
    }
    
    public String getName(){
        if( reference instanceof InterfaceReferenceModelConnection ){
            return ((InterfaceReferenceModelConnection)reference).getName().toIdentifier();
        }
        if( reference instanceof FieldModelConnection ){
            return ((FieldModelConnection)reference).getIdentifier();
        }
        return null;
    }
    
    public NesCEndpoint resolve( BindingResolver bindings ){
        String id = "connection." + getIdentifier() + " - " + componentName;
        if( specificatonName != null )
            id += "." + specificatonName;
        
        Binding result = bindings.getBinding( getPath(), id );
        if( result == null ){
            result = new NesCEndpoint( this, bindings );
            bindings.putBinding( getPath(), id, result );
        }
        if( result instanceof NesCEndpoint )
            return (NesCEndpoint)result;
        
        return null;
    }
}
