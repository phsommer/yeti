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
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.StandardModelConnection;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class DataObjectTypeModelConnection extends StandardModelConnection{
    public static final IGenericFactory<DataObjectTypeModelConnection> FACTORY = 
        new ReferenceFactory<DataObjectTypeModelConnection>( StandardModelConnection.FACTORY ){
      
        public DataObjectTypeModelConnection create(){
            return new DataObjectTypeModelConnection();
        }
    };
    
    protected DataObjectTypeModelConnection(){
        // nothing
    }
    
    public DataObjectTypeModelConnection( DataObjectType data, ASTNode ast ){
        super( data.id( false ), ast );
        
        setReference( true );
        
        TagSet tags = TagSet.get( NesC12ASTModel.TYPE, Tag.DATA_OBJECT );

        String name = data.getName();
        
        if( data.isStruct() ){
            tags.add( Tag.STRUCT );
            if( name == null )
                name = "struct";
        }
        else if( data.isUnion() ){
            tags.add( Tag.UNION );
            if( name == null )
                name = "union";
        }
        
        setLabel( name );

        if( getLabel() == null )
            setLabel( name == null ? "" : name );
        
        setTags( tags );
    }
}
