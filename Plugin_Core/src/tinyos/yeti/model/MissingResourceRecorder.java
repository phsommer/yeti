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
package tinyos.yeti.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import tinyos.yeti.ep.parser.IMissingResourceRecorder;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.model.missing.IMissingResource;
import tinyos.yeti.model.missing.MissingDeclaration;
import tinyos.yeti.model.missing.MissingSystemFile;
import tinyos.yeti.model.missing.MissingUserFile;

public class MissingResourceRecorder implements IMissingResourceRecorder{
    public static final IGenericFactory<MissingResourceRecorder> FACTORY = new IGenericFactory<MissingResourceRecorder>(){
        public MissingResourceRecorder create(){
            return new MissingResourceRecorder();
        }

        public MissingResourceRecorder read( MissingResourceRecorder value, IStorage storage ) throws IOException{
            int size = storage.in().readInt();
            for( int i = 0; i < size; i++ ){
                value.missing.add( storage.<IMissingResource>read() );
            }
            return value;
        }

        public void write( MissingResourceRecorder value, IStorage storage ) throws IOException{
            storage.out().writeInt( value.missing.size() );
            for( IMissingResource resource : value.missing ){
                storage.write( resource );
            }
        }
    };
    
    private Set<IMissingResource> missing = new HashSet<IMissingResource>();
    
    public void missingDeclaration( String name, Kind... kind ){
        missing.add( new MissingDeclaration( name, kind ) );
    }

    public void missingSystemFile( String name ){
        missing.add( new MissingSystemFile( name ) );
    }

    public void missingUserFile( String name ){
        missing.add( new MissingUserFile( name ) );
    }
    
    public void missing( IMissingResource resource ){
        missing.add( resource );
    }
    
    public IMissingResource[] getMissingResources(){
        return missing.toArray( new IMissingResource[ missing.size() ] );
    }
}
