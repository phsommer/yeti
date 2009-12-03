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
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterface;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeFactory;

/**
 * Represents an interface like "interface X{...}"
 * @author Benjamin Sigg
 */
public class InterfaceModelNode extends ModelNode implements Inspectable{
    public static final IGenericFactory<InterfaceModelNode> FACTORY = new ReferenceFactory<InterfaceModelNode>( ModelNode.FACTORY ){
        public InterfaceModelNode create(){
            return new InterfaceModelNode();
        }
        
        @Override
        public void write( InterfaceModelNode value, IStorage storage ) throws IOException{
            super.write( value, storage );
            if( value.generics == null ){
                storage.out().writeBoolean( false );
            }
            else{
                storage.out().writeBoolean( true );
                storage.out().writeInt( value.generics.length );
                for( GenericType type : value.generics ){
                    TypeFactory.write( type, storage );
                }
            }
        }
        
        @Override
        public InterfaceModelNode read( InterfaceModelNode value, IStorage storage ) throws IOException{
            super.read( value, storage );
            if( storage.in().readBoolean() ){
                int size = storage.in().readInt();
                value.generics = new GenericType[ size ];
                for( int i = 0; i < size; i++ ){
                    value.generics[i] = (GenericType)TypeFactory.read( storage );
                }
            }
            return value;
        }
    };
    
    private GenericType[] generics;
    
    protected InterfaceModelNode(){
        // nothing
    }
    
    public InterfaceModelNode( String name ){
        super( name, false, Tag.INTERFACE, Tag.OUTLINE, Tag.FIGURE, Tag.IDENTIFIABLE );
        setLabel( name );
    }
    
    public String getNodeName(){
    	return getLabel();
    }
    
    @Override
    protected void resolveNameRanges(){
        if( generics != null ){
            for( Type type : generics ){
                type.resolveNameRanges();
            }
        }
    }
    
    public void setGenerics( GenericType[] generics ){
        this.generics = generics;
        StringBuilder builder = new StringBuilder();
        builder.append( getIdentifier() );
        if( generics != null ){
            builder.append( "<" );
            for( int i = 0, n = generics.length; i<n; i++ ){
                if( i > 0 )
                    builder.append( "," );
                builder.append( generics[i].getName() );
            }
            builder.append( ">" );
        }
        setLabel( builder.toString() );
    }
    
    public GenericType[] getGenerics(){
        return generics;
    }
 
    public INesCNode inspect( BindingResolver resolver ){
	    return resolve( resolver );
    }
    
    public NesCInterface resolve( BindingResolver bindings ){
        NesCInterface interfaze = (NesCInterface)bindings.getBinding( getPath(), "interface" );
        
        if( interfaze == null ){
            interfaze = new NesCInterface( getDeclarationResolver(), bindings, this );
            bindings.putBinding( getPath(), "interface", interfaze );
        }
        return interfaze;
    }
}
