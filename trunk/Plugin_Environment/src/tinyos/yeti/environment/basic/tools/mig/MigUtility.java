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
package tinyos.yeti.environment.basic.tools.mig;

import java.io.FileNotFoundException;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import tinyos.yeti.Debug;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSConsole;
import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.environment.basic.AbstractEnvironment;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.nature.MissingNatureException;

public class MigUtility{
    public static void editMIG( IResource fileOrProject, Shell shell ) throws CoreException, FileNotFoundException {
        IProject project = fileOrProject.getProject();
        if( fileOrProject instanceof IFile )
            editMIG( project, (IFile)fileOrProject, shell );
        else
            editMIG( project, null, shell );
    }

    public static void editMIG( IProject project, IFile file, Shell shell ) throws CoreException, FileNotFoundException {
        Debug.info( "mig executed on "+project.getName() );
        try{
	        ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
	        IEnvironment environment = tos.getEnvironment();
	        if( environment instanceof AbstractEnvironment ){
	            MigDialog dialog = new MigDialog( shell );
	            dialog.setProject( tos );
	            MigSetting setting = dialog.openDialog( file );
	            if( setting != null ){
	                executeMIG( setting );
	            }    
	        }
        }
        catch( MissingNatureException ex ){
        	TinyOSCore.inform( "edit mig", ex );
        }
    }

    public static void executeMIG( final MigSetting setting ){
        Job job = new Job( "MIG" ){
            @Override
            protected IStatus run( IProgressMonitor monitor ){
                ProjectTOS tos = setting.getProject();
                IEnvironment environment = tos.getEnvironment();
                MigCommand command = new MigCommand( setting );

                TinyOSConsole console = TinyOSPlugin.getDefault().getConsole();
                console.setProject( tos );
                ((AbstractEnvironment)environment).execute( command, monitor, console.info(), console.out(), console.err() );
                
                return Status.OK_STATUS;
            }
        };
        
        job.setPriority( Job.LONG );
        job.schedule();
    }
    
    public static IResource getFileOrProject( ISelection selection ){
        if( !(selection instanceof StructuredSelection ))
            return null;

        StructuredSelection structured = (StructuredSelection)selection;

        Iterator iterator = structured.iterator();
        while (iterator.hasNext()) {
            //  obj => selected object in the view
            Object obj = iterator.next();

            // is it a resource
            if (obj instanceof IResource) {
                IResource resource = (IResource) obj;

                switch (resource.getType()) {
                    case IResource.FILE:
                        if( "mig".equals( resource.getFileExtension() )){
                            return resource;
                        }       
                        break;
                    case IResource.PROJECT:
                        return resource;
                }
            }
        }

        return null;
    }
}
