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
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclarationSpecifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.util.DefaultModifierValidator;
import tinyos.yeti.nesc12.parser.ast.util.messangers.FunctionMessanger;

public class ComponentAccessListValidator extends DefaultModifierValidator{
    @Override
    public void check( AnalyzeStack stack, DeclarationSpecifierList specifiers, InitDeclarator declaration ){
        if( specifiers == null )
            return;

        Type type = specifiers.resolveType();
        if( type != null )
            type = declaration.resolveType( type, stack );

        if( type != null || type.asFunctionType() != null ){
            // a forward declaration of an event or command function
            specifiers.checkModifiers(
                    stack, 
                    new int[]{ Modifiers.COMMAND | Modifiers.EVENT | Modifiers.TYPEDEF },
                    Modifiers.ALL_TYPE_QUALIFIER | Modifiers.INLINE | Modifiers.ASYNC,
                    Modifiers.TASK | Modifiers.DEFAULT | Modifiers.NORACE | Modifiers.AUTO | Modifiers.REGISTER,
                    null,
                    new FunctionMessanger( true, declaration.resolveName() ));
        }

    }
}
