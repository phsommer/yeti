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
package tinyos.yeti.environment.basic.tools.mig;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.environment.basic.tools.BaseSetting;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class MigSetting extends BaseSetting {
    public MigSetting( ProjectTOS project ){
        super( project );
    }

    @Override
    public void write(XWriteStack xml) {
        super.write( xml );

        String format = getMessageFormatFile();
        if( format != null ){
            xml.push( "format" );
            xml.setText( format );
            xml.pop();
        }

        String type = getMessageType();
        if( type != null ){
            xml.push( "type" );
            xml.setText( type );
            xml.pop();
        }
    }

    @Override
    public void read(XReadStack xml) {
        super.read( xml );
        if( xml.search( "format" )){
            setMessageFormatFile( xml.getText() );
            xml.pop();
        }

        if( xml.search( "type" )){
            setMessageType( xml.getText() );
            xml.pop();
        }
    }

    public void setMessageFormatFile( String file ){
        file = validate( file );
        if( file == null )
            values.remove( "format" );
        else
            values.put( "format", file );
    }

    public String getMessageFormatFile(){
        return (String)values.get( "format" );
    }

    public void setMessageType( String type ){
        type = validate( type );
        if( type == null ){
            values.remove( "type" );
        }
        else{
            values.put( "type", type );
        }
    }

    public String getMessageType(){
        return (String)values.get( "type" );
    }
}
