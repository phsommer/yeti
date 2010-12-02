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

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;

public class StandardFieldPusher extends FieldPusher{
    public static final int STANDARD_IGNORE_CHECK = Modifiers.INLINE;
    
    public StandardFieldPusher( String name, AnalyzeStack stack ){
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

        // 3. check that all fields have the same type
        checkFieldsSameType();
        
        // 4. check that all fields have the same modifiers
        checkFieldsSameModifiers();

        // 5. check no more than one non-forward declaration
        checkOneFieldDeclarationOnly();

        // 6. check functions have implementation
        checkFunctionImplemented();
    }

    protected void checkNoMixing(){
        boolean foundTypedef = false;
        boolean foundField = false;

        for( Definition definition : definitions ){
            if( definition.typedef )
                foundTypedef = true;
            else
                foundField = true;
        }

        if( foundTypedef && foundField ){
            List<Name> names = new ArrayList<Name>( definitions.size() );
            for( Definition definition : definitions ){
                names.add( definition.name );
                definition.putErrorFlag();
            }
            error( "redeclaration of '" + name + "' as different kind of symbol", names );
        }
    }

    protected void checkOnlyOneTypedef(){
        int typedefCount = 0;
        for( Definition definition : definitions ){
            if( definition.typedef )
                typedefCount++;
        }

        if( typedefCount > 1 ){
            List<Name> names = new ArrayList<Name>();
            for( Definition definition : definitions ){
                if( definition.typedef ){
                    names.add( definition.name );
                    definition.putErrorFlag();
                }
            }
            error( "redefinition of typedef '" + name + "'", names );
        }
    }

    protected void checkFieldsSameType(){
        checkFieldsSameType( null );
    }
    
    protected void checkFieldsSameType( Type base ){
        boolean error = false;
        boolean hard = base != null;

        for( Definition definition : definitions ){
            if( !definition.typedef ){
                if( base == null )
                    base = definition.type();
                else if( definition.type() != null ){
                    if( !base.equals( definition.type() ))
                        error = true;
                }
            }
        }

        if( error ){
            List<Name> names = new ArrayList<Name>();
            for( Definition definition : definitions ){
                if( !(hard && base.equals( definition.type() ))){
                    if( !definition.typedef ){
                        names.add( definition.name );
                        definition.putErrorFlag();
                    }
                }
            }

            if( hard ){
                error( "'" + name + "' must have type '" + base.toLabel( null, Type.Label.SMALL ), names );
            }
            else{
                error( "conflicting types for '" + name + "'", names );
            }
        }
    }
    
    protected void checkFieldsSameModifiers(){
        checkFieldsSameModifiers( null, STANDARD_IGNORE_CHECK );
    }
    
    protected void checkFieldsSameModifiers( Modifiers base, int ignoreCheck ){
        boolean error = false;
        boolean hard = base != null;
        boolean first = !hard;
        
        for( Definition definition : definitions ){
            if( !definition.typedef ){
                if( first ){
                    first = false;
                    base = definition.modifiers();
                }
                else{
                    Modifiers next = definition.modifiers();
                    if( !localCanFollowBase( base, next, ignoreCheck )){
                        error = true;
                        break;
                    }
                }
            }
        }
        
        if( error ){
            List<Name> names = new ArrayList<Name>();
            for( Definition definition : definitions ){
                if( !(hard && localCanFollowBase( base, definition.modifiers(), ignoreCheck ) )){
                    if( !definition.typedef ){
                        names.add( definition.name );
                        definition.putErrorFlag();
                    }
                }
            }

            if( hard ){
                error( "the modifiers of '" + name + "' must be '" + base + "'", names );
            }
            else{
                error( "conflicting modifiers for '" + name + "'", names );
            }
        }
    }
    
    private boolean localCanFollowBase( Modifiers base, Modifiers local, int ignore ){
        final int inherit = Modifiers.STATIC;
        
        int flagsBase = base == null ? 0 : base.getFlags();
        int flagsLocal = local == null ? 0 : local.getFlags();
        
        flagsLocal |= flagsBase & inherit;
        
        return (flagsBase & ~ignore) == (flagsLocal & ~ignore);
    }

    protected void checkOneFieldDeclarationOnly(){
        int count = 0;
        for( Definition definition : definitions ){
            if( !definition.typedef && !definition.forwardDeclaration ){
                count++;
            }
        }

        if( count > 1 ){
            List<Name> names = new ArrayList<Name>();
            boolean first = true;
            
            for( Definition definition : definitions ){
                if( !definition.typedef && !definition.forwardDeclaration ){
                	if( first ){
                		first = false;
                		error( "'" + name + "' gets redefined", definition.name );
                		definition.putWarningFlag();
                	}
                	else{
                		names.add( definition.name );
                		definition.putErrorFlag();
                	}
                }
            }    
            error( "redefinition of '" + name + "'", names );
        }
    }

    protected void checkFunctionImplemented(){
        boolean functionFound = false;
        boolean implementationFound = false;

        boolean extern = false;
        
        for( Definition definition : definitions ){
            if( !definition.typedef ){
                if( definition.type() != null && definition.type().asFunctionType() != null ){
                    functionFound = true;
                    if( !definition.forwardDeclaration )
                        implementationFound = true;
                }
                Modifiers modifiers = definition.modifiers();
                if( modifiers != null && modifiers.isExtern() ){
                    extern = true;
                }
            }
        }

        if( functionFound && !implementationFound && !extern ){
            List<Name> names = new ArrayList<Name>();
            for( Definition definition : definitions ){
                if( !definition.typedef && definition.type() != null && definition.type().asFunctionType() != null ){
                    names.add( definition.name );
                    definition.putWarningFlag();
                }
            }
            sendMissingDeclarationMessage( names );
        }
    }
    
    protected void sendMissingDeclarationMessage( List<Name> names ){
        warning( "forward declaration for function '"+name+"' found, but missing the implementation", names );
    }
}
