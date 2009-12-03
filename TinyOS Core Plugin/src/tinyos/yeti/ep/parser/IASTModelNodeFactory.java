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
package tinyos.yeti.ep.parser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ProjectTOS;

/**
 * A factory that can read and write {@link IASTModelNode}s.
 * @author Benjamin Sigg
 */
public interface IASTModelNodeFactory{
    /**
     * Gets the current version of this factory. The meaning of this number is
     * up to the factory, but a good suggestion would be that as higher the number
     * as newer the factory.
     * @return the version number
     */
    public int getVersion();
    
    /**
     * Writes <code>node</code> into <code>out</code>, also all
     * {@link IASTModelNodeConnection}s of that node have to be stored.
     * @param node the node to write
     * @param out the stream to write into
     * @param project the project for which the declaration is used
     * @param monitor to interact with the operation
     * @throws IOException forwarded from <code>out</code>
     */
    public void write( IASTModelNode node, DataOutputStream out, ProjectTOS project, IProgressMonitor monitor ) throws IOException;
    
    /**
     * Reads a node from <code>in</code>. This method must read or skip
     * exactly as many bytes as were written.
     * @param version the version of the factory which wrote the content of <code>in</code>
     * @param in the stream to write from
     * @param project the project for which the declaration is needed
     * @param monitor to interact with the operation
     * @return the declaration that was read
     * @throws IOException forwarded from <code>in</code> or if the version is
     * unknown
     */
    public IASTModelNode read( int version, DataInputStream in, ProjectTOS project, IProgressMonitor monitor ) throws IOException;
}
