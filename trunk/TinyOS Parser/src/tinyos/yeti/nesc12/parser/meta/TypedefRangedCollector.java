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
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.preprocessor.RangeDescription;

public class TypedefRangedCollector extends RangedCollector<String,NamedType>{
    public TypedefRangedCollector( AnalyzeStack stack ) {
        super( stack );
    }

    public void typedef( Name name, Type type, ModelAttribute[] attributes, int top ){
        RangeDescription range = name.getRange();
        String value = name.toIdentifier();
        
        if( range == null )
            active( 0, value, new NamedType( name, type, attributes ), top );
        else
            active( range.getLeft(), value, new NamedType( name, type, attributes ), top );
    }
    
    public void notypedef( Name name, int top ){
        RangeDescription range = name.getRange();
        String value = name.toIdentifier();
        notactive( range.getLeft(), value, top );
    }
}
