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
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ListASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.AttributeDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DataObjectDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorSpecifier;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.EnumDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.IncompleteDataObject;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.PrimitiveSpecifier;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypeSpecifier;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypedefName;

public class SpecifierCount {
    public static final int VOID           = 1;
    public static final int CHAR           = 1 << 1;
    public static final int SHORT          = 1 << 2;
    public static final int INT            = 1 << 3;
    public static final int LONG           = 1 << 4;
    public static final int FLOAT          = 1 << 5;
    public static final int DOUBLE         = 1 << 6;
    public static final int SIGNED         = 1 << 7;
    public static final int UNSIGNED       = 1 << 8;
    public static final int BOOL           = 1 << 9;
    public static final int COMPLEX        = 1 << 10;
    public static final int DATA_OBJECT    = 1 << 11;
    public static final int ATTRIBUTE      = 1 << 12;
    public static final int ENUM           = 1 << 13;
    public static final int TYPEDEF_NAME   = 1 << 14;
    public static final int ERROR          = 1 << 15;
    
    public int countVoid;
    public int countChar;
    public int countShort;
    public int countInt;
    public int countLong;
    public int countFloat;
    public int countDouble;
    public int countSigned;
    public int countUnsigned;
    public int countBool;
    public int countComplex;
    public int countDataObject;
    public int countAttribute;
    public int countEnum;
    public int countTypedefName;
    
    public int countError;
    
    public void count( ListASTNode<? extends DeclaratorSpecifier> specifiers ){
        for( int i = 0, n= specifiers.getChildrenCount(); i<n; i++ ){
            DeclaratorSpecifier specifier = specifiers.getNoError( i );
            if( specifier != null && specifier.isSpecifier() ){
                count( (TypeSpecifier)specifier );
            }
            else if( specifiers.getChild( i ) instanceof ErrorASTNode ){
                countError++;
            }
        }
    }
    
    public void count( TypeSpecifier specifier ){
        if( specifier.isPrimitive() ){
            PrimitiveSpecifier primitive = (PrimitiveSpecifier)specifier;
            if( primitive.getType() != null ){
                switch( primitive.getType() ){
                    case _BOOL:
                        countBool++;
                        break;
                    case _COMPLEX:
                        countComplex++;
                        break;
                    case CHAR:
                        countChar++;
                        break;
                    case DOUBLE:
                        countDouble++;
                        break;
                    case FLOAT:
                        countFloat++;
                        break;
                    case INT:
                        countInt++;
                        break;
                    case LONG:
                        countLong++;
                        break;
                    case SHORT:
                        countShort++;
                        break;
                    case SIGNED:
                        countSigned++;
                        break;
                    case UNSIGNED:
                        countUnsigned++;
                        break;
                    case VOID:
                        countVoid++;
                        break;
                }
            }
        }
        else if( specifier.isDataObject() ){
            countDataObject++;
        }
        else if( specifier.isEnum() ){
            countEnum++;
        }
        else if( specifier.isTypedefName() ){
            countTypedefName++;
        }
        else if( specifier.isAttribute() ){
            countAttribute++;
        }
    }
    
    /**
     * Checks whether the <code>specifiers</code> specify a type and if not
     * reports errors using <code>stack</code>.
     * @param specifiers a list of specifiers
     * @param stack used to report errors
     */
    public static void checkSpecifiers( ListASTNode<? extends DeclaratorSpecifier> specifiers, AnalyzeStack stack ){
        if( resolveRawType( specifiers ) == null ){
            // TODO make nicer error message...
            stack.error( "not a type", specifiers );
        }
    }
    
