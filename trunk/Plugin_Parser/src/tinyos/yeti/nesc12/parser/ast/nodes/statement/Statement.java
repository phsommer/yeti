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
package tinyos.yeti.nesc12.parser.ast.nodes.statement;

import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;

public interface Statement extends ASTNode, BlockItem {
    /**
     * Tells <code>flow</code> which statements can follow after this statement
     * @param flow the current flow of control
     */
    public void flow( ControlFlow flow );
    
    /**
     * Tells whether this statement would end the current function.
     * @return <code>true</code> if this statement ends the current function
     */
    public boolean isFunctionEnd();
    
    /**
     * If this statement is used as an expression, then return the
     * type of this expression
     * @return the type of this expression
     */
    public Type resolveExpressionType();
}
