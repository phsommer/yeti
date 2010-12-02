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
package tinyos.yeti.nesc12.ep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.nesc12.CancellationException;
import tinyos.yeti.nesc12.parser.ProjectDeclarationResolver;
import tinyos.yeti.nesc12.parser.SynchronizedDeclarationResolver;

public class NesC12ModelNodeFactory implements IASTModelNodeFactory{
    public int getVersion(){
        return 1;
    }
    
    public void write( IASTModelNode node, DataOutputStream out, ProjectTOS project, IProgressMonitor monitor ) throws IOException{
        try{
            if( monitor != null )
                monitor.beginTask( "Read", IProgressMonitor.UNKNOWN );
            
            IStorage storage = new NesC12GenericStorage( project, out, monitor );
            storage.write( node );
            
            if( monitor != null )
                monitor.done();
        }
        catch( CancellationException cancel ){
            if( monitor != null )
                monitor.done();
        }
    }
    
    public IASTModelNode read( int version, DataInputStream in, ProjectTOS project, IProgressMonitor monitor ) throws IOException{
        try{
            if( monitor != null )
                monitor.beginTask( "Read", IProgressMonitor.UNKNOWN );
            
            IStorage storage = new NesC12GenericStorage( project, in, monitor );
            IASTModelNode result = storage.read();
            
            if( result != null && project != null ){
            	ModelNode node = (ModelNode)result;
            	node.setDeclarationResolver( new SynchronizedDeclarationResolver( project, new ProjectDeclarationResolver( project ) ) );
            }
            
            if( monitor != null )
                monitor.done();
            
            return result;
        }
        catch( CancellationException cancel ){
            if( monitor != null )
                monitor.done();
            
            return null;
        }
    }
}
