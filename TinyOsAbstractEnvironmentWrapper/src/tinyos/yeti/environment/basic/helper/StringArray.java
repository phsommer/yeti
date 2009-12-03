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
package tinyos.yeti.environment.basic.helper;

/**
 * Various methods that can be helpful when working with String arrays.
 */
public class StringArray{
    public static String[] merge( String[] cmd2, String string ) {
        if( string == null )
            return cmd2;
        
        String[] res = new String[ cmd2.length+1 ];
        System.arraycopy( cmd2,0,res,0,cmd2.length );
        res[ cmd2.length ] = string;
        return res;
    }


    public static String[] merge( String[] a, String[] b ) {
        if( a==null )
            return b;
        
        if( b==null )
            return a;

        String[] res = new String[ a.length+b.length ];

        System.arraycopy( a,0,res,0,a.length );
        System.arraycopy( b,0,res,a.length,b.length );

        return res;
    }
}
