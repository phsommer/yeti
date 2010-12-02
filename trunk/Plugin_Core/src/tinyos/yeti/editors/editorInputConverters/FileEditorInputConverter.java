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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import tinyos.yeti.ep.IEditorInputConverter;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.model.ProjectModel;

public class FileEditorInputConverter implements IEditorInputConverter{
	public boolean matches( IEditorInput input ){
		return input instanceof IFileEditorInput;
	}
	
	public IProject getProject( IEditorInput input ){
        IFileEditorInput fileInput = (IFileEditorInput)input;
        IFile file = fileInput.getFile();
        return file.getProject();
	}
	
	public IParseFile getFile( IEditorInput input, ProjectModel model ){
		IFileEditorInput fileInput = (IFileEditorInput)input;
        IFile file = fileInput.getFile();
        return model.parseFile( file );
	}
	
	public IResource getResource( IEditorInput input ){
		return ((IFileEditorInput)input).getFile();
	}
}
