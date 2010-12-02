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
package tinyos.yeti.model.standard;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.model.ProjectModel;

/**
 * A cache that can write and read generic content.
 * @author Benjamin Sigg
 *
 * @param <V> the content to write or read
 */
public class GenericFileCache<V> extends StandardFileCache<V>{
    public GenericFileCache( ProjectModel model, String extension, IStreamProvider streams ){
        super( model, extension, streams );
    }

    public V readCache( IParseFile file, IProgressMonitor monitor ) throws IOException, CoreException{
        InputStream cache = read( file );
        if( cache == null )
            throw new IOException( "file does not exist" );
        
        monitor.beginTask( "Read", 1000 );
        DataInputStream in = new DataInputStream( cache );
        IStorage storage = TinyOSPlugin.getDefault().getParserFactory().createStorage( model.getProject(), in, monitor );
        
        V result = storage.read();
        
        in.close();
        monitor.done();
        return result;
    }

    public void writeCache( IParseFile file, V value, IProgressMonitor monitor ) throws IOException, CoreException{
        OutputStream cache = write( file );
        if( cache == null )
            throw new IOException( "file can't be opened" );
        
        monitor.beginTask( "Read", 1000 );
        DataOutputStream out = new DataOutputStream( cache );
        
        IStorage storage = TinyOSPlugin.getDefault().getParserFactory().createStorage( 
                model.getProject(), out, new SubProgressMonitor( monitor, 900 ) );
        
        storage.write( value );
        out.close();
        
        if( monitor.isCanceled() ){
            clearCache( file, new SubProgressMonitor( monitor, 100 ) );
        }
        
        monitor.done();
    }
}
