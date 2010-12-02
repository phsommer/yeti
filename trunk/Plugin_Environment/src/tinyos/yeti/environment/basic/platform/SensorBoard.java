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
package tinyos.yeti.environment.basic.platform;

import java.io.File;
import java.io.IOException;

import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;
import tinyos.yeti.environment.basic.path.PlatformFile;
import tinyos.yeti.ep.ISensorBoard;

/**
 * A simple implementation of {@link ISensorBoard}, stores all its values
 * in properties.
 * @author Benjamin Sigg
 */
public class SensorBoard implements ISensorBoard{
    private String name;
    private String description;
    private File directory;
    
    private String[] directives;
    
    public void setName( String name ){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }

    public void setDescription( String description ){
        this.description = description;
    }
    
    public String getDescription(){
        return description;
    }
    
    public void setDirectory( File directory ){
        this.directory = directory;
    }
    
    public File getDirectory(){
        return directory;
    }
    
    public String[] getDirectives(){
        if( directives == null ){
            directives = loadDirectives();
            if( directives == null )
                directives = new String[]{};
        }
        
        return directives;
    }
    
    protected String[] loadDirectives(){
        File sensor = new File( directory, ".sensor" );
        if( !sensor.exists() )
            return null;
        PlatformFile file = new PlatformFile();
        try{
            file.readFrom( sensor );
        }
        catch ( IOException e ){
            TinyOSAbstractEnvironmentPlugin.error( e );
        }
        return file.getIncludes();
    }
}
