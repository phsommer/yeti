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
package tinyos.yeti.environment.basic.tools.ncg;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.environment.basic.tools.BaseSetting;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class NcgSetting extends BaseSetting{
    public NcgSetting( ProjectTOS project ){
    	super( project );
    }
    
    @Override
    public void write( XWriteStack xml ){
    	super.write( xml );

        String file = getNesCFile();
        if( file != null ){
            xml.push( "input" );
            xml.setText( file );
            xml.pop();
        }
        
        String[] names = getFilenamesOrConstants();
        if( names != null ){
            xml.push( "names" );
            for( String name : names ){
                xml.push( "name" );
                xml.setText( name );
                xml.pop();
            }
            xml.pop();
        }
    }
    
    @Override
    public void read( XReadStack xml ){
        super.read( xml );

        if( xml.search( "input" )){
            setNesCFile( xml.getText() );
            xml.pop();
        }
        
        if( xml.search( "names" )){
            List<String> names = new ArrayList<String>();
            while( xml.hasNext( "name" ) ){
                xml.next( "name" );
                names.add( xml.getText() );
                xml.pop();
            }
            setFilenamesOrConstants( names.toArray( new String[ names.size() ] ) );
            xml.pop();
        }
    }
    
    public void setNesCFile( String file ){
        file = validate( file );
        if( file == null )
            values.remove( "nesCFile" );
        else
            values.put( "nesCFile", file );
    }
    
    public String getNesCFile(){
        return (String)values.get( "nesCFile" );
    }
    
    public void setFilenamesOrConstants( String[] names ){
        if( names == null )
            values.remove( "names" );
        else
            values.put( "names", names );
    }
    
    public String[] getFilenamesOrConstants(){
        return (String[])values.get( "names" );
    }
    
}
