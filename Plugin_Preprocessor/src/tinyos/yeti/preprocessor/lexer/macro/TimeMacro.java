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
package tinyos.yeti.preprocessor.lexer.macro;

import java.util.Calendar;

public class TimeMacro extends GenericMacro {
    public TimeMacro(){
        super( "__TIME__", null, VarArg.NO, null );
        
        Calendar calendar = Calendar.getInstance();
        
        setTokenSequence( "\"" +
                get( calendar, Calendar.HOUR_OF_DAY ) + ":" + 
                get( calendar, Calendar.MINUTE ) + ":" + 
                get( calendar, Calendar.SECOND ) + "\"" );
    }
    
    private String get( Calendar c, int field ){
        int value = c.get( field );
        if( value < 10 )
            return "0" + value;
        return String.valueOf( value );
    }
}
