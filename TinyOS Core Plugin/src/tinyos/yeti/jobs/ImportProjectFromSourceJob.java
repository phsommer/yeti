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

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.targets.MakeTargetSkeleton;
import tinyos.yeti.utility.ProjectTOSUtility;

/**
 * This job first creates an empty project, then copies some directory
 * full of source files into the projects source directory.  
 * @author Benjamin Sigg
 */
public class ImportProjectFromSourceJob extends CancelingJob{
    private String directory;
    private String project;
    private IEnvironment environment;
    private IPlatform platform;

    public ImportProjectFromSourceJob( String directory, String project, IEnvironment environment, IPlatform platform ){
        super( "Import '" + directory + "' as '" + project + "'" );

        this.directory = directory;
        this.project = project;
        this.environment = environment;
        this.platform = platform;
    }

    @Override
    public IStatus run( IProgressMonitor monitor ){
        ProjectTOS handle = null;
        try{
            // count expected number of files to copy
            File directory = new File( this.directory ); 
            monitor.beginTask( "Import files", 400 );

            // setup empty project
            monitor.subTask( "Setup project '" + project + "'" );
            handle = ProjectTOSUtility.createEmptyProject( project, new SubProgressMonitor( monitor, 50 ));
            if( monitor.isCanceled() ){
                monitor.done();
                return Status.CANCEL_STATUS;
            }

            // copy files
            byte[] makefile = ProjectTOSUtility.copyToProject( handle, directory, new SubProgressMonitor( monitor, 300 ) );
            monitor.done();
            if( monitor.isCanceled() ){
                return Status.CANCEL_STATUS;
            }

            MakeTargetSkeleton skeleton;
            if( makefile == null ){
            	skeleton = ProjectTOSUtility.readMakefile( handle, platform == null ? null : platform.getName(), null );
            }
            else{
            	skeleton = ProjectTOSUtility.readMakefile( handle, platform == null ? null : platform.getName(), new String( makefile ) );
            }
            
            // complete setup
            ProjectTOSUtility.doDefaultSetup( handle, environment, skeleton, new SubProgressMonitor( monitor, 50 ) );
            if( monitor.isCanceled() ){
                monitor.done();
                return Status.CANCEL_STATUS;
            }
            
            
            return Status.OK_STATUS;
        }
        catch( CoreException ex ){
            monitor.done();
            return ex.getStatus();
        }
        finally{
            if( handle != null ){
                handle.initialize();
            }
        }
    }    
}
