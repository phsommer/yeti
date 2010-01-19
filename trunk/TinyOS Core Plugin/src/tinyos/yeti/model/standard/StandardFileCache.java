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

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.model.IFileCache;
import tinyos.yeti.model.ProjectModel;

public abstract class StandardFileCache<V> implements IFileCache<V>{
    protected ProjectModel model;
    private String extension;
    
    private IStreamProvider streams;
    
    public StandardFileCache( ProjectModel model, String extension, IStreamProvider streams ){
        this.model = model;
        this.extension = extension;
        this.streams = streams;
    }
    
    /**
     * Sets the stream provider used for accessing the cache.
     * @param streams the stream provider
     */
    public void setStreams( IStreamProvider streams ){
    	if( streams == null )
    		throw new IllegalArgumentException();
		this.streams = streams;
	}

    protected InputStream read( IParseFile file ) throws CoreException{
    	return streams.read( file, extension );
    }
    
    protected OutputStream write( IParseFile file ) throws CoreException{
    	return streams.write( file, extension );
    }
    
    public void clearCache( IParseFile file, IProgressMonitor monitor ){
    	streams.clear( file, extension, monitor );
    }
    
    public boolean canReadCache( IParseFile file ){
    	return streams.canRead( file, extension );
    }
}
