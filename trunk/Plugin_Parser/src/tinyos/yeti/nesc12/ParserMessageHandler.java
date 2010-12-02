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
package tinyos.yeti.nesc12;

import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.output.Insight;

/**
 * A handler that collects various messages.
 * @author Benjamin Sigg
 */
public interface ParserMessageHandler {
    /**
     * Reports the presence of some error.
     * @param message information for the user
     * @param preprocessor whether the message comes from the preprocessor or not
     * @param insight additional information, can be <code>null</code>
     * @param ranges the location of the message
     */
    public void error( String message, boolean preprocessor, Insight insight, RangeDescription... ranges );
    
    /**
     * Reports the presence of some warning.
     * @param message information for the user
     * @param preprocessor whether the message comes from the preprocessor or not
     * @param insight additional information, can be <code>null</code>
     * @param ranges the location of the message
     */    
    public void warning( String message, boolean preprocessor, Insight insight, RangeDescription... ranges );
    /**
     * Reports some information.
     * @param message information for the user
     * @param preprocessor whether the message comes from the preprocessor or not
     * @param insight additional information, can be <code>null</code>
     * @param ranges the location of the message
     */
    public void message( String message, boolean preprocessor, Insight insight, RangeDescription... ranges );
}