    /**
     * Tries to find the type that is described by <code>specifiers</code>.
     * @param specifiers a list of specifiers
     * @return the type described by these specifiers
     */
    public static Type resolveRawType( ListASTNode<? extends DeclaratorSpecifier> specifiers ){
        SpecifierCount count = new SpecifierCount();
        count.count( specifiers );
        
        Type type = count.resolveBaseType();
        if( type != null )
            return type;
        

        if( count.checkCountOtherZero( SpecifierCount.DATA_OBJECT, 1 )){
            // there is a union or struct somewhere
            for( int i = 0, n = specifiers.getChildrenCount(); i<n; i++ ){
                DeclaratorSpecifier specifier = specifiers.getNoError( i );
                if( specifier instanceof DataObjectDeclaration ){
                    return ((DataObjectDeclaration)specifier).resolveType();
                }
                else if( specifier instanceof IncompleteDataObject ){
                    return ((IncompleteDataObject)specifier).resolveType();
                }
            }
        }
        
        if( count.checkCountOtherZero( SpecifierCount.ATTRIBUTE, 1 )){
            for( int i = 0, n = specifiers.getChildrenCount(); i<n; i++ ){
                DeclaratorSpecifier specifier = specifiers.getNoError( i );
                if( specifier instanceof AttributeDeclaration ){
                    return ((AttributeDeclaration)specifier).resolveType();
                }
            }
        }
        
        if( count.checkCountOtherZero( SpecifierCount.TYPEDEF_NAME, 1 )){
            // there is a typedef name somewhere
            for( int i = 0, n = specifiers.getChildrenCount(); i<n; i++ ){
                DeclaratorSpecifier specifier = specifiers.getNoError( i );
                if( specifier instanceof TypedefName ){
                    return ((TypedefName)specifier).resolveType();
                }
            }
        }
        
        if( count.checkCountOtherZero( SpecifierCount.ENUM, 1 )){
            // there is a enum somewhere
            for( int i = 0, n = specifiers.getChildrenCount(); i<n; i++ ){
                DeclaratorSpecifier specifier = specifiers.getNoError( i );
                if( specifier instanceof EnumDeclaration ){
                    return ((EnumDeclaration)specifier).resolveType();
                }
            }
        }
        
        // TODO there are more types
        return null;
    }
    
