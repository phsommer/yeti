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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.IMessage;
import tinyos.yeti.preprocessor.output.Insight;

public class Message implements IMessage {
    private String message;
    private String key;
    private IParseFile parseFile;
    private Severity severity;
    private List<IFileRegion> regions = new ArrayList<IFileRegion>( 2 );
    private Insight insight;
    
    public Message( IParseFile parseFile, Severity severity, String message, String key, Insight insight ){
        this.parseFile = parseFile;
        this.severity = severity;
        this.message = message;
        this.key = key;
        this.insight = insight == null ? null : insight.seal();
    }
    
    public void setMessage( String message ) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getMessageKey(){
        return key;
    }

    public void setParseFile( IParseFile parseFile ) {
        this.parseFile = parseFile;
    }
    
    public IParseFile getParseFile() {
        return parseFile;
    }

    public Map<String, Object> getQuickfixInfos() {
        if( insight == null )
            return null;
        
        return insight.getMap();
    }

    public void addRegion( IParseFile file, int offset, int length, int line ){
        regions.add( new FileRegion( file, offset, length, line ) );
    }
    
    public IFileRegion[] getRegions() {
        return regions.toArray( new IFileRegion[ regions.size() ] );
    }

    public void setSeverity( Severity severity ) {
        this.severity = severity;
    }
    
    public Severity getSeverity() {
        return severity;
    }
}
