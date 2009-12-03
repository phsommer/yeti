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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;

/**
 * A declaration resolver that uses a list of other resolvers to find
 * the its result.
 * @author Benjamin Sigg
 */
public class ListDeclarationResolver implements DeclarationResolver{
    private DeclarationResolver[] delegates;
    
    public ListDeclarationResolver( DeclarationResolver... delegates ){
        this.delegates = delegates;
    }
    
    public void parsingFinished(){
        for( DeclarationResolver resolver : delegates ){
            resolver.parsingFinished();
        }
    }
    
    public IDeclaration resolve( String name, IProgressMonitor monitor, Kind... kind ){
    	if( monitor == null )
    		monitor = new NullProgressMonitor();
    	
    	monitor.beginTask( "Resolve '" + name + "'", delegates.length );
        for( DeclarationResolver delegate : delegates ){
            IDeclaration result = delegate.resolve( name, new SubProgressMonitor( monitor, 1 ), kind );
            if( result != null ){
            	monitor.done();
                return result;
            }
        }
        monitor.done();
        return null;
    }
    
    public IDeclaration[] resolveAll( IProgressMonitor monitor, Kind... kind ){
    	if( monitor == null )
    		monitor = new NullProgressMonitor();
    	
    	monitor.beginTask( "Resolve", delegates.length );
    	for( DeclarationResolver delegate : delegates ){
            IDeclaration[] result = delegate.resolveAll( new SubProgressMonitor( monitor, 1 ), kind );
            if( result != null ){
            	monitor.done();
                return result;
            }
        }
    	monitor.done();
        return null;	
    }

    public ModelNode resolve( ModelConnection connection, IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Resolve Declaration", delegates.length );
        
        for( DeclarationResolver delegate : delegates ){
            ModelNode result = delegate.resolve( connection, new SubProgressMonitor( monitor, 1 ) );
            if( result != null || monitor.isCanceled() ){
                monitor.done();
                return result;
            }
        }
        monitor.done();
        return null;
    }

    public ModelNode resolve( IDeclaration declaration, IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Resolve Declaration", delegates.length );
        
        for( DeclarationResolver delegate : delegates ){
            ModelNode result = delegate.resolve( declaration, new SubProgressMonitor( monitor, 1 ) );
            if( result != null || monitor.isCanceled() ){
                monitor.done();
                return result;
            }
        }
        monitor.done();
        return null;
    }

    public ModelNode resolve( IASTModelPath path, IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Resolve Declaration", delegates.length );
        
        for( DeclarationResolver delegate : delegates ){
            ModelNode result = delegate.resolve( path, new SubProgressMonitor( monitor, 1 ) );
            if( result != null || monitor.isCanceled() ){
                monitor.done();
                return result;
            }
        }
        monitor.done();
        return null;
    }
    
    public IASTModelPath resolvePath( ModelConnection connection,
            IProgressMonitor monitor ){

        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Resolve Declaration", delegates.length );
        
        for( DeclarationResolver delegate : delegates ){
            IASTModelPath result = delegate.resolvePath( connection, new SubProgressMonitor( monitor, 1 ) );
            if( result != null || monitor.isCanceled() ){
                monitor.done();
                return result;
            }
        }
        monitor.done();
        return null;
    }
}
