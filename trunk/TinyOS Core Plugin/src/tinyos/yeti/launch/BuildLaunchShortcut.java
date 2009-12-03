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
package tinyos.yeti.launch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.nature.MissingNatureException;

public class BuildLaunchShortcut implements ILaunchShortcut{
	public void launch( ISelection selection, String mode ){
		if( !(selection instanceof IStructuredSelection) ){
			noStartMessage( "Selection invalid, can't process selection of type '" + selection.getClass() + "'." );
			return;
		}
		
		IStructuredSelection structured = (IStructuredSelection)selection;
		if( structured.size() != 1 ){
			noStartMessage( "Please select exactly one element, not '" + structured.size() + "'." );
			return;
		}
		
		Object first = structured.getFirstElement();
		if( !(first instanceof IResource )){
			noStartMessage( "No resource (file, project) selected." );
			return;
		}
		
		launch( (IResource)first );
	}

	public void launch( IEditorPart editor, String mode ){
		IEditorInput input = editor.getEditorInput();
		IResource resource = (IResource)input.getAdapter( IResource.class );
		if( resource == null ){
			noStartMessage( "No resource (file, project) selected.");
			return;
		}
	
		launch( resource );
	}
	
	public void launch( IResource resource ){
		try{
			IProject project = resource.getProject();
			if( project == null || !project.hasNature( TinyOSCore.NATURE_ID ) ){
				noStartMessage( "No TinyOS-project selected." );
				return;
			}

			ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
			MakeTarget target = tos.getMakeTarget();
			if( target == null ){
				noStartMessage( "No make-options specified." );
				return;
			}

			target = target.copy();
			
			if( resource != null && resource instanceof IFile && "nc".equals( resource.getFileExtension() ) ){
				target.setCustomComponentFile( (IFile)resource );
				target.setUseLocalProperty( MakeTargetPropertyKey.COMPONENT_FILE, true );
				target.setUseDefaultProperty( MakeTargetPropertyKey.COMPONENT_FILE, false );
			}
			
			BuildLaunch launch = new BuildLaunch();
			launch.buildAsync( target );
		}
		catch( MissingNatureException ex ){
			TinyOSCore.inform( "build", ex );
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex.getStatus() );
		}
	}
	
	private void noStartMessage( String cause ){
		
	}
}
