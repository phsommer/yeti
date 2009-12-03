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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;
import tinyos.yeti.environment.basic.path.IPathRequest;
import tinyos.yeti.environment.basic.path.IPathSet;
import tinyos.yeti.environment.basic.progress.ICancellation;
import tinyos.yeti.ep.IEnvironment.SearchFlag;

public class ProjectStep extends AbstractSearchStep{
	public String getName(){
		return "ProjectStep";
	}
	
    public void collect( IPathRequest request, final IPathSet paths, final ICancellation cancellation ){
        if( request.isSystemFiles() )
            return;
        
        if( request.getFlags().contains( SearchFlag.EXCLUDE_PROJECT ))
        	return;

        ProjectTOS project = request.getProject();
        if( project == null )
            return;

        try{
            project.acceptSourceFiles( new IResourceVisitor(){
                public boolean visit( IResource resource ) throws CoreException{
                    if( cancellation.isCanceled() )
                        return false;

                    if( !resource.isTeamPrivateMember() ){
                        boolean file = resource instanceof IFile;
                        boolean folder = resource instanceof IFolder;

                        if( file || folder ){
                            IPath location = resource.getLocation();
                            if( location != null ){
                                File locatedFile = location.toFile();
                                if( file && paths.validFileExtension( locatedFile.getPath() )){
                                    paths.store( locatedFile );
                                }

                                if( folder ){
                                    if( !paths.setProcessed( locatedFile.getAbsolutePath() ))
                                        return false;
                                }
                            }
                        }
                    }

                    return true;
                }
            });
        }
        catch ( CoreException e ){
            TinyOSAbstractEnvironmentPlugin.warning( e.getStatus() );
        }
    }

    public void locate( final String fileName, IPathRequest request, IPathSet paths, ICancellation cancellation ){
        if( request.isSystemFiles() )
            return;
        
        if( request.getFlags().contains( SearchFlag.EXCLUDE_PROJECT ))
        	return;

        ProjectTOS project = request.getProject();
        if( project == null )
            return;
        
        for( IContainer container : project.getSourceContainers() ){
	        if( !container.exists() )
	            continue;
	
	        IPath path = container.getLocation();
	        if( path == null )
	        	continue;
	
	        File root = path.toFile();
	        if( root == null )
	        	continue;

	        if( locateRecursive( fileName, paths, root, cancellation ) ){
	        	return;
	        }
        }
    }
}
