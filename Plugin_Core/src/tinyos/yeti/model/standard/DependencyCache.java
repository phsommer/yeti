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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.model.ProjectModel;

public class DependencyCache extends StandardFileCache<Set<IParseFile>>{
    public DependencyCache( ProjectModel model, String extension, IStreamProvider streams ){
        super( model, extension, streams );
    }
    

    public Set<IParseFile> readCache( IParseFile file, IProgressMonitor monitor ) throws IOException, CoreException{
    	InputStream input = read( file );
    	if( input == null )
            throw new IOException( "file does not exist" );
        
        DataInputStream in = new DataInputStream( input );
        
        int size = in.readInt();
        monitor.beginTask( "Read wiring", size );
        Set<IParseFile> result = new HashSet<IParseFile>();
        for( int i = 0; i < size; i++ ){
            File next = new File( in.readUTF() );
            IParseFile check = model.parseFile( next );
            if( check != null ){
                result.add( check );
            }
            monitor.worked( 1 );
        }
        
        in.close();
        monitor.done();
        return result;
    }
    
    public void writeCache( IParseFile file, Set<IParseFile> dependencies, IProgressMonitor monitor ) throws IOException, CoreException{
    	OutputStream output = write( file );
    	if( output == null ){
            monitor.beginTask( "Write wiring", 1 );
            monitor.done();
            return;
        }
        
        DataOutputStream out = new DataOutputStream( output );
        
        int size = dependencies == null ? 0 : dependencies.size();
        int clicks = 2*size;
        monitor.beginTask( "wiring", clicks );
        
        out.writeInt( size );
        if( dependencies != null ){
            for( IParseFile next : dependencies ){
                File real = next.toFile();
                out.writeUTF( real.getAbsolutePath() );
                
                monitor.worked( 1 );
                clicks--;
            }
        }
        
        out.close();
        
        if( monitor.isCanceled() ){
        	clearCache( file, new SubProgressMonitor( monitor, clicks ));
        }
        
        monitor.done();
    }
    
}
