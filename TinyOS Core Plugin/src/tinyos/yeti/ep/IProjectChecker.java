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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.model.ProjectChecker;

/**
 * A {@link IProjectChecker} checks the correctness of the settings of a
 * project. Any client can ask a project to be checked again using
 * {@link ProjectChecker#recheck(IProject)}.
 * @author Benjamin Sigg
 */
public interface IProjectChecker{
	/**
	 * Connects this checker with <code>checker</code>. 
	 * @param checker the owner of this element
	 */
	public void connect( ProjectChecker checker );
	
	/**
	 * Called if this checker should check the properties of <code>project</code>
	 * again. This method can report errors, warnings and messages through
	 * <code>callback</code>.
	 * @param project the project to check
	 * @param monitor to report progress
	 * @param callback to add new message to <code>project</code>
	 */
	public void checkProject( IProject project, IProgressMonitor monitor, IProjectCheckerCallback callback ) throws CoreException;
}
