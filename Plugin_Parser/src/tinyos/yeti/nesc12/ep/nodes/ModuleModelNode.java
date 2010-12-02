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

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCModule;

public class ModuleModelNode extends GenericComponentModelNode{
    public static final IGenericFactory<ModuleModelNode> FACTORY = new ReferenceFactory<ModuleModelNode>( GenericComponentModelNode.FACTORY ){
        public ModuleModelNode create(){
            return new ModuleModelNode();
        }
    };
    
    protected ModuleModelNode(){
        // nothing
    }
    
    public ModuleModelNode( boolean generic, Name name ){
        super( generic, name, Tag.MODULE );
    }

    @Override
    public NesCModule resolve( BindingResolver bindings ){
        NesCModule module = (NesCModule)bindings.getBinding( getPath(), getIdentifier() );
        if( module == null ){
            module = new NesCModule( this, bindings );
            bindings.putBinding( getPath(), getIdentifier(), module );
        }
        return module;
    }
    
    
    /**
     * Gets a list of connections to the content of the implementation-block of 
     * this module.
     * @param monitor used to cancel this operation
     * @return the connection
     */
    public ModelConnection[] listImplementation( ICancellationMonitor monitor ){
        for( ModelConnection connection : getConnections() ){
            if( connection.getTags().contains( ASTModel.IMPLEMENTATION )){
                ModelNode implementation = getDeclarationResolver().resolve( connection, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                if( implementation == null )
                    return new ModelConnection[]{};
                
                return implementation.getConnections();
            }
        }
        
        return new ModelConnection[]{};
    }
    
    public FieldModelConnection searchFunction( String name, ICancellationMonitor monitor ){
        ModelConnection[] connection = listImplementation( monitor );
        monitor.checkCancellation();
        
        for( ModelConnection check : connection ){
            if( check instanceof FieldModelConnection ){
                TagSet tags = check.getTags();
                if( tags != null && tags.contains( Tag.FUNCTION )){
                    FieldModelConnection field = (FieldModelConnection)check;
                    if( name.equals( field.getName() ))
                        return field;
                }
            }
        }
        return null;
    }
}
