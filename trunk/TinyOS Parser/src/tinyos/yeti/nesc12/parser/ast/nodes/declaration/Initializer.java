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
package tinyos.yeti.nesc12.parser.ast.nodes.declaration;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.util.InitializerCounter;

public interface Initializer extends ASTNode{
    /**
     * Counter to check the current initializing process and create a constant value
     * representing the initialization.
     */
    public static final Key<InitializerCounter> INITIALIZER_COUNTER = new Key<InitializerCounter>( "initializer counter" );
    
    /**
     * Key for the {@link AnalyzeStack} to access the base type specified in the
     * enclosing declaration.
     */
    public static final Key<Type> BASE_TYPE = new Key<Type>( "base type" );
    
    /**
     * Tries to find out which type this initializer represents.
     * @return the type of this initializer
     */
    public Type resolveType();
    
    /**
     * Tries to find out which value this initializer represents. The
     * behavior of this method is unspecified if {@link #isAssignmentable()}
     * return <code>false</code>.
     * @return the assignable value represented by this initializer, can be
     * <code>null</code>
     */
    public Value resolveValue();
    
    /**
     * Tells whether this initializer has a form such that it can be used
     * in an assignment, meaning the initializer is no composite of braces, 
     * indices or fields.
     * @return <code>true</code> if this has the form of an assignment
     */
    public boolean isAssignmentable();
}
