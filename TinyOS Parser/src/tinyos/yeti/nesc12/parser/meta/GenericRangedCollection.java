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
package tinyos.yeti.nesc12.parser.meta;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.meta.GenericRangedCollector.GenericValue;

public class GenericRangedCollection extends RangedCollection<GenericRangedCollector.GenericValue>{

    public List<Field> getFields( int location ){
        return get( GenericRangedCollector.KIND_FIELD, location );
    }
    
    public List<ComponentReferenceModelConnection> getComponentReferences( int location ){
        return get( GenericRangedCollector.KIND_COMPONENT_REFERENCE, location );
    }
    
    public List<InterfaceReferenceModelConnection> getInterfaceReferences( int location ){
        return get( GenericRangedCollector.KIND_INTERFACE_REFERENCE, location );
    }
    
    public List<NamedType> getTypeTags( int location ){
        return get( GenericRangedCollector.KIND_TYPE_TAG, location );
    }
    
    @SuppressWarnings("unchecked")
    private <V> List<V> get( final int kind, int location ){
        final List<V> result = new ArrayList<V>();
        visit( location, new Visitor<GenericRangedCollector.GenericValue>(){
            public boolean visit( GenericValue value ){
                if( value.getKind() == kind ){
                    result.add( (V)value.getValue() );
                }
                return true;
            }
        });
        
        return result;
    }
}
