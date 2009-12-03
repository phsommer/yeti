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
package tinyos.yeti.nesc12.parser.ast.util;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclarationSpecifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ParameterDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;

public class DefaultModifierValidator implements ModifierValidator{
    public void check( AnalyzeStack stack, DeclarationSpecifierList specifiers, InitDeclarator declaration ){
        // ignore
    }

    public void check( AnalyzeStack stack, FunctionDefinition definition ){
        // ignore
    }

    public void check( AnalyzeStack stack, ParameterDeclaration declaration ){
        // ignore
    }
    
    protected void checkAutoWarning( AnalyzeStack stack, FunctionDefinition definition ){
        DeclarationSpecifierList specifiers = definition.getSpecifiers();
        if( specifiers == null )
            return;
        
        specifiers.checkModifiers( stack, null, 0, Modifiers.AUTO, null, false, definition.resolveName() );
    }
}
