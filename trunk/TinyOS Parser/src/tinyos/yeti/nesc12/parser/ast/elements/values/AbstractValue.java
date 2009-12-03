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
package tinyos.yeti.nesc12.parser.ast.elements.values;

import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;

/**
 * A value implementing the most common methods of {@link Value}
 * @author Benjamin Sigg
 */
public abstract class AbstractValue implements Value{
    public Type asType() {
        return null;
    }

    public Value asValue() {
        return this;
    }

    public Binding getSegmentChild( int segment, int index ) {
        return getType();
    }

    public int getSegmentCount() {
        return 1;
    }

    public String getSegmentName( int segment ) {
        return "Type";
    }

    public int getSegmentSize( int segment ) {
        return 1;
    }
}
