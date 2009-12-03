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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeFactory;
import tinyos.yeti.model.IASTModelFileCache;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.model.SubASTModel;

/**
 * A cache for storing {@link IASTModelNode}s.
 * @author Benjamin Sigg
 */
public class ModelNodeFileCache extends StandardFileCache<SubASTModel> implements IASTModelFileCache{
    private IASTModelNodeFactory factory;
    
    public ModelNodeFileCache( ProjectModel model, IASTModelNodeFactory factory, String extension ){
        super( model, extension );
        
        if( factory == null )
            throw new IllegalArgumentException( "factory must not be null" );
        
        this.factory = factory;
    }
    
    public void writeCache( IParseFile file, SubASTModel value, IProgressMonitor monitor ) throws IOException, CoreException{
        IFile cache = getCacheFile( file );
        if( cache == null )
        	return;
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( bout );
        
        out.writeBoolean( value.isFullyLoaded() );
        
        ProjectTOS project = model.getProject();
        IASTModelNode[] nodes = value.getNodes();
        out.writeInt( nodes.length );
        monitor.beginTask( "Write Cache", 10 * nodes.length + 100 );

        out.writeInt( factory.getVersion() );

        for( IASTModelNode node : nodes ){
            factory.write( node, out, project, new SubProgressMonitor( monitor, 10 ) );
            if( monitor.isCanceled() ){
                monitor.done();
                return;
            }
        }
        
        out.close();
        ByteArrayInputStream in = new ByteArrayInputStream( bout.toByteArray() );
        create( cache, in, new SubProgressMonitor( monitor, 100 ) );
        monitor.done();
    }
    
    public boolean isFullyLoaded( IParseFile file ){
        IFile cache = getCacheFile( file );
        if( cache == null || !cache.exists() )
            return false;
        
        try{
            DataInputStream in = new DataInputStream( cache.getContents() );
            boolean result = in.readBoolean();
            in.close();
            return result;
        }
        catch ( CoreException e ){
            TinyOSPlugin.warning( e.getStatus() );
            return false;
        }
        catch ( IOException e ){
            e.printStackTrace();
            return false;
        }
    }
    
    public SubASTModel readCache( IParseFile file, IProgressMonitor monitor ) throws IOException, CoreException{
        IFile cache = getCacheFile( file );
        DataInputStream in = new DataInputStream( new BufferedInputStream( cache.getContents() ));
        
        boolean fullyLoaded = in.readBoolean();
        
        int size = in.readInt();
        int version = in.readInt();
        ProjectTOS project = model.getProject();
        monitor.beginTask( "Read Cache", 10 * size );
        
        IASTModelNode[] list = new IASTModelNode[ size ];
        for( int i = 0; i < size; i++ ){
            list[i] = factory.read( version, in, project, new SubProgressMonitor( monitor, 10 ) );
            if( monitor.isCanceled() ){
                monitor.done();
                in.close();
                return null;
            }
        }
        
        in.close();
        monitor.done();
        return new SubASTModel( fullyLoaded, list );
    }
}
