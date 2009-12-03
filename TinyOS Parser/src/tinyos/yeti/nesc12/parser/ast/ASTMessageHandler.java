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
package tinyos.yeti.nesc12.parser.ast;

import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.output.Insight;

/**
 * A {@link ASTMessageHandler} is given to an AST, the AST will report all
 * messages, warnings and errors to this handler.
 * @author Benjamin Sigg
 */
public interface ASTMessageHandler {
    public static enum Severity{
        MESSAGE, WARNING, ERROR
    }
    
    /**
     * Called when a message is to be reported.
     * @param severity the importance of the message
     * @param message the message itself
     * @param insight additional information about the error
     * @param ranges the location of the nodes
     */
    public void report( Severity severity, String message, Insight insight, RangeDescription... ranges );
    
    /**
     * Called when a message is to be reported.
     * @param severity the importance of the message
     * @param message the message itself
     * @param insight additional information about the error
     * @param nodes the nodes involved in the problem
     */
    public void report( Severity severity, String message, Insight insight, ASTNode... nodes );
}