    public BaseType resolveBaseType(){
        if( checkCountOtherZero( VOID, 1 ))
            return BaseType.VOID;
        
        if( checkCountOtherZero( CHAR, 1 ))
            return BaseType.U_CHAR;
        if( checkCountOtherZero( UNSIGNED | CHAR, 1 ))
            return BaseType.U_CHAR;
        
        if( checkCountOtherZero( SIGNED | CHAR, 1 ))
            return BaseType.S_CHAR;
        
        if( checkCountOtherZero( SHORT, 1 ))
            return BaseType.S_SHORT;
        if( checkCountOtherZero( SIGNED | SHORT, 1 ))
            return BaseType.S_SHORT;
        if( checkCountOtherZero( SHORT | INT, 1 ))
            return BaseType.S_SHORT;
        if( checkCountOtherZero( SIGNED | SHORT | INT, 1 ))
            return BaseType.S_SHORT;
        
        if( checkCountOtherZero( UNSIGNED | SHORT, 1 ))
            return BaseType.U_SHORT;
        if( checkCountOtherZero( UNSIGNED | SHORT | INT, 1 ))
            return BaseType.U_SHORT;
        
        if( checkCountOtherZero( INT, 1 ))
            return BaseType.S_INT;
        if( checkCountOtherZero( SIGNED, 1 ))
            return BaseType.S_INT;
        if( checkCountOtherZero( SIGNED | INT, 1 ))
            return BaseType.S_INT;
        
        if( checkCountOtherZero( UNSIGNED, 1 ))
            return BaseType.U_INT;
        if( checkCountOtherZero( UNSIGNED | INT, 1 ))
            return BaseType.U_INT;
        
        if( checkCountOtherZero( SIGNED | LONG | INT, 1 ))
            return BaseType.S_LONG;
        if( checkCountOtherZero( SIGNED | LONG, 1 ))
            return BaseType.S_LONG;
        if( checkCountOtherZero( LONG | INT, 1 ))
            return BaseType.S_LONG;
        if( checkCountOtherZero( LONG, 1 ))
            return BaseType.S_LONG;
        
        if( checkCountOtherZero( UNSIGNED | LONG, 1 ))
            return BaseType.U_LONG;
        if( checkCountOtherZero( UNSIGNED | LONG | INT, 1 ))
            return BaseType.U_LONG;
        
        if( checkCountOtherZero( LONG, 2 ))
            return BaseType.S_LONG_LONG;
        if( checkCountOtherZero( SIGNED | INT, 1, LONG, 2 ))
            return BaseType.S_LONG_LONG;
        if( checkCountOtherZero( SIGNED, 1, LONG, 2 ))
            return BaseType.S_LONG_LONG;
        if( checkCountOtherZero( INT, 1, LONG, 2 ))
            return BaseType.S_LONG_LONG;
        
        if( checkCountOtherZero( UNSIGNED | INT, 1, LONG, 2 ))
            return BaseType.U_LONG_LONG;
        if( checkCountOtherZero( UNSIGNED, 1, LONG, 2 ))
            return BaseType.U_LONG_LONG;
        
        if( checkCountOtherZero( FLOAT, 1 ))
            return BaseType.FLOAT;
        
        if( checkCountOtherZero( DOUBLE, 1 ))
            return BaseType.DOUBLE;
        
        if( checkCountOtherZero( LONG | DOUBLE, 1 ))
            return BaseType.LONG_DOUBLE;
        
        if( checkCountOtherZero( BOOL, 1 ))
            return BaseType.BOOL;
        
        if( checkCountOtherZero( FLOAT | COMPLEX, 1 ))
            return BaseType.FLOAT_COMPLEX;
        
        if( checkCountOtherZero( DOUBLE | COMPLEX, 1 ))
            return BaseType.DOUBLE_COMPLEX;
        
        if( checkCountOtherZero( DOUBLE | COMPLEX | LONG, 1 ))
            return BaseType.LONG_DOUBLE_COMPLEX;
        
        return null;
    }
    
    public boolean checkCountOtherZero( int flag, int expected ){
        if( !checkCount( flag, expected ))
            return false;
        
        int disable = ERROR;
        return checkCount( ~flag & ~disable, 0 );
    }
    
    public boolean checkCountOtherZero( int flagA, int expectedA, int flagB, int expectedB ){
        if( !checkCount( flagA, expectedA ))
            return false;
        if( !checkCount( flagB, expectedB ))
            return false;
        
        int disable = ERROR;
        return checkCount( ~flagA & ~flagB & ~disable, 0 );
    }
    
    public boolean checkCount( int flag, int expected ){
        if( (flag & VOID) == VOID && countVoid != expected )
            return false;
        if( (flag & CHAR) == CHAR && countChar != expected )
            return false;
        if( (flag & SHORT) == SHORT && countShort != expected )
            return false;
        if( (flag & INT) == INT && countInt != expected )
            return false;
        if( (flag & LONG) == LONG && countLong != expected )
            return false;
        if( (flag & FLOAT) == FLOAT && countFloat != expected )
            return false;
        if( (flag & DOUBLE) == DOUBLE && countDouble != expected )
            return false;
        if( (flag & SIGNED) == SIGNED && countSigned != expected )
            return false;
        if( (flag & UNSIGNED) == UNSIGNED && countUnsigned != expected )
            return false;
        if( (flag & BOOL) == BOOL && countBool != expected )
            return false;
        if( (flag & COMPLEX) == COMPLEX && countComplex != expected )
            return false;
        if( (flag & DATA_OBJECT) == DATA_OBJECT && countDataObject != expected )
            return false;
        if( (flag & ENUM) == ENUM && countEnum != expected )
            return false;
        if( (flag & TYPEDEF_NAME) == TYPEDEF_NAME && countTypedefName != expected )
            return false;
        if( (flag & ERROR) == ERROR && countError != expected )
            return false;
        
        return true;
    }
}
