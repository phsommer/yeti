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
package tinyos.yeti.nesc12.parser.ast.elements.types.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.types.EnumType;
import tinyos.yeti.nesc12.parser.ast.elements.types.FunctionType;

/**
 * Singleton class used to check the validity of a conversion from one {@link Type} 
 * to another {@link Type}.
 * @author Benjamin Sigg
 */
public class ConversionTable{
    private static ConversionTable table;

    public static ConversionTable instance(){
        if( table == null ){
            table = new ConversionTable();
        }
        
        return table;
    }
    
    private List<Conversion> conversions = new ArrayList<Conversion>();
    
    private ConversionTable(){
        /*
         * 
         * row: source
         * column: destination
         * 
         * enum: always transformed to signed int
         * nx/uint8_t, etc.: transformed to intern definition
         * 
         *              void    base    ptr     array   object  funct   const   generic
         * void         1       2       2       2       2       2       2       0
         * base         1       3       5                                       0
         * pointer      1       4       6       9                               0
         * array        1               8       7                               0
         * object       1                               10                      0
         * function     1                                       11              0
         * const        1                                                       0
         * generic      0       0       0       0       0       0       0       0
         */
        
        conversions.add( new EnumConversion() );
        
        BaseTypeConversion baseTypeConversion = new BaseTypeConversion();
        conversions.add( baseTypeConversion.createSourceConversion() );
        conversions.add( baseTypeConversion.createDestinationConversion() );
        
        conversions.add( new Generic() );               // 0
        conversions.add( new AnyToVoid() );             // 1
        conversions.add( new VoidToAny() );             // 2
        conversions.add( new BaseToBase() );            // 3
        conversions.add( new PointerToBase() );         // 4
        conversions.add( new BaseToPointer() );         // 5
        conversions.add( new PointerToPointer() );      // 6
        conversions.add( new ArrayToArray() );          // 7
        conversions.add( new ArrayToPointer() );        // 8
        conversions.add( new PointerToArray() );		// 9
        conversions.add( new ObjectToObject() );        // 10
        conversions.add( new FunctionToFunction() );    // 11
        
        // special conversions
        conversions.add( new StringToArray() );
    }
    
    public void check( Type source, Type destination, ConversionMap map ){
        check( source, destination, map, new InfiniteRecursionPreventer() );
    }
    
    private void check( Type source, Type destination, ConversionMap map, InfiniteRecursionPreventer stack ){
    	if( stack.push( source, destination )){
            for( Conversion conversion : conversions ){
                if( conversion.responsible( source, destination )){
                    conversion.check( source, destination, this, map );
                    stack.pop();
                    return;
                }
            }
            
            map.reportError( "incompatible types", source, destination );
            stack.pop();
        }
    }
    
    /**
     * Checks the equality of two types preventing any problems that can
     * happen because of infinite recursion.
     * @param source the first type
     * @param destination the second type
     * @return <code>true</code> if the types are equal
     */    
    public boolean equals( Type source, Type destination ){
        return equals( source, destination, false );
    }
    
    /**
     * Checks the equality of two types preventing any problems that can
     * happen because of infinite recursion.
     * @param source the first type
     * @param destination the second type
     * @param respectTypedefNames if <code>true</code> then the name of typedefs must be equal as well
     * @return <code>true</code> if the types are equal
     */    
    public boolean equals( Type source, Type destination, boolean respectTypedefNames ){
        return equals( source, destination, new InfiniteRecursionPreventer(), respectTypedefNames );
    }

