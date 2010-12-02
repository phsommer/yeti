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

import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

/**
 * A declarator is a combination of other declarators. The full declarator
 * should define one new variable, like <code>*x</code> or <code>t[6]</code>.
 * @author Benjamin Sigg
 *
 */
public interface Declarator extends ASTNode{
    /**
     * Tries to resolve the type of this declarator.
     * @param name the type which the identifier of the declarator has.
     * @param stack used to report errors
     * @return the resolved type or <code>null</code>
     */
    public Type resolveType( Type name, AnalyzeStack stack );
    
    /**
     * Tries to find the only name that is in the declarator.
     * @return the only name in the declarator
     */
    public Name resolveName();
    
    /**
     * Tries to identify the attributes that are associated with
     * this declarator.
     * @return the attributes or <code>null</code>
     */
    public ModelAttribute[] resolveAttributes();
    
    /**
     * Resolves the indices that a NesC-function can have.
     * @return the indices, these fields may not have a name
     */
    public Field[] resolveIndices();
    
    /**
     * If this declarator wrapps a {@link FunctionDeclarator} then
     * this function declarator is returned.
     * @return information about the function or <code>null</code>
     */
    public FunctionDeclarator getFunction();
}
