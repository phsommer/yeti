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
package tinyos.yeti.nesc.parser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

import tinyos.yeti.editors.quickfixer.QuickFixer;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.IMessage;
import tinyos.yeti.ep.parser.standard.FileRegion;

public class ParserError implements IMessage{

    public int severity = IMarker.SEVERITY_ERROR;

    public int state=-1;
    public int token = -1;
    public int offset = -1;
    public int line = -1;
    public int length = -1;

    public String[] expected; 
    public String message;
    public IParseFile file;

    public ParserError(int state, int token, int offset, String[] expected, String message) {
        this.state = state;
        this.token = token;
        this.offset = offset;
        this.expected = expected;
        this.message = message;
    }

    public ParserError() {

    }

    public String getExpected() {
        String exp = "";
        if (expected != null) {
            for (int i = 0; i < expected.length; i++) exp += expected[i]+" ";
        }
        return exp;
    }

    public String errorString() {
        String exp = "";
        if (expected != null) {
            for (int i = 0; i < expected.length; i++) exp += expected[i]+" ";
            exp = "- Expected = " + exp;
        } 
        if (message == null) { 
            message = "";
        }
        return message + exp; 

    }

    public Map<String, Object> getQuickfixInfos() {
        Map<String, Object> result = new HashMap<String, Object>();
        if( expected != null )
            result.put( QuickFixer.EXPECTED, expected );
        return result;
    }

    @Override
    public String toString() {
        String exp = "";
        if (expected != null) {
            for (int i = 0; i < expected.length; i++) exp += expected[i]+" ";
        }

        return "State    : " + state + "\n" + 
        "Token    : " + token + "\n" + 
        "@offset  : " + offset + "\n" +
        "length   : " + length + "\n"+
        "Line     : " + line + "\n"+
        "ErrorMsg : " + message + "\n"+
        "Expected : " + exp+"\n"+
        "-----------------------------------";
    }



    public String getMessage() {
        return message;
    }

    public String getMessageKey(){
        return getMessage();
    }

    public IParseFile getParseFile() {
        return file;
    }

    public IFileRegion[] getRegions() {
        IFileRegion[] regions = new IFileRegion[]{ new FileRegion( offset, length, line, file ) };
        return regions;
    }

    public Severity getSeverity() {
        switch( severity ){
            case IMarker.SEVERITY_ERROR: return Severity.ERROR;
            case IMarker.SEVERITY_INFO: return Severity.INFO;
            case IMarker.SEVERITY_WARNING: return Severity.WARNING;
        }

        return Severity.WARNING;
    }

}