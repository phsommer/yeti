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
package tinyos.yeti.nesc12.parser.ast.elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;

/**
 * A name that is created from many other names.
 * @author Benjamin Sigg
 *
 */
public class CombinedName extends Name{
    public static final IGenericFactory<CombinedName> FACTORY = new ReferenceFactory<CombinedName>( Name.FACTORY ){
        public CombinedName create(){
            return new CombinedName();
        }
        
        @Override
        public void write( CombinedName value, IStorage storage ) throws IOException{
            super.write( value, storage );
            storage.out().writeInt( value.names.length );
            for( Name name : value.names ){
                storage.write( name );
            }
        }
        
        @Override
        public CombinedName read( CombinedName value, IStorage storage ) throws IOException{
            super.read( value, storage );
            int size = storage.in().readInt();
            Name[] names = new Name[ size ];
            for( int i = 0; i < size; i++ ){
                names[i] = storage.read();
            }
            value.names = names;
            return value;
        }
    };
    
    private Name[] names;
    
    private CombinedName(){
        // nothing
    }
    
    public CombinedName( LazyRangeDescription range, Name... names ){
        super( range );
        this.names = names;
    }
    
    @Override
    public void resolveRange(){
        super.resolveRange();
        for( Name name : names ){
            name.resolveRange();
        }
    }
    
    public int getNameCount(){
        return names.length;
    }
    
    public Name getName( int index ){
        return names[index];
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + Arrays.hashCode( names );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if( this == obj )
            return true;
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        final CombinedName other = (CombinedName)obj;
        if( !Arrays.equals( names, other.names ) )
            return false;
        return true;
    }

    @Override
    protected void segments( List<String> list ){
        for( Name name : names )
            name.segments( list );
    }
}
