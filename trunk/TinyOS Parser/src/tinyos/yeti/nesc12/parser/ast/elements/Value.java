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
package tinyos.yeti.nesc12.parser.ast.elements;

import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;

/**
 * A value is a result that an {@link Expression} can have. Values must be 
 * known during compilation, hence only constant expressions can be evaluated.
 * @author Benjamin Sigg
 *
 */
public interface Value extends Generic{
    /**
     * Tries to find out which type this value is.
     * @return the type that best matches this value or <code>null</code>
     */
    public Type getType();
    
    /**
     * Tries to calculate the size of this value in bytes.
     * @return the size or -1 if not enough information available for
     * calculation
     */
    public int sizeOf();
    
    /**
     * Gets this value as human readable string.
     * @return the label of this value
     */
    public String toLabel();
}
