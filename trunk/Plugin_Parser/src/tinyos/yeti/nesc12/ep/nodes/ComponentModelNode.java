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
import java.util.List;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCComponent;

/**
 * Represents a component like a module or configuration.
 * @author Benjamin Sigg
 */
public abstract class ComponentModelNode extends ModelNode implements Inspectable{
    public static final IGenericFactory<ComponentModelNode> FACTORY = new ReferenceFactory<ComponentModelNode>( ModelNode.FACTORY ){
        public ComponentModelNode create(){
            return null;
        }
        
        @Override
        public void write( ComponentModelNode value, IStorage storage ) throws IOException{
            super.write( value, storage );
            storage.write( value.name );
        }
        
        @Override
        public ComponentModelNode read( ComponentModelNode value, IStorage storage ) throws IOException{
            super.read( value, storage );
            value.name = storage.read();
            return value;
        }
    };
    
    private Name name;
    
    protected ComponentModelNode(){
        // nothing
    }
    
    public ComponentModelNode( Name name, Tag... tags ){
        super( name.toIdentifier(), false );
        this.name = name;
        setLabel( name.toIdentifier() );
        TagSet set = new TagSet();
        for( Tag tag : tags )
            set.add( tag );
        set.add( Tag.COMPONENT );
        set.add( Tag.OUTLINE );
        set.add( Tag.FIGURE );
        set.add( Tag.IDENTIFIABLE );
        setTags( set );
    }
    
    public String getNodeName(){
    	return name.toIdentifier();
    }
    
    public Name getName(){
        return name;
    }
    
    @Override
    protected void resolveNameRanges(){
        name.resolveRange();
    }
    
    public INesCNode inspect( BindingResolver resolver ){
	    return resolve( resolver );
    }
    
    public abstract NesCComponent resolve( BindingResolver bindings );
    
    /**
     * Lists all connections to nodes that are used or provided.
     * @param monitor used to cancel this operation
     * @return the list, may be empty
     */
    public ModelConnection[] listUsesProvides( ICancellationMonitor monitor ){
        for( ModelConnection child : getConnections() ){
            if( child.getTags().contains( ASTModel.SPECIFICATION )){
                ModelNode specification = getDeclarationResolver().resolve( child, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                
                if( specification == null )
                    return new ModelConnection[]{};
                
                List<ModelConnection> result = new ArrayList<ModelConnection>();
                for( ModelConnection check : specification.getConnections() ){
                    if( check.getTags().contains( Tag.USES ) || check.getTags().contains( Tag.PROVIDES ))
                        result.add( check );
                }
                
                return result.toArray( new ModelConnection[ result.size() ] );
            }
        }
        return new ModelConnection[]{};
    }
    
    /**
     * Gets a function or an interface from the uses/provides block
     * that matches the name <code>name</code>.
     * @param name the name to look for
     * @param monitor used to cancel this operation
     * @return the connection or <code>null</code>
     */
    public ModelConnection getUsesProvidesConnection( String name, ICancellationMonitor monitor ){
        for( ModelConnection child : getConnections() ){
            if( child.getTags().contains( ASTModel.SPECIFICATION )){
                ModelNode specification = getDeclarationResolver().resolve( child, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                if( specification == null )
                    return null;
                
                for( ModelConnection check : specification.getConnections() ){
                    if( check.getTags().contains( Tag.FUNCTION ) || check.getTags().contains( Tag.INTERFACE )){
                        if( check instanceof InterfaceReferenceModelConnection ){
                            InterfaceReferenceModelConnection ref = (InterfaceReferenceModelConnection)check;
                            if( ref.getName().toIdentifier().equals( name )){
                                return ref;
                            }
                        }
                        else if( check instanceof FieldModelConnection &&
                                name.equals( ((FieldModelConnection)check).getName() )){
                            return check;
                        }
                    }
                }
                
                return null;
            }
        }
        return null;
    }
    
    /**
     * Gets the reference to the interface with the given renamed name.
     * @param name the renamed name
     * @param monitor used to cancel this operation
     * @return the reference or <code>null</code>
     */
    public InterfaceReferenceModelConnection getUsesProvides( String name, ICancellationMonitor monitor ){
        for( ModelConnection child : getConnections() ){
            if( child.getTags().contains( ASTModel.SPECIFICATION )){
                ModelNode specification = getDeclarationResolver().resolve( child, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                if( specification == null )
                    return null;
                
                for( ModelConnection check : specification.getConnections() ){
                    if( check instanceof InterfaceReferenceModelConnection ){
                        InterfaceReferenceModelConnection ref = (InterfaceReferenceModelConnection)check;
                        if( ref.getName().toIdentifier().equals( name )){
                            return ref;
                        }
                    }
                }
                
                return null;
            }
        }
        return null;
    }
    
    /**
     * Gets the reference to the function with the given name.
     * @param name the name of the function
     * @param monitor used to cancel this operation
     * @return the function or <code>null</code>
     */
    public FieldModelConnection getUsesProvidesFunction( String name, ICancellationMonitor monitor ){
        for( ModelConnection child : getConnections() ){
            if( child.getTags().contains( ASTModel.SPECIFICATION )){
                ModelNode specification = getDeclarationResolver().resolve( child, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                if( specification == null )
                    return null;
                
                for( ModelConnection check : specification.getConnections() ){
                    if( check instanceof FieldModelConnection ){
                        if( name.equals( ((FieldModelConnection)check).getName())){
                            return (FieldModelConnection)check;
                        }
                    }
                }
                
                return null;
            }
        }
        return null;
    }
    
}
