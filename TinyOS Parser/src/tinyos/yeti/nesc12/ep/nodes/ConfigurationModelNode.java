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

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCConfiguration;

public class ConfigurationModelNode extends GenericComponentModelNode{
    public static final IGenericFactory<ConfigurationModelNode> FACTORY = new ReferenceFactory<ConfigurationModelNode>( GenericComponentModelNode.FACTORY ){
        public ConfigurationModelNode create(){
            return new ConfigurationModelNode();
        }
    };
    
    protected ConfigurationModelNode(){
        // nothing
    }
    
    public ConfigurationModelNode( Name name, boolean generic ) {
        super( generic, name, Tag.CONFIGURATION );
    }
    
    /**
     * Gets the reference to the component with the given renamed name.
     * @param name the renamed name
     * @param monitor used to cancel this operation
     * @return the reference
     */
    public ComponentReferenceModelConnection getComponent( String name, ICancellationMonitor monitor ){
        for( ModelConnection connection : getConnections() ){
            if( connection.getTags().contains( ASTModel.IMPLEMENTATION )){
                ModelNode implementation = getDeclarationResolver().resolve( connection, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                if( implementation != null ){
                    for( ModelConnection implementationConnection : implementation.getConnections() ){
                        if( implementationConnection.getTags().contains( ASTModel.COMPONENTS )){
                            ModelNode components = getDeclarationResolver().resolve( implementationConnection, monitor.getProgressMonitor() );
                            monitor.checkCancellation();
                            if( components != null ){
                                for( ModelConnection component : components.getConnections() ){
                                    if( component instanceof ComponentReferenceModelConnection ){
                                        ComponentReferenceModelConnection ref = (ComponentReferenceModelConnection)component;
                                        if( name.equals( ref.getName() ))
                                            return ref;
                                    }
                                }
                            }
                        }
                    }
                }
                
                return null;
            }
        }
        return null;
    }
    
    public ComponentReferenceModelConnection[] listComponents( ICancellationMonitor monitor ){
        for( ModelConnection connection : getConnections() ){
            if( connection.getTags().contains( ASTModel.IMPLEMENTATION )){
                ModelNode implementation = getDeclarationResolver().resolve( connection, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                
                if( implementation != null ){
                    for( ModelConnection implementationConnection : implementation.getConnections() ){
                        if( implementationConnection.getTags().contains( ASTModel.COMPONENTS )){
                            ModelNode components = getDeclarationResolver().resolve( implementationConnection, monitor.getProgressMonitor() );
                            monitor.checkCancellation();
                            if( components != null ){
                                List<ComponentReferenceModelConnection> result = new ArrayList<ComponentReferenceModelConnection>();
                                
                                for( ModelConnection component : components.getConnections() ){
                                    if( component instanceof ComponentReferenceModelConnection ){
                                        ComponentReferenceModelConnection ref = (ComponentReferenceModelConnection)component;
                                        result.add( ref );
                                    }
                                }
                                return result.toArray( new ComponentReferenceModelConnection[ result.size() ] );                            
                            }
                        }
                    }
                }
                
                return new ComponentReferenceModelConnection[]{};
            }
        }
        return new ComponentReferenceModelConnection[]{};
    }
    
    /**
     * Gets a list of references to all connections of this configuration
     * @param monitor used to cancel this operation
     * @return the list of references
     */
    public ModelConnection[] getWiring( ICancellationMonitor monitor ){
        for( ModelConnection connection : getConnections() ){
            if( connection.getTags().contains( ASTModel.IMPLEMENTATION )){
                ModelNode implementation = getDeclarationResolver().resolve( connection, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                if( implementation != null ){
                    for( ModelConnection implementationConnection : implementation.getConnections() ){
                        if( implementationConnection.getTags().contains( ASTModel.CONNECTIONS )){
                            ModelNode connections = getDeclarationResolver().resolve( implementationConnection, monitor.getProgressMonitor() );
                            monitor.checkCancellation();
                            if( connections != null ){
                                List<ModelConnection> result = new ArrayList<ModelConnection>();
                                
                                for( ModelConnection check : connections.getConnections() ){
                                    if( check.getTags().contains( Tag.CONNECTION )){
                                        result.add( check );
                                    }
                                }
                                
                                return result.toArray( new ModelConnection[ result.size() ] );
                            }
                        }
                    }
                }
                
                return new ModelConnection[]{};
            }
        }
        
        return new ModelConnection[]{};
    }

    @Override
    public NesCConfiguration resolve( BindingResolver bindings ) {
        Binding result = bindings.getBinding( getPath(), getIdentifier() );
        if( result == null ){
            result = new NesCConfiguration( this, bindings );
            bindings.putBinding( getPath(), getIdentifier(), result );
        }
        
        if( result instanceof NesCConfiguration )
            return (NesCConfiguration)result;
        
        return null;
    }
}
