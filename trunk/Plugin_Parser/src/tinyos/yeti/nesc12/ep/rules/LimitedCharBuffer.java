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
package tinyos.yeti.nesc12.ep.rules;

public class LimitedCharBuffer{
    private char[] buffer;
    private int offset;
    private int length;
    
    public LimitedCharBuffer( int max ){
        buffer = new char[ max ];
        offset = 0;
        length = 0;
    }
    
    public void clear(){
        offset = 0;
        length = 0;
    }
    
    public int length(){
        return length;
    }
    
    /**
     * Adds a character at the end of this buffer, may delete the first
     * character of this buffer to gain space.
     * @param c the new character
     */
    public void tail( char c ){
        int index = offset + length;
        index %= buffer.length;
        buffer[ index ] = c;
        if( length < buffer.length )
            length++;
        else
            offset++;
    }
    
    /**
     * Adds a character at the beginning of this buffer, may delete
     * the last character of this buffer to gain space.
     * @param c the new character
     */
    public void head( char c ){
        offset--;
        if( offset < 0 )
            offset = buffer.length-1;
        
        buffer[offset] = c;
        if( length < buffer.length )
            length++;
    }
    
    public boolean startsWith( String string ){
        if( string.length() > length )
            return false;
        
        for( int i = 0, j = offset, n = string.length(); i < n; i++, j = (j+1) % length ){
            if( buffer[j] != string.charAt( i ))
                return false;
        }
        
        return true;
    }
    
    public boolean equals( String string ){
        if( string.length() != length )
            return false;
        
        for( int i = 0, j = offset; i < length; i++, j = (j+1) % length ){
            if( buffer[j] != string.charAt( i ))
                return false;
        }
        
        return true;
    }
}
