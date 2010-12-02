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
package tinyos.yeti.environment.basic.path.steps;

import java.io.File;

import org.eclipse.core.resources.IContainer;

import tinyos.yeti.environment.basic.path.IPathRequest;
import tinyos.yeti.environment.basic.path.IPathSet;
import tinyos.yeti.environment.basic.progress.ICancellation;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.make.MakeInclude.Include;

public class DirectivesStep extends AbstractSearchStep{
    private boolean system;

    public DirectivesStep( boolean system ){
        this.system = system;
    }
    
	public String getName(){
		return "DirectivesStep (system=" + system + ")";
	}
	
	private boolean include( MakeInclude directive ){
		if( system )
			return directive.getInclude() == Include.SYSTEM;
		else
			return directive.getInclude() == Include.SOURCE;
	}

    public void collect( IPathRequest request, IPathSet paths, ICancellation cancellation ){
        if( system && request.isNostdinc() )
            return;

        MakeInclude[] directives = request.getDirectives();
        if( directives == null )
            return;

        for( MakeInclude directive : directives ){
            if( include( directive ) ){
                String path = directive.getPath();

                boolean environment = path.startsWith( "/" ) || path.startsWith( "%T" );
                path = paths.replace( path );

                if( environment ){
                    File file = paths.getPathManager().modelToSystem( path );
                    if( file != null ){
                        collect( paths, file, directive.isRecursive(), cancellation );
                    }
                }
                else{
                    if( request.getProject() != null ){
                        for( IContainer source : request.getProject().getSourceContainers() ){
	                        if( source.exists() ){
	                            File file = new File( source.getLocation().toFile(), path );
	                            collect( paths, file, directive.isRecursive(), cancellation );
	                            if( cancellation.isCanceled() )
	                            	return;
	                        }
                        }
                    }
                }

                if( cancellation.isCanceled() )
                    return;
            }
        }
    }

    public void locate( String fileName, IPathRequest request, IPathSet paths, ICancellation cancellation ){
        if( (system && request.isNostdinc()) || (!system && request.isSystemFiles()) )
            return;

        MakeInclude[] directives = request.getDirectives();
        if( directives == null )
            return;

        for( MakeInclude directive : directives ){
            if( include( directive ) ){
                String path = directive.getPath();
                boolean environment = path.startsWith( "/" ) || path.startsWith( "%T" );
                path = paths.replace( path );

                if( environment ){
                    File file = paths.getPathManager().modelToSystem( path );
                    locate( directive, fileName, paths, file, cancellation );
                }
                else{
                    if( request.getProject() != null ){
                        for( IContainer source : request.getProject().getSourceContainers() ){
                        	if( source.exists() ){
                        		File file = new File( source.getLocation().toFile(), path );
                        		if( locate( directive, fileName, paths, file, cancellation )){
                        			return;
                        		}
                        	}
                        }
                    }
                }

                if( cancellation.isCanceled() )
                    return;
            }
        }
    }
    
    private boolean locate( MakeInclude directive, String fileName, IPathSet paths, File file, ICancellation cancellation ){
        if( file != null ){
            if( directive.isRecursive() ){
                if( locateRecursive( fileName, paths, file, cancellation ) )
                    return true;
            }
            else{
                if( locate( fileName, paths, file, cancellation ) )
                    return true;
            }
        }
        return false;
    }
}
