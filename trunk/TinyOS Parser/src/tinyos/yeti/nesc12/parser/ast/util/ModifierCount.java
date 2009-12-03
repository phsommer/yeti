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

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.nodes.ListASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorSpecifier;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DirectDeclaratorSpecifier;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.StorageClass;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypeQualifier;
import tinyos.yeti.nesc12.parser.ast.util.messangers.NamedMessanger;

public class ModifierCount {
    public static final int TYPEDEF    = Modifiers.TYPEDEF;

    public static final int STATIC     = Modifiers.STATIC;
    public static final int AUTO       = Modifiers.AUTO;
    public static final int REGISTER   = Modifiers.REGISTER;
    public static final int EXTERN     = Modifiers.EXTERN;

    public static final int CONST      = Modifiers.CONST;
    public static final int RESTRICT   = Modifiers.RESTRICT;
    public static final int VOLATILE   = Modifiers.VOLATILE;

    public static final int INLINE     = Modifiers.INLINE;
    public static final int DEFAULT    = Modifiers.DEFAULT;

    public static final int COMMAND    = Modifiers.COMMAND;
    public static final int EVENT      = Modifiers.EVENT;
    public static final int TASK       = Modifiers.TASK;
    public static final int ASYNC      = Modifiers.ASYNC;
    public static final int NORACE     = Modifiers.NORACE;

    public int countTypedef;
    public int countStatic;
    public int countAuto;
    public int countRegister;
    public int countExtern;
    public int countConst;
    public int countRestrict;
    public int countVolatile;
    public int countInline;
    public int countDefault;
    public int countCommand;
    public int countEvent;
    public int countTask;
    public int countAsync;
    public int countNorace;

    public static Modifiers resolveModifiers( ListASTNode<? extends DeclaratorSpecifier> list ){
        ModifierCount count = new ModifierCount();
        count.count( list );
        return count.getModifiers();
    }

    public Modifiers getModifiers(){
        Modifiers modifiers = new Modifiers();
        if( countTypedef > 0 )
            modifiers.setTypedef( true );

        if( countStatic > 0 )
            modifiers.setStatic( true );

        if( countAuto > 0 )
            modifiers.setAuto( true );

        if( countRegister > 0 )
            modifiers.setRegister( true );

        if( countExtern > 0 )
            modifiers.setExtern( true );

        if( countConst > 0 )
            modifiers.setConst( true );

        if( countRestrict > 0 )
            modifiers.setRestrict( true );

        if( countVolatile > 0 )
            modifiers.setVolatile( true );

        if( countInline > 0 )
            modifiers.setInline( true );

        if( countDefault > 0 )
            modifiers.setDefault( true );

        if( countCommand > 0 )
            modifiers.setCommand( true );

        if( countEvent > 0 )
            modifiers.setEvent( true );

        if( countTask > 0 )
            modifiers.setTask( true );

        if( countAsync > 0 )
            modifiers.setAsync( true );
        
        if( countNorace > 0 )
            modifiers.setNorace( true );

        return modifiers;
    }

    /**
     * Checks the presence or absence of modifiers and reports errors if the 
     * conditions are not met.
     * @param list a list of modifiers
     * @param stack used to report any errors
     * @param presentCheck several combinations, from each combination at least
     * one modifier must be present
     * @param onceCheck the mask of modifiers which must not be used more than once
     * @param noneCheck the mask of modifiers which must not be present
     * @param oneOfCheck a list of masks of modifiers, only one modifier per mask
     * may be present (or non), can be <code>null</code>
     * @param error whether to report findings as error or only as warning
     * @param name name of the field that gets checked, can be <code>null</code>
     */
    public static void checkModifiers(
            ListASTNode<? extends DeclaratorSpecifier> list, AnalyzeStack stack,
            int[] presentCheck,
            int onceCheck, 
            int noneCheck,
            int[] oneOfCheck,
            boolean error,
            Name name ){

        checkModifiers( list, stack, presentCheck, onceCheck,
                noneCheck, oneOfCheck, new NamedMessanger( error, name ) );
    }


