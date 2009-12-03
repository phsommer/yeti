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

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.StandardBindingResolver;
import tinyos.yeti.nesc12.ep.StandardModelNode;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Unit;

public class UnitModelNode extends StandardModelNode{
    public static final IGenericFactory<UnitModelNode> FACTORY = new ReferenceFactory<UnitModelNode>( StandardModelNode.FACTORY ){
        public UnitModelNode create(){
            return new UnitModelNode();
        }
    };
    
    protected UnitModelNode(){
        // nothing
    }
    
    public UnitModelNode( IParseFile parseFile, int fileLength ){
        super( parseFile.getPath(), false, NesC12ASTModel.UNIT );
        setLabel( parseFile.getPath() );
        setNodeName( getLabel() );
        addRegion( new FileRegion( parseFile, 0, fileLength, 0 ) );
    }
    
    public Unit resolve(){
        return resolve( new StandardBindingResolver() );
    }
    
    public Unit resolve( BindingResolver bindings ){
        Binding result = bindings.getBinding( getPath(), "unit" );
        if( result == null ){
            result = new Unit( this, bindings );
            bindings.putBinding( getPath(), "unit", result );
        }
        if( result instanceof Unit )
            return (Unit)result;
        
        return null;
    }
}
