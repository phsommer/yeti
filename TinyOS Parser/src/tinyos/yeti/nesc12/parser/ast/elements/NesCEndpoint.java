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

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.nodes.EndpointModelConnection;
import tinyos.yeti.nesc12.ep.nodes.FieldModelConnection;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;

/**
 * An endpoint is one end of a wire. In "x -> y", "x" and "y" would both 
 * be endpoints. Syntactically an endpoint can point to a copmonent, interface
 * or a function. Semantically it is always pointing to an interface or function.
 * @author Benjamin Sigg
 *
 */
public class NesCEndpoint extends AbstractBinding {
    private EndpointModelConnection endpoint;
    private BindingResolver bindings;
    
    private Value[] parameters;
    
    private UsedBinding used;
    private ProvidedBinding provided;
    
    public NesCEndpoint( EndpointModelConnection endpoint, BindingResolver bindings ){
        super( "Endpoint" );
        this.endpoint = endpoint;
        this.bindings = bindings;
    }
    
    public int getSegmentCount() {
        return 4;
    }
    
    public String getSegmentName( int segment ) {
        switch( segment ){
            case 0: return "reference";
            case 1: return "parameter";
            case 2: return "used";
            case 3: return "provided";
            default: return null;
        }
    }
    
    public int getSegmentSize( int segment ) {
        switch( segment ){
            case 0: return 1;
            case 1: return getParameterCount();
            case 2: return 1;
            case 3: return 1;
            default: return 0;
        }
    }
    
    public Binding getSegmentChild( int segment, int index ) {
        switch( segment ){
            case 0: return getReference();
            case 1: return getParameter( index );
            case 2: return used == null ? used = new UsedBinding() : used;
            case 3: return provided == null ? provided = new ProvidedBinding() : provided;
            default: return null;
        }
    }
    
    public Binding getReference(){
        ModelConnection reference = endpoint.getReference();
        if( reference instanceof InterfaceReferenceModelConnection ){
            return ((InterfaceReferenceModelConnection)reference).resolve( bindings );
        }
        if( reference instanceof FieldModelConnection ){
            ICancellationMonitor monitor = bindings.getCancellationMonitor();
            ModelNode field = endpoint.getDeclarationResolver().resolve( reference, monitor.getProgressMonitor() );
            monitor.checkCancellation();
            
            if( field instanceof FieldModelNode ){
                return (FieldModelNode)field;
            }
        }
        return null;
    }
    
    public NesCInterfaceReference getInterface(){
        ModelConnection reference = endpoint.getReference();
        if( reference instanceof InterfaceReferenceModelConnection ){
            return ((InterfaceReferenceModelConnection)reference).resolve( bindings );
        }
        else{
            return null;
        }
    }
    
    public Field getFunction(){
        ModelConnection reference = endpoint.getReference();
        if( reference instanceof FieldModelConnection ){
            ICancellationMonitor monitor = bindings.getCancellationMonitor();
            ModelNode field = endpoint.getDeclarationResolver().resolve( reference, monitor.getProgressMonitor() );
            monitor.checkCancellation();
            
            if( field instanceof FieldModelNode ){
                return (FieldModelNode)field;
            }
        }
        
        return null;
    }
    
    public boolean isUsed(){
        return endpoint.getTags().contains( Tag.USES );
    }
    
    public boolean isProvided(){
        return endpoint.getTags().contains( Tag.PROVIDES );
    }
    
    private void resolveParameters(){
        if( parameters == null ){
            parameters = endpoint.getIndex();
            if( parameters == null )
                parameters = new Value[]{};
        }
    }
    
    public boolean hasParameters(){
        return endpoint.getIndex() != null;
    }
    
    public int getParameterCount(){
        resolveParameters();
        return parameters.length;
    }
    
    public Value getParameter( int index ){
        resolveParameters();
        return parameters[ index ];
    }
    
    public String getComponentName(){
        return endpoint.getComponent();
    }
    
    public String getSpecificationName(){
        return endpoint.getSpecificaton();
    }
    
    public IASTModelPath getPath(){
        return endpoint.getPath();
    }
    
    public boolean isImplicit(){
        return endpoint.isImplicit();
    }
    
    public boolean isExplicit(){
        return !isImplicit();
    }
    
    public boolean isIntern(){
        return endpoint.isIntern();
    }
    
    public boolean isExtern(){
        return !isIntern();
    }
    
    /**
     * Gets the name of the referenced interface or function as used
     * in the component.
     * @return the name or the renamed name
     */
    public String getName(){
        return endpoint.getName();
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append( getComponentName() );
        if( getSpecificationName() != null ){
            builder.append( "." );
            builder.append( getSpecificationName() );
        }
        
        if( hasParameters() ){
            builder.append( "[" );
            for( int i = 0, n = getParameterCount(); i<n; i++ ){
                if( i > 0 )
                    builder.append( ", " );
                builder.append( getParameter( i ) );
            }
            builder.append( "]" );
        }
        return builder.toString();
    }
    
    private class UsedBinding extends AbstractBinding{
        public UsedBinding(){
            super( "boolean" );
        }

        @Override
        public String getBindingValue() {
            return String.valueOf( isUsed() );
        }
        
        public Binding getSegmentChild( int segment, int index ) {
            return null;
        }

        public int getSegmentCount() {
            return 0;
        }

        public String getSegmentName( int segment ) {
            return null;
        }

        public int getSegmentSize( int segment ) {
            return 0;
        }        
    }
    

    private class ProvidedBinding extends AbstractBinding{
        public ProvidedBinding(){
            super( "boolean" );
        }

        @Override
        public String getBindingValue() {
            return String.valueOf( isProvided() );
        }
        
        public Binding getSegmentChild( int segment, int index ) {
            return null;
        }

        public int getSegmentCount() {
            return 0;
        }

        public String getSegmentName( int segment ) {
            return null;
        }

        public int getSegmentSize( int segment ) {
            return 0;
        }        
    }

}