    /**
     * Checks the presence or absence of modifiers and reports errors if the 
     * conditions are not met.
     * @param list a list of modifiers
     * @param stack used to report any errors
     * @param presentCheck several combinations, from each combination at least
     * one modifier must be present
     * @param onceCheck the mask of modifiers which must not be used more than once
     * @param noneCheck the mask of modifiers which must not be present
     * @param oneOfCheck a list of masks of modifiers, only one modifier per mask
     * may be present (or non), can be <code>null</code>
     * @param messanger used to format the error output
     */
    public static void checkModifiers(
            ListASTNode<? extends DeclaratorSpecifier> list, AnalyzeStack stack,
            int[] presentCheck,
            int onceCheck, 
            int noneCheck,
            int[] oneOfCheck,
            ModifierCountMessanger messanger ){
        int countPresent = presentCheck == null ? 0 : presentCheck.length;
        int countOnce = Integer.bitCount( onceCheck );
        int countNone = Integer.bitCount( noneCheck );
        int countOneOf = oneOfCheck == null ? 0 : oneOfCheck.length;

        int count = countPresent + countOnce + countNone + countOneOf;

        Pusher[] pushers = new Pusher[ count ];
        int index = 0;

        // build pushers
        if( presentCheck != null ){
            for( int mask : presentCheck ){
                pushers[ index++ ] = new Pusher( mask, messanger );
            }
        }

        for( int word : Modifiers.ALL_MODIFIERS ){
            if( (onceCheck & word) == word )
                pushers[ index++ ] = new Pusher( word, messanger );
        }

        for( int word : Modifiers.ALL_MODIFIERS ){
            if( (noneCheck & word) == word )
                pushers[ index++ ] = new Pusher( word, messanger );
        }

        if( oneOfCheck != null ){
            for( int mask : oneOfCheck ){
                pushers[ index++ ] = new Pusher( mask, messanger );
            }
        }

        // collect data
        for( int i = 0, n = list.getChildrenCount(); i<n; i++ ){
            DeclaratorSpecifier specifier = list.getNoError( i );
            if( specifier != null ){
                int type = typeOf( specifier );
                if( type != 0 ){
                    for( Pusher pusher : pushers )
                        pusher.push( type, specifier );
                }
            }
        }

        // write errors
        index = 0;
        for( int i = 0; i < countPresent; i++ )
            pushers[ index++ ].checkAtLeastOnce( stack );

        for( int i = 0; i < countOnce; i++ )
            pushers[ index++ ].checkModifierOnlyOnce( stack );

        for( int i = 0; i < countNone; i++ )
            pushers[ index++ ].checkEmpty( stack );

        for( int i = 0; i < countOneOf; i++ )
            pushers[ index++ ].checkOneKindOfModifier( stack );
    }

    public static String toString( int type ){
        switch( type ){
            case TYPEDEF: return "typedef";

            case STATIC: return "static";
            case AUTO: return "auto";
            case REGISTER: return "register";
            case EXTERN: return "extern";

            case CONST: return "const";
            case RESTRICT: return "restrict";
            case VOLATILE: return "volatile";

            case INLINE: return "inline";
            case DEFAULT: return "default";

            case COMMAND: return "command";
            case EVENT: return "event";
            case TASK: return "task";
            case ASYNC: return "async";
            case NORACE: return "norace";

            default: return null;
        }
    }

    /**
     * Tells what kind of modifier <code>specifier</code> is. One of the constant
     * values of this class is returned as result.
     * @param specifier the specifier, can be <code>null</code> or invalid
     * @return the type of the modifier or 0
     */
    public static int typeOf( DeclaratorSpecifier specifier ){
        if( specifier != null ){
            if( specifier instanceof StorageClass ){
                StorageClass sc = (StorageClass)specifier;
                switch( sc.getStorage() ){
                    case ASYNC: 
                        return ASYNC;
                    case NORACE: 
                        return NORACE;
                    case AUTO:
                        return AUTO;
                    case COMMAND:
                        return COMMAND;
                    case EVENT:
                        return EVENT;
                    case EXTERN:
                        return EXTERN;
                    case REGISTER:
                        return REGISTER;
                    case STATIC:
                        return STATIC;
                    case TASK:
                        return TASK;
                    case TYPEDEF:
                        return TYPEDEF;
                }
            }
            else if( specifier instanceof DirectDeclaratorSpecifier ){
                DirectDeclaratorSpecifier dd = (DirectDeclaratorSpecifier)specifier;
                switch( dd.getSpecifier() ){
                    case DEFAULT:
                        return DEFAULT;
                    case INLINE:
                        return INLINE;
                }
            }
            else if( specifier instanceof TypeQualifier ){
                TypeQualifier tq = (TypeQualifier)specifier;
                switch( tq.getQualifier() ){
                    case CONST:
                        return CONST;
                    case RESTRICT:
                        return RESTRICT;
                    case VOLATILE:
                        return VOLATILE;
                }
            }
        }

        return 0;
    }

