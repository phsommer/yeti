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
package tinyos.yeti.nesc12.parser.ast.nodes.nesc;

import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public interface Component extends ASTNode{
	/**
	 * Gets the name under which the component is visible inside
	 * the configuration.
	 * @return the name, may be <code>null</code>
	 */
    public String getFinalName();
    
    /**
     * Gets the node which is the origin of {@link #getFinalName()}.
     * @return the node, may be <code>null</code>
     */
    public ASTNode getFinalNameNode();
    
    /**
     * Returns the connection that points to the component which
     * is referenced by this.
     * @return the connection
     */
    public ModelConnection resolveConnection();
    
}
