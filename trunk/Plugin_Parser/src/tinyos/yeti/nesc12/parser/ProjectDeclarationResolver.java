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
package tinyos.yeti.nesc12.parser;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;

/**
 * A declaration resolver that uses all the files of an {@link IProject}
 * to resolve elements.
 * @author Benjamin Sigg
 */
public class ProjectDeclarationResolver implements DeclarationResolver{
    private ProjectTOS project;
    
    public ProjectDeclarationResolver( ProjectTOS project ){
        this.project = project;
    }
    
    public void parsingFinished(){
        // nothing
    }
    
    public IDeclaration resolve( String name, IProgressMonitor monitor, Kind... kind ){
    	if( monitor == null )
    		monitor = new NullProgressMonitor();
    	
    	monitor.beginTask( "Resolve '" + name + "'", 1 );
    	if( project == null ){
        	monitor.done();
            return null;
        }
        
        IDeclaration declaration = project.getModel().getDeclaration( name, kind );
        monitor.done();
        return declaration;
    }
    
    public IDeclaration[] resolveAll( IProgressMonitor monitor, Kind... kind ){
    	if( monitor == null )
    		monitor = new NullProgressMonitor();
    	
    	monitor.beginTask( "Resolve", 10 );
        if( project == null )
            return null;
        
        List<IDeclaration> result = project.getModel().getDeclarations( kind );
        monitor.done();
        return result.toArray( new IDeclaration[ result.size() ] );
    }

    public ModelNode resolve( ModelConnection connection, IProgressMonitor monitor ) {
        if( project == null ){
            if( monitor != null )
                monitor.done();
            return null;
        }
        
        return (ModelNode)project.getModel().getNode( connection, monitor );
    }

    public ModelNode resolve( IDeclaration declaration, IProgressMonitor monitor ) {
        if( project == null ){
            if( monitor != null )
                monitor.done();
            return null;
        }
        
        return (ModelNode)project.getModel().getNode( declaration, monitor );
    }

    public ModelNode resolve( IASTModelPath path, IProgressMonitor monitor ) {
        if( project == null ){
            if( monitor != null )
                monitor.done();
            return null;
        }
        
        return (ModelNode)project.getModel().getNode( path, monitor );
    }

    public IASTModelPath resolvePath( ModelConnection connection, IProgressMonitor monitor ){
        IASTModelPath path = connection.getReferencedPath();
        if( path != null ){
            if( monitor != null )
                monitor.done();
            return path;
        }
        
        ModelNode node = resolve( connection, monitor );
        if( node != null )
            return node.getPath();
        
        return null;
    }
}
