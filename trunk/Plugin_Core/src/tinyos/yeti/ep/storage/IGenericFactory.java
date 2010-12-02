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

/**
 * A generic factory can read or write some kind of class.
 * @author Benjamin Sigg
 *
 * @param <V> the type of object this factory can handle
 */
public interface IGenericFactory<V>{
    /**
     * Writes <code>value</code> into <code>out</code>.
     * @param value the value to write, never <code>null</code>
     * @param storage additional information
     * @throws IOException if the stream is not accessible
     */
    public void write( V value, IStorage storage ) throws IOException;
    
    /**
     * Creates a new empty data object.
     * @return the new empty data object or <code>null</code> if this kind of
     * object cannot be instantiated (either because it is abstract or because
     * there are informations required that are not yet available).
     */
    public V create();
    
    /**
     * Reads data from a stream and stores the data in <code>value</code>
     * @param value the value to write into, this might be <code>null</code> if
     * {@link #create()} returned <code>null</code> or from another factory
     * that uses this factory.
     * @param storage storage with additional information
     * @return either <code>value</code> or a newly created value or <code>null</code>
     * in case that a value can't be created. Returning <code>null</code> will
     * cause an exception by the client of this factory
     * @throws IOException if the stream is not accessible or the content invalid 
     */
    public V read( V value, IStorage storage ) throws IOException;
}
