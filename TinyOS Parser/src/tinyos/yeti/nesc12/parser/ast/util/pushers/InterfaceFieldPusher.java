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
package tinyos.yeti.nesc12.parser.ast.util.pushers;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;

public class InterfaceFieldPusher extends NesCFieldPusher{
    public InterfaceFieldPusher( String name, AnalyzeStack stack ){
        super( name, stack );
    }

    /**
     * Checks the errors that come from redefinition and redeclarations.
     */
    @Override
    public void resolve(){
        /*
         * Rules to apply:
         * - only one type must be used
         * - typedefs and nontypedefs are not to be mixed
         * - for fields: only one non-forward declaration may be used
         * - for typedefs: only one typedef may be used
         * - for functions: forwardDeclaration without implementation should 
         *   yield a warning
         */

        // 1. check only typedefs or non typedefs
        checkNoMixing();

        // 2. check not more than one typedef
        checkOnlyOneTypedef();

        Definition declaration = checkDefinedOnlyOnce();
        if( declaration == null ){
            // not in uses/provides clause

            // 3. check that all fields have the same type
            checkFieldsSameType();
            
            // 4. check that all fields have the same modifiers
            checkFieldsSameModifiers();
        }
        else{
            // in uses/provides clause
            
            // 3. ensure the field is a function
            checkIsFunctionType();
        }
    }
}
