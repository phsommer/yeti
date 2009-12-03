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
package tinyos.yeti.preprocessor.parser.stream;

import java.io.IOException;
import java.io.Reader;

import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * A {@link Reader} that reads the {@link PreprocessorToken tokens} from a 
 * tree of {@link PreprocessorElement}s.
 * @author Benjamin Sigg
 */
public class TokenReader extends Reader{
    private ElementStream stream;
    
    private String current;
    private int index;
    
    public TokenReader( PreprocessorElement element ){
        stream = new ElementStream( element, true );
    }
    
    @Override
    public int read( char[] cbuf, int off, int len ) throws IOException {
        if( len <= 0 )
            return 0;
        
        if( current == null ){
            current = nextString();
            index = 0;
            
            if( current == null )
                return -1;
        }
        
        int read = 0;
        while( current != null && read < len ){
            while( index >= current.length() ){
                current = nextString();
                index = 0;
                
                if( current == null )
                    return read;
            }
            
            cbuf[ off+read ] = current.charAt( index );
            index++;
            read++;
        }
        
        return read;
    }
    
    @Override
    public void close() throws IOException {
        // nothing to do
    }
    
    private String nextString(){
        while( stream.hasNext() ){
            PreprocessorToken token = stream.next().getToken();
            if( token != null )
                return token.getText();
        }
        
        return null;
    }
}
