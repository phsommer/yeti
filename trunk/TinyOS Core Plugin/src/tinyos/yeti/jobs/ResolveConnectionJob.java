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
package tinyos.yeti.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.model.ProjectModel;

public class ResolveConnectionJob extends FerryJob<IASTModelNode>{
    private ProjectModel model;
    private IASTModelNodeConnection connection;
    
    public ResolveConnectionJob( ProjectModel model, IASTModelNodeConnection connection ){
        super( "resolve connection" );
        if( model == null )
        	throw new IllegalArgumentException( "model must not be null" );
        if( connection == null )
        	throw new IllegalArgumentException( "connection must not be null" );
        
        this.model = model;
        this.connection = connection;
        setPriority( SHORT );
        setRule( model.getProject().getProject() );
    }
    
    @Override
    public IStatus run( IProgressMonitor monitor ){
        monitor.beginTask( "Search node for '" + connection.getLabel() + "'", 100 );
        content = model.getNode( connection, new SubProgressMonitor( monitor, 100 ) );
        monitor.done();
        return Status.OK_STATUS;
    }
}
