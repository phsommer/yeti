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
package tinyos.yeti.editors.editorInputConverters;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;

import tinyos.yeti.editors.ExternalEditorInput;
import tinyos.yeti.ep.IEditorInputConverter;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.model.ProjectModel;

public class ExternalEditorInputConverter implements IEditorInputConverter{
	public boolean matches( IEditorInput input ){
		return input instanceof ExternalEditorInput;
	}
	
	public IProject getProject( IEditorInput input ){
		if( input == null )
			return null;
		return ((ExternalEditorInput)input).getProject();
	}
	
	public IParseFile getFile( IEditorInput input, ProjectModel model ){
        ExternalEditorInput fileInput = (ExternalEditorInput)input;
        String fullPath = fileInput.getFullPath();
        if( fullPath != null ){
            File file = new File( fullPath );
            return model.parseFile( file );
        }
        return null;
	}
	
	public IResource getResource( IEditorInput input ){
		return null;
	}
}
