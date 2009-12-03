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
package tinyos.yeti.utility;

import tinyos.yeti.ep.parser.IMacro;

public final class MacroUtility{
    private MacroUtility(){
        // nothing
    }
    
    public static IMacro[] combine( IMacro[] alpha, IMacro[] beta ){
        if( alpha == null || alpha.length == 0 )
            return beta;
        
        if( beta == null || beta.length == 0 )
            return alpha;
        
        IMacro[] both = new IMacro[ alpha.length + beta.length ];
        System.arraycopy( alpha, 0, both, 0, alpha.length );
        System.arraycopy( beta, 0, both, alpha.length, beta.length );
        return both;
    }
}
