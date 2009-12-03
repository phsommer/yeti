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

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;

public class ModuleFieldPusher extends NesCFieldPusher{
    protected final int TRUE = 0;
    protected final int FALSE = 1;
    protected final int UNKNOWN = 2;
    
    public ModuleFieldPusher( String name, AnalyzeStack stack ){
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

            // 5. check no more than one non-forward declaration
            checkOneFieldDeclarationOnly();
        
            // 6. check functions have (no) implementation
            checkFunctionImplemented();
    
        }
        else{
            // in uses/provides clause
            
            // 3. ensure the field is a function
            checkIsFunctionType();
            
            // 4. check that all fields have the same modifiers
            checkFieldsSameModifiers( declaration.modifiers(), Modifiers.EVENT | Modifiers.COMMAND | STANDARD_IGNORE_CHECK );

            int need = checkNeedsImplementation();
            
            if( need == TRUE ){
                // 5. check no more than one non-forward declaration
                checkOneFieldDeclarationOnly();
            
                // 6. check functions have (no) implementation
                checkFunctionImplemented();                
            }
            else if( need == FALSE ){
                // 5. check no forward declarations
                checkNoFunctionImplemented();
            }   
        }
    }
    

    protected void checkNoFunctionImplemented(){
        boolean functionFound = false;
        boolean implementationFound = false;

        for( Definition definition : definitions ){
            if( !definition.typedef ){
                if( definition.type() != null && definition.type().asFunctionType() != null ){
                    functionFound = true;
                    if( !definition.forwardDeclaration )
                        implementationFound = true;
                }
            }
        }

        if( functionFound && implementationFound ){
            List<Name> names = new ArrayList<Name>();
            for( Definition definition : definitions ){
                if( !definition.typedef && definition.type() != null && definition.type().asFunctionType() != null ){
                    names.add( definition.name );
                    definition.putWarningFlag();
                }
            }
            error( "no need to implement '" + name + "'", names );
        }
    }

    @Override
    protected void sendMissingDeclarationMessage( List<Name> names ){
        error( "missing implementation for '" + name + "'", names );
    }
    
    protected int checkNeedsImplementation(){
        boolean needsImplementation = false;
        boolean needsNoImplementation = false;
        
        for( Definition definition : definitions ){
            if( !definition.typedef ){
                Field field = definition.getField();
                if( field != null ){
                    FieldModelNode node = field.asNode();
                    if( node != null ){
                        if( node.getTags().contains( Tag.USES )){
                            if( node.getTags().contains( Tag.EVENT )){
                                needsImplementation = true;
                            }
                            if( node.getTags().contains( Tag.COMMAND )){
                                needsNoImplementation = true;
                            }
                        }
                        if( node.getTags().contains( Tag.PROVIDES )){
                            if( node.getTags().contains( Tag.EVENT )){
                                needsNoImplementation = true;
                            }
                            if( node.getTags().contains( Tag.COMMAND )){
                                needsImplementation = true;
                            }
                        }
                    }
                }
            }
        }
        
        if( needsImplementation == needsNoImplementation )
            return UNKNOWN;
        
        if( needsImplementation )
            return TRUE;
        
        return FALSE;
    }
}
