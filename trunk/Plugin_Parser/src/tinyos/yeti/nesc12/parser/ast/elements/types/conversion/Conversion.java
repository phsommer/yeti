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
package tinyos.yeti.nesc12.parser.ast.elements.types.conversion;

import tinyos.yeti.nesc12.parser.ast.elements.Type;

/**
 * A conversion is reponsible to check some kind of conversion of {@link Type}s.
 * @author Benjamin Sigg
 */
public interface Conversion{
    /**
     * Tells whether this conversion is responsible to handle <code>source</code>
     * to <code>destination</code> conversions.
     * @param source the source type
     * @param destination the destination type
     * @return <code>true</code> if this conversion applies
     */
    public boolean responsible( Type source, Type destination );
    
    /**
     * Checks the validity of the conversion from <code>source</code>
     * to <code>destination</code>.
     * @param source the source type
     * @param destination the destination type
     * @param table additional information and possibility to check other types
     * @param map additional information about the environment
     */
    public void check( Type source, Type destination, ConversionTable table, ConversionMap map );
}
