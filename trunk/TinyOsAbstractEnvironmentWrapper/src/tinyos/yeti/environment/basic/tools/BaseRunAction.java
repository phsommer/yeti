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
package tinyos.yeti.environment.basic.tools;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.xml.sax.SAXException;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.environment.basic.AbstractEnvironment;
import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.utility.XReadStack;

public abstract class BaseRunAction implements IObjectActionDelegate{
    private IWorkbenchPart workbenchPart;

    protected abstract IResource getFileOrProject( ISelection selection );
    
    protected abstract void run( ProjectTOS project, XReadStack xml ) throws CoreException, IOException;
    
    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
        workbenchPart = targetPart;
    }

    public void run(IAction action) {
        ISelectionProvider selectionProvider = null;
        selectionProvider = workbenchPart.getSite().getSelectionProvider();

        ISelection selection = selectionProvider.getSelection();
        IResource resource = getFileOrProject( selection );

        if( resource instanceof IFile ){
            try{
                ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( resource.getProject() );
                
                IFile file = (IFile)resource;
                InputStream in = file.getContents();
                XReadStack xml = new XReadStack( in );
                in.close();
                
                run( tos, xml );
            }
            catch( MissingNatureException ex ){
            	TinyOSCore.inform( "?", ex );
            }
            catch( CoreException e ){
                TinyOSAbstractEnvironmentPlugin.getDefault().getLog().log( e.getStatus() );
            }
            catch( SAXException e ){
                TinyOSAbstractEnvironmentPlugin.getDefault().getLog().log(
                        new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, e.getMessage(), e ) );
            }
            catch( IOException e ){
                TinyOSAbstractEnvironmentPlugin.getDefault().getLog().log(
                        new Status( IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, e.getMessage(), e ) );
            }
        }
    }

    public void selectionChanged( IAction action, ISelection selection ){
        IResource resource = getFileOrProject( selection );
        IProject project = resource == null ? null : resource.getProject();

        if( project == null ){
            action.setEnabled( false );
        }
        else{
        	try{
	            ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
	            if( tos.getEnvironment() instanceof AbstractEnvironment ){
	                action.setEnabled( resource instanceof IFile );
	            }
	            else{
	                action.setEnabled( false );
	            }
        	}
        	catch( MissingNatureException ex ){
        		action.setEnabled( false );
        	}
        }
    }
}
