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

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.Name;

/**
 * Represents a component that can be generic.
 * @author Benjamin Sigg
 */
public abstract class GenericComponentModelNode extends ComponentModelNode{
    public static final IGenericFactory<GenericComponentModelNode> FACTORY = new ReferenceFactory<GenericComponentModelNode>( ComponentModelNode.FACTORY ){
        public GenericComponentModelNode create(){
            return null;
        }
        
        @Override
        public void write( GenericComponentModelNode value, IStorage storage ) throws IOException{
            super.write( value, storage );
            storage.out().writeBoolean( value.generic );
        }
        
        @Override
        public GenericComponentModelNode read( GenericComponentModelNode value, IStorage storage ) throws IOException{
            super.read( value, storage );
            value.generic = storage.in().readBoolean();
            return value;
        }
    };
    
    private boolean generic;
    
    protected GenericComponentModelNode(){
        // nothing
    }
    
    public GenericComponentModelNode(  boolean generic, Name name, Tag... tags ){
        super( name, tags );
        this.generic = generic;
        if( generic ){
            getTags().add( NesC12ASTModel.GENERIC );
        }
    }
    
    public boolean isGeneric(){
        return generic;
    }
    
    /**
     * Gets all the connections to nodes that are in the parameter list of
     * this generic module.
     * @param monitor used to cancel this operation
     * @return the parameters
     */
    public ModelConnection[] listParameters( ICancellationMonitor monitor ){
        for( ModelConnection connection : getConnections() ){
            if( connection.getTags().contains( NesC12ASTModel.PARAMETERS )){
                ModelNode parameters = getDeclarationResolver().resolve( connection, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                
                if( parameters == null )
                    return new ModelConnection[]{};
                
                return parameters.getConnections();
            }
        }
        
        return new ModelConnection[]{};
    }
}
