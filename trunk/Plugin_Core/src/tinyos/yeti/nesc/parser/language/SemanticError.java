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
package tinyos.yeti.nesc.parser.language;

import java.util.Map;

import tinyos.yeti.editors.quickfixer.QuickFixer;
import tinyos.yeti.nesc.parser.ParserError;
import tinyos.yeti.nesc.scanner.ITokenInfo;

public class SemanticError extends ParserError {
	public final static int NO_TYPE = -1;
	private Map<String, Object> quickfixInfos;
	
	// public int severity = IMarker.SEVERITY_ERROR;
	public int type = NO_TYPE;
	
	public SemanticError (String message, int line, int charoffset, int length, int type){
		this.message = message;
		this.line = line;
		this.offset = charoffset;
		this.length = length;
		this.type = type;
	}
	
	public SemanticError (String message, int line, int charoffset, int length){
		this(message,line,charoffset,length,NO_TYPE);
	}
	
	public SemanticError (String message, int line){
		this.message = message;
		this.line = line;
	}
	public SemanticError(String string, ITokenInfo it) {
		this(string,it,NO_TYPE);
	}
	
	public void addQuickfixInfos( Map<String, Object> quickfixInfos ) {
        this.quickfixInfos = quickfixInfos;
    }
	
	@Override
	public Map<String, Object> getQuickfixInfos() {
	    Map<String, Object> map = super.getQuickfixInfos();
	    if( type != NO_TYPE )
	        map.put( QuickFixer.SEMANTICS, type );
	    map.put( QuickFixer.OFFSET, offset );
	    map.put( QuickFixer.LENGTH, length );
	    if( quickfixInfos != null )
	        map.putAll( quickfixInfos );
	    return map;
	}
	
	public SemanticError(String string, ITokenInfo it, int type) {
		if (it == null) {
			this.message = string;
			this.type = type;
		} else {
			this.message = string;
			this.line = it.getLine();
			this.offset = it.getOffset();
			this.length = it.getLength();
			this.type = type;
		}
	}	
}
