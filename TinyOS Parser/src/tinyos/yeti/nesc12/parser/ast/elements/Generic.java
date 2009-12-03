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

/**
 * A {@link Generic} is either a type or a value.
 * @author Benjamin Sigg
 */
public interface Generic extends Binding{
    /**
     * Returns <code>this</code> if this is a type.
     * @return <code>this</code> or <code>null</code>
     */
    public Type asType();

    /**
     * Returns <code>this</code> if this is a value.
     * @return <code>this</code> or <code>null</code>
     */
    public Value asValue();
    
    /**
     * Recursively calls {@link Name#resolveRange()}
     */
    public void resolveNameRanges();
}