    public void count( ListASTNode<? extends DeclaratorSpecifier> list ){
        for( int i = 0, n = list.getChildrenCount(); i<n; i++ ){
            DeclaratorSpecifier spec = list.getNoError( i );
            if( spec != null ){
                if( spec instanceof StorageClass ){
                    StorageClass sc = (StorageClass)spec;
                    switch( sc.getStorage() ){
                        case ASYNC:
                            countAsync++;
                            break;
                        case NORACE:
                            countNorace++;
                            break;
                        case AUTO:
                            countAuto++;
                            break;
                        case COMMAND:
                            countCommand++;
                            break;
                        case EVENT:
                            countEvent++;
                            break;
                        case EXTERN:
                            countExtern++;
                            break;
                        case REGISTER:
                            countRegister++;
                            break;
                        case STATIC:
                            countStatic++;
                            break;
                        case TASK:
                            countTask++;
                            break;
                        case TYPEDEF:
                            countTypedef++;
                            break;
                    }
                }
                else if( spec instanceof DirectDeclaratorSpecifier ){
                    DirectDeclaratorSpecifier dd = (DirectDeclaratorSpecifier)spec;
                    switch( dd.getSpecifier() ){
                        case DEFAULT:
                            countDefault++;
                            break;
                        case INLINE:
                            countInline++;
                            break;
                    }
                }
                else if( spec instanceof TypeQualifier ){
                    TypeQualifier tq = (TypeQualifier)spec;
                    switch( tq.getQualifier() ){
                        case CONST:
                            countConst++;
                            break;
                        case RESTRICT:
                            countRestrict++;
                            break;
                        case VOLATILE:
                            countVolatile++;
                            break;
                    }
                }
            }
        }
    }

    /**
     * Used to check that no modifiers collide
     */
    private static class Pusher{
        private List<DeclaratorSpecifier> specifiers = new ArrayList<DeclaratorSpecifier>();
        private int mask;

        private ModifierCountMessanger messanger;

        /**
         * Creates a new pusher
         * @param mask the mask of all types that should be collected by 
         * this pusher.
         * @param messanger used to generate error messages
         */
        public Pusher( int mask, ModifierCountMessanger messanger ){
            this.mask = mask;
            this.messanger = messanger;
        }

        public void push( int type, DeclaratorSpecifier specifier ){
            if( (type & mask) != 0 ){
                specifiers.add( specifier );
            }
        }

        /**
         * Checks whether at least one modifier was found. If not, then
         * an error "missing modifier" is reported.
         * @param stack used to report the error
         */
        public void checkAtLeastOnce( AnalyzeStack stack ){
            if( specifiers.size() == 0 ){
                messanger.reportMissing( stack, mask, specifiers );
            }
        }

        /**
         * Checks whether this pusher is empty or not. If not empty, then
         * a "modifier not allowed" error is reported.
         * @param stack used to report the error
         */
        public void checkEmpty( AnalyzeStack stack ){
            if( specifiers.size() > 0 ){
                messanger.reportForbidden( stack, mask, specifiers );
            }
        }

        /**
         * Checks that the number of modifiers is not greater then one and
         * reports an "modifier can be used only once" error.
         * @param stack used to report errors
         */
        public void checkModifierOnlyOnce( AnalyzeStack stack ){
            if( specifiers.size() > 1 ){
                messanger.reportMultiOccurence( stack, mask, specifiers );
            }
        }

        /**
         * Checks that only one kind of modifier (or non at all) is collected.
         * Reports an "use only one of ..." error.
         * @param stack used to report errors
         */
        public void checkOneKindOfModifier( AnalyzeStack stack ){
            if( specifiers.size() > 1 ){
                int found = 0;
                for( DeclaratorSpecifier specifier : specifiers ){
                    found |= ModifierCount.typeOf( specifier );
                }

                int count = Integer.bitCount( found );
                if( count > 1 ){
                    messanger.reportNotSet( stack, mask, specifiers );
                }
            }
        }
    }
}
