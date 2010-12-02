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
package tinyos.yeti.nesc12.parser.ast.util.pushers;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;

public class NesCFieldPusher extends StandardFieldPusher{
    public NesCFieldPusher( String name, AnalyzeStack stack ){
        super( name, stack );
    }
    
    /**
     * Ensures that the fields are functions
     */
    protected void checkIsFunctionType(){
        List<Name> names = new ArrayList<Name>();
        for( Definition definition : definitions ){
            if( !definition.typedef ){
                Field field = definition.getField();
                if( field != null ){
                    FieldModelNode node = field.asNode();
                    if( node != null && (node.getTags().contains( Tag.USES ) || node.getTags().contains( Tag.PROVIDES ))){

                        Type type = definition.type();
                        if( type == null || type.asFunctionType() == null ){
                            names.add( definition.getName() );
                        }
                    }
                }
            }
        }
        
        if( names.size() > 0 ){
            error( "'" + name + "' must have a function type", names );
        }
    }
    
    /**
     * Checks whether the field is defined only once in the uses/provides clause.
     * @return the definition of the field
     */
    protected Definition checkDefinedOnlyOnce(){
        Definition result = null;
        List<Name> names = new ArrayList<Name>();
        
        for( Definition definition : definitions ){
            if( !definition.typedef ){
                Field field = definition.getField();
                if( field != null ){
                    FieldModelNode node = field.asNode();
                    if( node != null ){
                        if( node.getTags().contains( Tag.USES ) || node.getTags().contains( Tag.PROVIDES )){
                        	Name name = node.getName();
                        	if( name != null ){
                        		names.add( name );
                        	}
                            result = definition;
                        }
                    }
                }
            }
        }
        
        if( names.size() > 1 ){
            error( "multiple definitions of command/event '" + name + "'", names );
            return null;
        }
        
        return result;
    }
}
