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
import tinyos.yeti.nesc12.ep.StandardModelConnection;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeFactory;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class FieldModelConnection extends StandardModelConnection{
    public static final IGenericFactory<FieldModelConnection> FACTORY = 
        new ReferenceFactory<FieldModelConnection>( StandardModelConnection.FACTORY ){
        
        public FieldModelConnection create(){
            return new FieldModelConnection();
        }
        
        @Override
        public void write( FieldModelConnection value, IStorage storage ) throws IOException{
            super.write( value, storage );
            TypeFactory.write( value.type, storage );
            storage.writeString( value.name );
        }
        
        @Override
        public FieldModelConnection read( FieldModelConnection value, IStorage storage ) throws IOException{
            super.read( value, storage );
            value.type = TypeFactory.read( storage );
            value.name = storage.readString();
            return value;
        }
    };
    
    private Type type;
    private String name;
    
    protected FieldModelConnection(){
        // nothing
    }
    
    public FieldModelConnection( FieldModelNode node, ASTNode ast ){
        super( node.getIdentifier(), ast );
        setLabel( node.getLabel() );
        setTags( node.getTags() );
        
        type = node.getType();
        Name name = node.getName();
        if( name != null )
            this.name = name.toIdentifier();
    }
    
    public Type getType(){
        return type;
    }
    
    public String getName(){
        return name;
    }
    
}
