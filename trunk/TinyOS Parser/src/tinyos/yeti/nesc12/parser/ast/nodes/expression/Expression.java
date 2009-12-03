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
package tinyos.yeti.nesc12.parser.ast.nodes.expression;

import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.GenericArgument;

/**
 * Represents an expression.
 * @author Benjamin Sigg
 */
public interface Expression extends ASTNode, GenericArgument{
    /**
     * Tells whether this expression is constant, meaning whether this
     * expression can be evaluated at compile time or not.
     * @return <code>true</code> if this expression is constant, if in
     * doubt, returns <code>false</code>.
     */
    public boolean isConstant();
    
    /**
     * Tells whether this expression has commas that are not within
     * parenthesis.
     * @return <code>true</code> if this expression has commas
     */
    public boolean hasCommas();
    
    /**
     * Tries to find the constant value that this expression resembles.
     * @return the constant value, a {@link Number}, {@link String}, an array
     * of any possible result, or <code>null</code>
     */
    public Value resolveConstantValue();
    
    /**
     * Tries to find the constant value that this list of expressions resembles.
     * If this is not a list, then an array of size 1 should be returned.
     * @return an array with <code>null</code> values for faulty expressions,
     * or <code>null</code> if the whole statement is illegal
     */
    public Value[] resolveConstantValues();
    
    /**
     * Tries to find out what type this expression is.
     * @return the type or <code>null</code> if unknown
     */
    public Type resolveType();
}
