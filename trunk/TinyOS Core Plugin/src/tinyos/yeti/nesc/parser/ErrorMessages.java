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
package tinyos.yeti.nesc.parser;

import java.util.Hashtable;
import java.util.Map;


public class ErrorMessages {

    private static Map<Integer, Map<Integer, ParserError>> messages = 
        new Hashtable<Integer, Map<Integer,ParserError>>();

    public static ParserError getDetailedMessage(ParserError pe) {
        if (pe == null) return null;

        int state = pe.state;
        if (state == -1) {
            return pe;
        }
        // state probably known ?
        Map<Integer, ParserError> ht = messages.get( state );
        if (ht == null)
            return pe;

        // state known ..
        // does error with the same next token exist ?
        ParserError re = ht.get( pe.token );

        if (re == null) {
            // return generic message (next token = 0)
            re = ht.get( 0 );
            if (re != null) {
                pe.message = re.message;
                return pe;
            } 
        } else {
            // exact match
            pe.message = re.message;
            return pe;
        }

        // nothing suitable found..
        return pe;
    }

    /*
     * do not edit this static initializer block by hand..
     * let it be created from GenerateErrorMessages.java
     */
    static { 
        Map<Integer, ParserError> a2 = new Hashtable<Integer, ParserError>();
//      InterfaceName.nc
        a2.put( 123,new ParserError(2,123,20,new String[]{"ENUMERATION_CONSTANT","TYPEDEF_NAME","IDENTIFIER"},"Missing interface name"));
        messages.put( 2, a2 );
        Map<Integer, ParserError> a4 = new Hashtable<Integer, ParserError>();
//      ModuleName.nc
        a4.put( 123,new ParserError(4,123,20,new String[]{"ENUMERATION_CONSTANT","TYPEDEF_NAME","IDENTIFIER"},"Missing module name"));
        messages.put(4, a4);
        Map<Integer, ParserError> a7 = new Hashtable<Integer, ParserError>();
//      OnlyIncludesList.nc
        a7.put( 0,new ParserError(7,0,0,new String[]{"CONFIGURATION","INTERFACE","INCLUDES","MODULE"},"Missing interface, module or configuration specification"));
        messages.put(7, a7);
        Map<Integer, ParserError> a27 = new Hashtable<Integer, ParserError>();
//      MissingSemikolonIncludes.nc
        a27.put(324,new ParserError(27,324,19,new String[]{",",";"},"Missing Semikolon"));
        messages.put(27, a27);
        Map<Integer, ParserError> a28 = new Hashtable<Integer, ParserError>();
//      ModuleImplMissing.nc
        a28.put(0,new ParserError(28,0,34,new String[]{"IMPLEMENTATION"},"Missing module implementation"));
        messages.put(28, a28);
        Map<Integer, ParserError> a32 = new Hashtable<Integer, ParserError>();
//      BracketModule.nc
        a32.put(341,new ParserError(32,341,132,new String[]{"}","PROVIDES","USES"},"Missing closing bracket"));
        messages.put(32, a32);
        Map<Integer, ParserError> a98 = new Hashtable<Integer, ParserError>();
//      MissingSemikolonInInterfaceDeclaration.nc
        a98.put(342,new ParserError(98,342,56,new String[]{"(",",",";","=","["},"Missing Semikolon"));
        messages.put(98, a98);
        Map<Integer, ParserError> a157 = new Hashtable<Integer, ParserError>();
//      BracketImplementation.nc
        a157.put(0,new ParserError(157,0,283,new String[]{"(","*","}","TYPEDEF_NAME","TYPEDEF","EXTERN","STATIC","AUTO","REGISTER","CHAR","SHORT","INT","LONG","SIGNED","UNSIGNED","FLOAT","DOUBLE","CONST","VOLATILE","VOID","STRUCT","UNION","ENUM","DEFAULT","IDENTIFIER","EVENT","TASK","ASYNC","NORACE","INLINE","COMMAND"},"Missing closing bracket"));
        messages.put(157, a157);
    }
}
