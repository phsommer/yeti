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

import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;

public class NamedType{
    private Name name;
    private Type type;
    private ModelAttribute[] attributes;
    
    public NamedType( Name name, Type type, ModelAttribute[] attributes ){
        this.name = name;
        this.type = type;
        this.attributes = attributes;
    }
    
    public Name getName(){
        return name;
    }
    
    public Type getType(){
        return type;
    }
    
    public ModelAttribute[] getAttributes(){
		return attributes;
	}
    
    @Override
    public String toString(){
        if( name == null && type == null )
            return "named type";
        
        if( name != null )
            return name.toIdentifier();
        
        if( type != null )
            return type.toString();
        
        return null;
    }
}
