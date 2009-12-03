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

public class DateMacro extends GenericMacro {
    public DateMacro(){
        super( "__DATE__", null, VarArg.NO, null );
        
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get( Calendar.MONTH );
        int day = calendar.get( Calendar.DAY_OF_MONTH );
        int year = calendar.get( Calendar.YEAR );
        
        setTokenSequence( "\"" + getMonth( month ) + " " + getDay( day ) + " " + year + "\"" );
    }
    
    private String getMonth( int month ){
        switch( month ){
            case Calendar.JANUARY:      return "jan";
            case Calendar.FEBRUARY:     return "feb";
            case Calendar.MARCH:        return "mar";
            case Calendar.APRIL:        return "apr";
            case Calendar.MAY:          return "may";
            case Calendar.JUNE:         return "jun";
            case Calendar.JULY:         return "jul";
            case Calendar.AUGUST:       return "aug";
            case Calendar.SEPTEMBER:    return "sep";
            case Calendar.OCTOBER:      return "oct";
            case Calendar.NOVEMBER:     return "now";
            case Calendar.DECEMBER:     return "dec";
            default: return null;
        }
    }
    
    private String getDay( int day ){
        if( day < 10 )
            return "0" + day;
        
        return String.valueOf( day );
    }
}
