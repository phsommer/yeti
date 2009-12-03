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

public class LimitedIntBuffer {
    private int[] buffer;
    private int offset;
    private int length;
    
    public LimitedIntBuffer( int max ){
        buffer = new int[ max ];
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
    
    public int get( int index ){
        return buffer[ (offset + index) % buffer.length ];
    }
    
    /**
     * Adds a character at the end of this buffer, may delete the first
     * character of this buffer to gain space.
     * @param c the new character
     */
    public void tail( int c ){
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
    public void head( int c ){
        offset--;
        if( offset < 0 )
            offset = buffer.length-1;
        
        buffer[offset] = c;
        if( length < buffer.length )
            length++;
    }
}