    private boolean equals( Type source, Type destination, InfiniteRecursionPreventer stack, boolean respectTypedefNames ){
        if( stack.push( source, destination )){
            try{
                if( source == destination ){
                    return true;
                }

                if( source == null && destination != null )
                    return false;
                
                if( source != null && destination == null )
                    return false;
                
                if( respectTypedefNames ){
                	String sName = null;
                	String dName = null;
                	
                	if( source.asTypedefType() != null )
                		sName = source.asTypedefType().getName();
                	
                	if( destination.asTypedefType() != null )
                		dName = destination.asTypedefType().getName();
                	
                	if( sName != null && !sName.equals( dName ))
                		return false;
                	else if( sName == null && dName != null )
                		return false;
                }
                
                if( source.asConstType() != null ){
                    if( destination.asConstType() != null )
                        return equals( source.asConstType().getRawType(), destination.asConstType().getRawType(), stack, respectTypedefNames );
                    else
                        return false;
                }

                if( source.asBase() != null ){
                    return source.asBase() == destination.asBase();
                }
                
                if( source.asGenericType() != null ){
                    if( destination.asGenericType() != null ){
                        return source.asGenericType().getName().equals( destination.asGenericType().getName() );
                    }
                    else
                        return false;
                }
                
                if( source.asArrayType() != null ){
                    if( destination.asArrayType() != null ){
                        if( source.asArrayType().getSize() != destination.asArrayType().getSize() )
                            return false;
                        
                        if( source.asArrayType().getLength() != destination.asArrayType().getLength() )
                            return false;
                        
                        return equals( source.asArrayType().getRawType(), destination.asArrayType().getRawType(), stack, respectTypedefNames );
                    }
                    else
                        return false;
                }
                
                if( source.asPointerType() != null ){
                    if( destination.asPointerType() != null ){
                        return equals( source.asPointerType().getRawType(), destination.asPointerType().getRawType(), stack, respectTypedefNames );
                    }
                    else
                        return false;
                }
                
                if( source.asEnumType() != null ){
                    if( destination.asEnumType() != null ){
                        EnumType sData = source.asEnumType();
                        EnumType dData = destination.asEnumType();
                        
                        String sName = sData.getName();
                        String dName = dData.getName();
                        
                        if( sName != null && !sName.equals( dName )){
                            return false;
                        }
                        if( sName == null && dName != null ){
                            return false;
                        }
                        
                        return Arrays.equals( sData.getConstants(), dData.getConstants() );
                    }
                }
                
                if( source.asDataObjectType() != null ){
                    if( destination.asDataObjectType() != null ){
                        DataObjectType sData = source.asDataObjectType();
                        DataObjectType dData = destination.asDataObjectType();
                        
                        if( sData.getKind() != dData.getKind() )
                        	return false;
                        
                        if( sData.getFieldCount() != dData.getFieldCount() )
                            return false;
                        
                        for( int i = 0, n = sData.getFieldCount(); i<n; i++ ){
                            Field sField = sData.getField( i );
                            Field dField = dData.getField( i );
                            
                            if( sField == null || dField == null ){
                                if( sField != null )
                                    return false;
                                if( dField != null )
                                    return false;
                            }
                            else{
                                Name sName = sField.getName();
                                Name dName = dField.getName();
                                
                                if( sName == null || dName == null ){
                                    if( sName != null )
                                        return false;
                                    if( dName != null )
                                        return false;
                                }
                                else{
                                    if( !sName.equals( dName ))
                                        return false;
                                }
                                
                                Type sType = sField.getType();
                                Type dType = dField.getType();
                                
                                if( sType == null || dType == null ){
                                    if( sType != null )
                                        return false;
                                    if( dType != null )
                                        return false;
                                }
                                else{
                                    if( !equals( sType, dType, stack, respectTypedefNames ))
                                        return false;
                                }
                            }
                        }
                        
                        return true;
                    }
                    else
                        return false;
                }
                
                if( source.asFunctionType() != null ){
                    if( destination.asFunctionType() != null ){
                        FunctionType sFunction = source.asFunctionType();
                        FunctionType dFunction = destination.asFunctionType();
                        
                        Type sResult = sFunction.getResult();
                        Type dResult = dFunction.getResult();
                        if( sResult == null || dResult == null ){
                            if( sResult != null )
                                return false;
                            if( dResult != null )
                                return false;
                        }
                        else{
                            if( !equals( sResult, dResult, stack, respectTypedefNames ))
                                return false;
                        }
                        
                        if( sFunction.getArgumentCount() != dFunction.getArgumentCount() )
                            return false;
                        
                        for( int i = 0, n = sFunction.getArgumentCount(); i<n; i++ ){
                            Type sType = sFunction.getArgument( i );
                            Type dType = dFunction.getArgument( i );
                            if( sType == null || dType == null ){
                                if( sType != null )
                                    return false;
                                if( dType != null )
                                    return false;
                            }
                            else{
                                if( !equals( sType, dType, stack, respectTypedefNames ))
                                    return false;
                            }
                        }
                        
                        return true;
                    }
                    else
                        return false;
                }
                
                return false;
            }
            finally{
                stack.pop();
            }
        }
        return true;
    }
}

















