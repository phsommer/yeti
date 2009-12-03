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

import java.util.HashMap;
import java.util.Map;

import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypedefType;

/**
 * Converts base types like uint16_t to their internal representation.
 * @author Benjamin Sigg
 */
public class BaseTypeConversion {
//    "signed char",              "int8_t",
//    "int8_t",                   "nx_int8_t",
//    "int8_t",                   "nxle_int8_t",
//    "signed int",               "int16_t",
//    "int16_t",                  "nx_int16_t",
//    "int16_t",                  "nxle_int16_t",
//    "signed long int",          "int32_t",
//    "int32_t",                  "nx_int32_t",
//    "int32_t",                  "nxle_int32_t",
//    "signed long long int",     "int64_t",
//    "int64_t",                  "nx_int64_t",
//    "int64_t",                  "nxle_int64_t",
//    "unsigned char",            "uint8_t",
//    "uint8_t",                  "nx_uint8_t",
//    "uint8_t",                  "nxle_uint8_t",
//    "unsigned int",             "uint16_t",
//    "uint16_t",                 "nx_uint16_t",
//    "uint16_t",                 "nxle_uint16_t",
//    "unsigned long int",        "uint32_t",
//    "uint32_t",                 "nx_uint32_t",
//    "uint32_t",                 "nxle_uint32_t",
//    "unsigned long long int",   "uint64_t",
//    "uint64_t",                 "nx_uint64_t",
//    "uint64_t",                 "nxle_uint64_t",
    
    private Map<String, Type> conversions = new HashMap<String, Type>();
    
    public BaseTypeConversion(){
    	conversions.put( "int8_t", BaseType.S_CHAR );
    	conversions.put( "nx_int8_t", BaseType.S_CHAR );
    	conversions.put( "nxle_int8_t", BaseType.S_CHAR );
    	
    	conversions.put( "int16_t", BaseType.S_INT );
    	conversions.put( "nx_int16_t", BaseType.S_INT );
    	conversions.put( "nxle_int16_t", BaseType.S_INT );
    	
    	conversions.put( "int32_t", BaseType.S_LONG );
    	conversions.put( "nx_int32_t", BaseType.S_LONG );
    	conversions.put( "nxle_int32_t", BaseType.S_LONG );
    	
    	conversions.put( "int64_t", BaseType.S_LONG_LONG );
    	conversions.put( "nx_int64_t", BaseType.S_LONG_LONG );
    	conversions.put( "nxle_int64_t", BaseType.S_LONG_LONG );
    	
    	
    	conversions.put( "uint8_t", BaseType.U_CHAR );
    	conversions.put( "nx_uint8_t", BaseType.U_CHAR );
    	conversions.put( "nxle_uint8_t", BaseType.U_CHAR );
    	
    	conversions.put( "uint16_t", BaseType.U_INT );
    	conversions.put( "nx_uint16_t", BaseType.U_INT );
    	conversions.put( "nxle_uint16_t", BaseType.U_INT );
    	
    	conversions.put( "uint32_t", BaseType.U_LONG );
    	conversions.put( "nx_uint32_t", BaseType.U_LONG );
    	conversions.put( "nxle_uint32_t", BaseType.U_LONG );
    	
    	conversions.put( "uint64_t", BaseType.U_LONG_LONG );
    	conversions.put( "nx_uint64_t", BaseType.U_LONG_LONG );
    	conversions.put( "nxle_uint64_t", BaseType.U_LONG_LONG );
    }
    
    public boolean isKey( Type type ){
    	TypedefType typedef = type.asTypedefType();
    	if( typedef == null )
    		return false;
    	return conversions.containsKey( typedef.getName() );
    }
    
    public Type get( Type type ){
    	return conversions.get( type.asTypedefType().getName() );
    }
    
    public Conversion createSourceConversion(){
    	return new Conversion(){
    		public boolean responsible( Type source, Type destination ){
	    		return isKey( source );
    		}
    		
    		public void check( Type source, Type destination, ConversionTable table, ConversionMap map ){
    			table.check( get( source ), destination, map );
    		}
    	};
    }
    
    public Conversion createDestinationConversion(){
    	return new Conversion(){
    		public boolean responsible( Type source, Type destination ){
	    		return isKey( destination );
    		}
    		
    		public void check( Type source, Type destination, ConversionTable table, ConversionMap map ){
    			table.check( source, get( destination ), map );
    		}
    	};    	
    }
}
