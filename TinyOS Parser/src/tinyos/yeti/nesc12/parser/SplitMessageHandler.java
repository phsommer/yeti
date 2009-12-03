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
package tinyos.yeti.nesc12.parser;

import tinyos.yeti.nesc12.ParserMessageHandler;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.output.Insight;

/**
 * A message handler that sends the message to two other handlers. 
 * @author Benjamin Sigg
 *
 */
public class SplitMessageHandler implements ParserMessageHandler{
    private ParserMessageHandler first;
    private ParserMessageHandler second;
    
    public SplitMessageHandler( ParserMessageHandler first, ParserMessageHandler second ){
        this.first = first;
        this.second = second;
    }
    
    public void error( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
        first.error( message, preprocessor, insight, ranges );
        second.error( message, preprocessor, insight, ranges );
    }
    
    public void message( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
        first.message( message, preprocessor, insight, ranges );
        second.message( message, preprocessor, insight, ranges );
    }
    
    public void warning( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
        first.warning( message, preprocessor, insight, ranges );
        second.warning( message, preprocessor, insight, ranges );
    }
}
