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
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Generic;
import tinyos.yeti.nesc12.parser.ast.elements.NesCComponentReference;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeFactory;
import tinyos.yeti.nesc12.parser.ast.elements.values.ValueFactory;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class ComponentReferenceModelConnection extends ModelConnection{
    public static final IGenericFactory<ComponentReferenceModelConnection> FACTORY =
        new ReferenceFactory<ComponentReferenceModelConnection>( ModelConnection.FACTORY ){
      
        public ComponentReferenceModelConnection create(){
            return new ComponentReferenceModelConnection();
        }
        
        @Override
        public void write( ComponentReferenceModelConnection value, IStorage storage ) throws IOException{
            super.write( value, storage );
            storage.writeString( value.name );
            storage.writeString( value.rename );
            storage.out().writeBoolean( value.newComponent );
            
            if( value.arguments == null ){
                storage.out().writeBoolean( false );
            }
            else{
                storage.out().writeBoolean( true );
                int size = value.arguments.length;
                storage.out().writeInt( size );
                
                for( Generic check : value.arguments ){
                    if( check instanceof Type ){
                        storage.out().writeInt( 0 );
                        TypeFactory.write( (Type)check, storage );
                    }
                    else if( check instanceof Value ){
                        storage.out().writeInt( 1 );
                        ValueFactory.write( (Value)check, storage );
                    }
                    else{
                        storage.out().writeInt( 2 );
                    }
                }
            }
        }
        
        @Override
        public ComponentReferenceModelConnection read( ComponentReferenceModelConnection value, IStorage storage ) throws IOException{
            super.read( value, storage );
            value.name = storage.readString();
            value.rename = storage.readString();
            value.newComponent = storage.in().readBoolean();
            
            if( storage.in().readBoolean() ){
                int size = storage.in().readInt();
                value.arguments = new Generic[ size ];
                for( int i = 0; i < size; i++ ){
                    int kind = storage.in().readInt();
                    
                    switch( kind ){
                        case 0:
                            value.arguments[i] = TypeFactory.read( storage );
                            break;
                        case 1:
                            value.arguments[i] = ValueFactory.read( storage );
                            break;
                        case 2:
                            break;
                        default:
                            throw new IOException( "unknown kind of generic: " + kind );
                    }
                }
            }
            return value;
        }
    };
    
    private String name;
    private String rename;
    
    private boolean newComponent;
    private Generic[] arguments;
    
    protected ComponentReferenceModelConnection(){
        // nothing
    }
    
    /**
     * Creates a new connection
     * @param name the name of the referenced component
     * @param rename the renamed name, or <code>null</code>
     * @param node the node which created this connection
     * @param newComponent whether this describes a new component
     * @param arguments the arguments for a new component
     */
    public ComponentReferenceModelConnection( String name, String rename, ASTNode node, boolean newComponent, Generic[] arguments ){
        super( name, node );
        if( rename == null )
            setLabel( name );
        else
            setLabel( rename + " (" + name + ")" );
        setReference( true );
        
        TagSet tags = new TagSet();
        tags.add( Tag.COMPONENT );
        if( rename != null )
            tags.add( Tag.RENAMED );
        tags.add( Tag.AST_CONNECTION_ICON_RESOLVE );
        tags.add( Tag.AST_CONNECTION_LABEL_RESOLVE );
        tags.add( Tag.AST_CONNECTION_GRAPH_LABEL_RESOLVE );
        setTags( tags );
        
        this.name = name;
        this.rename = rename;
        
        this.newComponent = newComponent;
        this.arguments = arguments;
    }
    
    @Override
    protected void resolveNameRanges(){
        if( arguments != null ){
            for( Generic argument : arguments ){
                if( argument != null ){
                    argument.resolveNameRanges();
                }
            }
        }
    }
    
    public boolean isNewComponent() {
        return newComponent;
    }
    
    public Generic[] getArguments() {
        return arguments;
    }
    
    public String getReference(){
        return name;
    }
    
    public String getRename(){
        return rename;
    }
    
    public String getName(){
        if( rename == null )
            return name;
        else
            return rename;
    }
    
    public NesCComponentReference resolve( BindingResolver bindings ){
        String id = "component.reference." + getIdentifier();
        Binding result = bindings.getBinding( getPath(), id );
        if( result == null ){
            result = new NesCComponentReference( this, bindings );
            bindings.getBinding( getPath(), id );
        }
        if( result instanceof NesCComponentReference )
            return (NesCComponentReference)result;
        
        return null;
    }
}
