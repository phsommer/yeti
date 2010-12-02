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
package tinyos.yeti.model.missing;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;

public class MissingUserFile implements IMissingResource{
    public static final IGenericFactory<MissingUserFile> FACTORY = new IGenericFactory<MissingUserFile>(){
        public MissingUserFile create(){
            return new MissingUserFile( null );
        }

        public MissingUserFile read( MissingUserFile value, IStorage storage ) throws IOException{
            value.name = storage.readString();
            return value;
        }

        public void write( MissingUserFile value, IStorage storage ) throws IOException{
            storage.writeString( value.name );
        }
        
    };
    
    private String name;
    
    public MissingUserFile( String name ){
        this.name = name;
    }
    
    public boolean checkAvailable( ProjectTOS project, IParseFile file, IProgressMonitor monitor ){
        File result = project.locateFile( name, false, monitor );
        if( result == null )
            return false;
        
        IParseFile parseFile = project.getModel().parseFile( result );
        if( parseFile == null )
            return false;
        
        return !parseFile.equals( file );
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public boolean equals( Object obj ){
        if( this == obj )
            return true;
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        MissingUserFile other = (MissingUserFile)obj;
        
        return name.equals( other.name );
    }
}
