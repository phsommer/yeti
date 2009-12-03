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
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeFactory;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeUtility;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypedefType;

public class TypedefModelNode extends ModelNode{
    public static final IGenericFactory<TypedefModelNode> FACTORY = new ReferenceFactory<TypedefModelNode>( ModelNode.FACTORY ){
        public TypedefModelNode create(){
            return new TypedefModelNode();
        }
        
        @Override
        public void write( TypedefModelNode value, IStorage storage ) throws IOException{
            super.write( value, storage );
            storage.write( value.name );
            TypeFactory.write( value.type, storage );
        }
        
        @Override
        public TypedefModelNode read( TypedefModelNode value, IStorage storage ) throws IOException{
            super.read( value, storage );
            value.name = storage.read();
            value.type = TypeFactory.read( storage ).asTypedefType();
            return value;
        }
    };
    
    private Name name;
    private TypedefType type;
    
    protected TypedefModelNode(){
        // nothing
    }
    
    public TypedefModelNode( Name name, TypedefType type ){
        super( name.toIdentifier(), false, NesC12ASTModel.TYPEDEF, NesC12ASTModel.TYPE, Tag.NO_BASE_EXPANSION, Tag.IDENTIFIABLE );
    
        this.name = name;
        this.type = type;
        
        setLabel( TypeUtility.toAstNodeLabel( type ) );
    }
    
    @Override
    protected void resolveNameRanges(){
        if( name != null )
            name.resolveRange();
        
        if( type != null )
            type.resolveNameRanges();
    }
    
    public String getNodeName(){
    	if( name == null )
    		return null;
    	return name.toIdentifier();
    }
    
    public Name getName(){
        return name;
    }
    
    public TypedefType getType(){
        return type;
    }
}
