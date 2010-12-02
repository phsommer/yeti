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
package tinyos.yeti.search.model.group.declaration;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.search.model.group.Group;

public class ProjectGroupGenerator extends NodeGroupGenerator<Object, IASTModelNode>{

	@Override
	protected Group createGroupFor( Object groupKey ){
		if( groupKey instanceof IParseFile ){
			return FileGroupGenerator.createFileGroup( (IParseFile)groupKey, false );
		}
		
		if( groupKey instanceof ProjectTOS ){
			ProjectTOS project = (ProjectTOS)groupKey;
			Group group = new Group( project.getProject().getName(), getWorkbenchImage( project ), project );
			return group;
		}
		
		throw new IllegalArgumentException();
	}

	@Override
	protected Object[] getKeys( IASTModelNode node ){
		IParseFile file = null;
		IASTModelPath path = node.getLogicalPath();
		if( path != null ){
			file = path.getParseFile();
		}
		if( file == null ){
			file = node.getParseFile();
		}
		ProjectTOS project = file.getProject();
		
		if( project == null )
			return new Object[]{ file };
		else
			return new Object[]{ project, file };
	}
	
	public static Image getWorkbenchImage( Object object ){
		if( !(object instanceof IAdaptable )){
			return null;
		}
		
		IAdaptable adaptable = (IAdaptable)object;
		
		IWorkbenchAdapter wbAdapter= (IWorkbenchAdapter) adaptable.getAdapter(IWorkbenchAdapter.class);
		if (wbAdapter == null) {
			return null;
		}
		ImageDescriptor descriptor = wbAdapter.getImageDescriptor(adaptable);
		if (descriptor == null ){
			return null;
		}
		
		return NesCIcons.icons().get( descriptor, true );
	}
	
	public static ImageDescriptor getProjectImageDescriptor(){
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( 
				IDE.SharedImages.IMG_OBJ_PROJECT );
	}
}
