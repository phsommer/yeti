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
package tinyos.yeti.ep;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.model.ProjectModel;

/**
 * Used by a {@link NesCEditor} to read data from a {@link IEditorInput}.
 * @author Benjamin Sigg
 */
public interface IEditorInputConverter{
	/**
	 * Tells whether this {@link IEditorInputConverter} can read
	 * data from <code>input</code>.
	 * @param input the input to read
	 * @return <code>true</code> if <code>input</code> can be read
	 */
	public boolean matches( IEditorInput input );
	
	/**
	 * Tells to which project <code>input</code> belongs.
	 * @param input some input {@link #matches(IEditorInput) matching} this converter
	 * @return its project or <code>null</code>
	 */
	public IProject getProject( IEditorInput input );
	
	/**
	 * Tells which file <code>input</code> represents.
	 * @param input some input {@link #matches(IEditorInput) matching} this converter
	 * @param model the model to resolve paths
	 * @return the file or <code>null</code>
	 */
	public IParseFile getFile( IEditorInput input, ProjectModel model );
	
	/**
	 * Gets the resource which is represented by <code>input</code>.
	 * @param input some input {@link #matches(IEditorInput) matching} this converter
	 * @return the resource, can be <code>null</code>
	 */
	public IResource getResource( IEditorInput input );
}
