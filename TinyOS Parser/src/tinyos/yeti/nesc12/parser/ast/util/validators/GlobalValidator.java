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
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclarationSpecifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.util.DefaultModifierValidator;
import tinyos.yeti.nesc12.parser.ast.util.messangers.FunctionMessanger;

public class GlobalValidator extends DefaultModifierValidator{
    @Override
    public void check( AnalyzeStack stack, DeclarationSpecifierList specifiers, InitDeclarator declaration ){
        if( specifiers == null )
            return;

        Type type = specifiers.resolveType();
        if( type != null )
            type = declaration.resolveType( type, stack );

        if( type == null || type.asFunctionType() == null ){
            // a normal field like int x = 5;

            specifiers.checkModifiers(
                    stack, 
                    null,
                    Modifiers.ALL_TYPE_QUALIFIER | Modifiers.NORACE | Modifiers.INLINE,
                    (Modifiers.ALL_NESC & ~Modifiers.NORACE) | Modifiers.AUTO | Modifiers.REGISTER,
                    new int[]{ Modifiers.STATIC | Modifiers.EXTERN | Modifiers.TYPEDEF },
                    true,
                    declaration.resolveName() );

            Modifiers modifiers = specifiers.resolveModifiers();
            if( modifiers != null ){
                if( modifiers.isInline() ){
                    Name name = declaration.resolveName();
                    if( name != null ){
                        stack.warning( "variable '" + name.toIdentifier() + "' declared 'inline'", name.getRange() );
                    }
                }
            }
        }
        else{
            // a forward declaration of a function
            specifiers.checkModifiers(
                    stack, 
                    null,
                    Modifiers.ALL_TYPE_QUALIFIER | Modifiers.INLINE,
                    Modifiers.ALL_NESC | Modifiers.AUTO | Modifiers.REGISTER,
                    new int[]{ Modifiers.STATIC | Modifiers.EXTERN | Modifiers.TYPEDEF },
                    new FunctionMessanger( true, declaration.resolveName() ));
        }

    }

    @Override
    public void check( AnalyzeStack stack, FunctionDefinition definition ){
        DeclarationSpecifierList specifiers = definition.getSpecifiers();
        if( specifiers != null ){
            specifiers.checkModifiers(
                    stack, 
                    null,
                    Modifiers.CONST | Modifiers.VOLATILE | Modifiers.INLINE,
                    Modifiers.ALL_NESC | Modifiers.RESTRICT | Modifiers.REGISTER | Modifiers.TYPEDEF | Modifiers.AUTO,
                    new int[]{ Modifiers.STATIC | Modifiers.EXTERN },
                    new FunctionMessanger( true, definition.resolveName() ) );
            
            // checkAutoWarning( stack, definition );
            
        }
    }
}
