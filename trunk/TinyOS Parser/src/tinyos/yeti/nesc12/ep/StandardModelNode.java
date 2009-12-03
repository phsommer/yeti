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
package tinyos.yeti.nesc12.ep;

import java.io.IOException;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;

public class StandardModelNode extends ModelNode{
    public static final IGenericFactory<StandardModelNode> FACTORY = new ReferenceFactory<StandardModelNode>( ModelNode.FACTORY ){
        public StandardModelNode create(){
            return new StandardModelNode();
        }
        @Override
        public void write( StandardModelNode value, IStorage storage )
        		throws IOException{
        	super.write( value, storage );
        	storage.writeString( value.name );
        }
        @Override
        public StandardModelNode read( StandardModelNode value, IStorage storage )
        		throws IOException{
        	StandardModelNode node = super.read( value, storage );
        	node.setNodeName( storage.readString() );
        	return node;
        }
    };
    
    private String name;
    
    protected StandardModelNode(){
        // nothing
    }
    
    public StandardModelNode( String identifier, boolean leaf, Tag... tags ){
        super( identifier, leaf, tags );
    }

    @Override
    protected void resolveNameRanges(){
        // ignore
    }
    
    public void setNodeName( String name ){
    	this.name = name;
    }
    
    public String getNodeName(){
    	return name;
    }
}
