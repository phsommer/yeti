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

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;

/**
 * A declaration resolver that works only on a single model.
 * @author Benjamin Sigg
 */
public class ModelDeclarationResolver implements DeclarationResolver {
    private ASTModel model;
    
    public ModelDeclarationResolver( ASTModel model ){
        this.model = model;
    }
    
    public void parsingFinished(){
        // nothing
    }
    
    public IDeclaration resolve( String name, IProgressMonitor monitor, Kind... kind ) {
    	if( monitor != null ){
    		monitor.beginTask( "none", 1 );
    		monitor.done();
    	}
        return null;
    }
    
    public IDeclaration[] resolveAll( IProgressMonitor monitor, Kind... kind ){
    	if( monitor != null ){
    		monitor.beginTask( "none", 1 );
    		monitor.done();
    	}
    	return null;
    }

    public ModelNode resolve( ModelConnection connection, IProgressMonitor monitor ) {
        if( monitor != null )
            monitor.beginTask( "Resolve", 1 );
        
        ModelNode result = (ModelNode)model.getNode( connection );
        if( monitor != null )
            monitor.done();
        
        return result;
    }

    public ModelNode resolve( IDeclaration declaration, IProgressMonitor monitor ) {
        return resolve( declaration.getPath(), monitor );
    }

    public ModelNode resolve( IASTModelPath path, IProgressMonitor monitor ) {
        if( monitor != null )
            monitor.beginTask( "Resolve", 1 );
        
        ModelNode result = (ModelNode)model.getNode( path );
        if( monitor != null )
            monitor.done();
        
        return result;
    }
    
    public IASTModelPath resolvePath( ModelConnection connection, IProgressMonitor monitor ){
        IASTModelPath path = connection.getReferencedPath();
        if( path != null )
            return path;
        
        ModelNode node = resolve( connection, monitor );
        if( node != null )
            return node.getPath();
        
        return null;
    }
}
