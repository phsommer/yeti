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
package tinyos.yeti.environment.basic.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractCommand<R> implements ICommand<R>{
    private String[] command;
    private File directory;
    private Map<String,String> parameters;
    private boolean useDefaultParameters = true;
    private boolean interactive = false;
    
    public void setOneLineCommand( String command ){
        setCommand( splitCommand( command ) );
    }
    
    public void setCommand( String... command ){
        this.command = command;
    }
    
    public String[] splitCommand( String command ){
        List<String> list = new ArrayList<String>();
        int offset = 0;
        int length = 0;
        boolean jump = false;
        
        for( int i = 0, n = command.length(); i<n; i++ ){
            char c = command.charAt( i );
            
            if( c == '"' ){
                if( length == 0 ){
                    jump = true;
                    length = 1;
                }
                else if( jump ){
                    jump = false;
                    list.add( command.substring( offset+1, offset+length ) );
                    offset += length + 1;
                    length = 0;
                }
            }
            else if( Character.isWhitespace( c )){
                if( jump )
                    length++;
                else{
                    if( length == 0 ){
                        offset++;
                    }
                    else{
                        list.add( command.substring( offset, offset+length ) );
                        offset += length + 1;
                        length = 0;
                    }
                }
            }
            else{
                length++;
            }
        }
        
        if( length > 0 ){
            list.add( command.substring( offset ) );
        }
        
        return list.toArray( new String[ list.size() ] );
    }
    
    public String[] getCommand(){
        return command;
    }

    public void setDirectory( File directory ){
        this.directory = directory;
    }
    
    public File getDirectory(){
        return directory;
    }
    
    public void setUseDefaultParameters( boolean useDefaultParameters ) {
		this.useDefaultParameters = useDefaultParameters;
	}
    
    public boolean useDefaultParameters() {
    	return useDefaultParameters;
    }
    
    public boolean assumesInteractive(){
    	return interactive;
    }
    
    public void setAssumesInteractive( boolean interactive ){
		this.interactive = interactive;
	}
    
    public void putEnvironmentParameters( Map<String, String> parameters ){
        if( this.parameters == null )
            this.parameters = new HashMap<String, String>();
        
        for( Map.Entry<String, String> entry : parameters.entrySet() ){
        	putEnvironmentParameter( entry.getKey(), entry.getValue() );
        }
    }
    
    public void putEnvironmentParameter( String name, String value ){
        if( parameters == null )
            parameters = new HashMap<String, String>();
        
        parameters.put( name, value );
    }
    
    public Map<String, String> getEnvironmentParameters(){
        return parameters;
    }
    
    public boolean setup(){
        return true;
    }
}
