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
package tinyos.yeti.preprocessor.lexer;

import java.io.IOException;
import java.io.Reader;

/**
 * This reader can tell how many newlines and characters were read.
 * @author Benjamin Sigg
 */
public class CountingReader extends Reader{
    private Reader base;
    
    private int count;
    private int newlines;

    private int lastChar = -1;

    private final char[] NEWLINES = { '\r', '\n', '\u2028', '\u2029', '\u000B', '\u000C', '\u0085' };
    
    
    public CountingReader( Reader base ){
        this.base = base;
    }

    public int getCount() {
        return count;
    }
    
    public int getNewlines() {
        return newlines;
    }
    
    @Override
    public void close() throws IOException {
        base.close();
    }
    
    @Override
    public int read( char[] cbuf, int off, int len ) throws IOException {
        int read = base.read( cbuf, off, len );
        for( int i = off, n = off+read; i<n; i++ ){
            check( cbuf[i] );
        }
        return read;
    }
    
    @Override
    public int read() throws IOException {
        int c = base.read();
        check( c );
        return c; 
    }
    
    private void check( char character ){
        count++;
        if( lastChar != '\r' || character != '\n' ){
            for( char newline : NEWLINES ){
                if( newline == character ){
                    newlines++;
                    break;
                }
            }
        }
        lastChar = character;
    }
    
    private void check( int character ){
        if( character != -1 ){
            check( (char)character );
        }
    }
}
