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
package tinyos.yeti.environment.winXP.path;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import tinyos.yeti.environment.basic.path.IPathTranslator;

/**
 * This translator uses maps to translate paths.
 * @author Benjamin Sigg
 */
public class DatabaseTranslator implements IWinPathTranslator{
    private Map<String, Object> modelToSystem = new HashMap<String, Object>();
    private Map<File, String> systemToModel = new HashMap<File, String>();
    
    private IPathTranslator backup;
    
    private final Object NULL = new Object();
    
    public DatabaseTranslator( IPathTranslator backup ){
        if( backup == null )
            throw new IllegalArgumentException( "backup must not be null" );
        this.backup = backup;
    }
    
    public void notifyResourceChanged() {
        synchronized( modelToSystem ){
            modelToSystem.clear();
        }
        synchronized( systemToModel ) {
            systemToModel.clear();    
        }
    }
    
    public File modelToSystem( String file ) {
        synchronized( modelToSystem ) {
            Object result = modelToSystem.get( file );
            if( result == NULL )
            	return null;
            
            if( result == null ){
                result = backup.modelToSystem( file );
                modelToSystem.put( file, result );
            }
            return (File)result;
        }
    }
    
    public String systemToModel( File file ) {
        synchronized( systemToModel ) {
            String result = systemToModel.get( file );
            if( result == null ){
                result = backup.systemToModel( file );
                systemToModel.put( file, result );
            }
            return result;
        }
    }
}
