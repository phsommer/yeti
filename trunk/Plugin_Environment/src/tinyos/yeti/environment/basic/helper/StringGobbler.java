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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link StreamGobbler} that collects its output as text.
 * @author Benjamin Sigg
 */
public class StringGobbler extends StreamGobbler{
    private StringBuilder buffer;
    
    private boolean newlineOrEOF = false;
    
    public StringGobbler( InputStream input, OutputStream output ){
        super( input, output );
        buffer = new StringBuilder();
    }
    
    public String getOutputString(){
        return buffer.toString();
    }
    
    public boolean hasNewlineOrEOF(){
        return newlineOrEOF;
    }
    
    @Override
    protected void piped( int value ){
        if( value != -1 ){
        	if( value == '\n' || value == '\r' )
                newlineOrEOF = true;
            
            buffer.append( (char)value );
        }
        else{
            newlineOrEOF = true;
        }
    }
}
