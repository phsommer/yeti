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
package tinyos.yeti.nesc12.parser.ast.util.validators;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclarationSpecifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.util.DefaultModifierValidator;

public class FunctionBodyValidator extends DefaultModifierValidator {
    @Override
    public void check(AnalyzeStack stack, DeclarationSpecifierList specifiers, InitDeclarator declaration){
        if( specifiers == null )
            return;
        
        specifiers.checkModifiers(
                stack, 
                null,
                Modifiers.ALL_TYPE_QUALIFIER, 
                Modifiers.ALL_REMAINING | Modifiers.ALL_NESC,
                new int[]{ Modifiers.ALL_STORAGE_CLASSES },
                true,
                declaration.resolveName() );
    }
}