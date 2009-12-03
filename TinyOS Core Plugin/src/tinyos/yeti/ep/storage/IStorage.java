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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import tinyos.yeti.ProjectTOS;

/**
 * Storages can be used by clients to read and write contents of this plugin
 * to the disk. The kind of objects which can be stored with an {@link IStorage}
 * can be limited. Please refer to a concrete implementation of {@link IStorage}
 * to get an overview of its restrictions.
 * @author Benjamin Sigg
 * @see GenericStorage extensible standard implementation
 */
public interface IStorage{
    /**
     * Gets a stream to read from.
     * @return the stream to read from or <code>null</code> if currently not
     * reading.
     */
    public DataInputStream in();
    
    /**
     * Gets a stream to write into.
     * @return the stream to write or <code>null</code> if currently not
     * writing.
     */
    public DataOutputStream out();
    
    /**
     * Gets the project for which this storage is used
     * @return the project
     */
    public ProjectTOS getProject();
    
    /**
     * Stores <code>value</code> which can be <code>null</code>. How to
     * store the value is up to the implementation. If the implementation does
     * not know a way to store <code>value</code>, then an {@link IOException}
     * is thrown.
     * @param <V> the kind of value to store
     * @param value the value to store, may be <code>null</code>
     * @throws IOException forwarded from the underlying stream or if there
     * is no rule to store <code>value</code>
     */
    public <V> void write( V value ) throws IOException;
    
    /**
     * Stores <code>value</code> which can be <code>null</code>. The storage
     * has to use <code>factory</code> in order to store the value.
     * @param <V> the kind of value to store
     * @param value the value to store, may be <code>null</code>
     * @param factory the factory to use for storage
     * @throws IOException forwarded from the underlying stream or if there
     * is no rule to store <code>value</code>
     */
    public <V> void write( V value, IGenericFactory<V> factory ) throws IOException;
    
    /**
     * Reads a value from the stream and casts it to <code>V</code>.
     * @param <V> the kind of value that is expected
     * @return the value or <code>null</code> if <code>null</code> was stored
     * @throws IOException forwarded from the underlying stream
     */
    public <V> V read() throws IOException;

    /**
     * Reads a value from the stream and casts it to <code>V</code>.
     * @param <V> the kind of value that is expected
     * @param factory the factory to use for reading
     * @return the value or <code>null</code> if <code>null</code> was stored
     * @throws IOException forwarded from the underlying stream
     */
    public <V> V read( IGenericFactory<V> factory ) throws IOException;
    
    /**
     * Writes a possible <code>null</code> string.
     * @param string the string to store
     * @throws IOException forwarded from the underlying stream
     */
    public void writeString( String string ) throws IOException;
    
    /**
     * Reads a possible <code>null</code> string.
     * @return the String that was read or <code>null</code>
     * @throws IOException forwarded from the underlying stream
     */
    public String readString() throws IOException;
}
