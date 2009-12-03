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
package tinyos.yeti.ep.storage;

import java.io.IOException;

public abstract class GenericArrayFactory<V> implements IGenericFactory<V[]>{
    public V[] create(){
        return null;
    }

    public abstract V[] create( int size );
    
    public V[] read( V[] value, IStorage storage ) throws IOException{
        int size = storage.in().readInt();
        value = create( size );
        for( int i = 0; i < size; i++ ){
            value[i] = storage.read();
        }
        return value;
    }

    public void write( V[] value, IStorage storage ) throws IOException{
        int size = value.length;
        storage.out().writeInt( size );
        for( int i = 0; i < size; i++ ){
            storage.write( value[i] );
        }
    }
}
