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
package tinyos.yeti.utility;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.TinyOSCore;

/**
 * A listener intended for {@link TinyOSProjects}. The methods of this listener
 * can be called from any thread and should execute fast, most clients should
 * just start a {@link Job} that further investigates the project.
 * @author Benjamin Sigg
 *
 */
public interface TinyOSProjectsListener{
	/**
	 * Called if <code>project</code> is newly created or has been opened.
	 * @param project an open project with nature {@link TinyOSCore#NATURE_ID}.
	 */
	public void projectAdded( IProject project );

	/**
	 * Called if <code>project</code> gets deleted or closed. The project
	 * may already be closed when this method is called.
	 * @param project the removed project wich had nature {@link TinyOSCore#NATURE_ID}.
	 */
	public void projectRemoved( IProject project );
}
