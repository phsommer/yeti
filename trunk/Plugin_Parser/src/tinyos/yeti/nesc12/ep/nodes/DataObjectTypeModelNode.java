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
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;

public class DataObjectTypeModelNode extends StandardModelNode{
    public static final IGenericFactory<DataObjectTypeModelNode> FACTORY = new ReferenceFactory<DataObjectTypeModelNode>( StandardModelNode.FACTORY ){
        public DataObjectTypeModelNode create(){
            return new DataObjectTypeModelNode();
        }
    };
    
    protected DataObjectTypeModelNode(){
        // nothing
    }
    
    public DataObjectTypeModelNode( String name, Type type ){
        super( type.id( false ), false );
        setTags( getTags( type ) );
        getTags().add( Tag.IDENTIFIABLE );
        
        DataObjectType data = type.asDataObjectType();
        if( data != null && name == null ){
            if( data.isStruct() ){
                name = "struct";
            }
            else if( data.isUnion() ){
                name = "union";
            }
            
            setLabel( name );
        }

        if( getLabel() == null )
            setLabel( name == null ? "" : name );
        
        setNodeName( getLabel() );
    }
    
    public static TagSet getTags( Type type ){
        DataObjectType data = type.asDataObjectType();
        TagSet tags = TagSet.get( Tag.DATA_OBJECT );
        if( data != null ){
            if( data.isStruct() ){
                tags.add( Tag.STRUCT );
            }
            else if( data.isUnion() ){
                tags.add( Tag.UNION );
            }
        }
        return tags;
    }
}
