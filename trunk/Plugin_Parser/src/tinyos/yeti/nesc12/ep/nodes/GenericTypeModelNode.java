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
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;

/**
 * Represents the statement "typedef name" that can be in the parameter
 * list of a generic component.
 * @author Benjamin Sigg
 */
public class GenericTypeModelNode extends StandardModelNode{
    public static final IGenericFactory<GenericTypeModelNode> FACTORY = new ReferenceFactory<GenericTypeModelNode>( StandardModelNode.FACTORY ){
        public GenericTypeModelNode create(){
            return new GenericTypeModelNode();
        }
    };
    
    protected GenericTypeModelNode(){
        // nothing
    }
    
    public GenericTypeModelNode( String identifier ){
        super( identifier, true, NesC12ASTModel.TYPEDEF, NesC12ASTModel.TYPE, Tag.IDENTIFIABLE );
        setLabel( identifier );
        setNodeName( identifier );
    }
    
    public GenericType resolve(){
        return new GenericType( getIdentifier() );
    }
}
