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

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.storage.DeclarationKindFactory;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;

public class MissingDeclaration implements IMissingResource{
    public static final IGenericFactory<MissingDeclaration> FACTORY = new IGenericFactory<MissingDeclaration>(){
        public MissingDeclaration create(){
            return new MissingDeclaration();
        }

        public MissingDeclaration read( MissingDeclaration value, IStorage storage ) throws IOException{
            value.name = storage.readString();
            value.kind = DeclarationKindFactory.readArray( storage );
            return value;
        }

        public void write( MissingDeclaration value, IStorage storage ) throws IOException{
            storage.writeString( value.name );
            DeclarationKindFactory.writeArray( value.kind, storage );
        }
    };
    
    private String name;
    private Kind[] kind;
    
    private MissingDeclaration(){
        // nothing
    }
    
    public MissingDeclaration( String name, Kind[] kind ){
        this.name = name;
        this.kind = kind;
    }
    
    public boolean checkAvailable( ProjectTOS project, IParseFile file, IProgressMonitor monitor ){
        monitor.beginTask( "Search", 1 );
        IDeclaration declaration = project.getModel().getDeclaration( name, kind );
        monitor.done();
        
        if( declaration == null )
            return false;
        
        return !file.equals( declaration.getParseFile() );
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode( kind );
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ){
        if( this == obj )
            return true;
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        final MissingDeclaration other = ( MissingDeclaration )obj;
        if( !Arrays.equals( kind, other.kind ) )
            return false;
        if( name == null ){
            if( other.name != null )
                return false;
        }else if( !name.equals( other.name ) )
            return false;
        return true;
    }
    
    
}
