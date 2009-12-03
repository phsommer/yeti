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
package tinyos.yeti.search.model.scope;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Scope for searching the whole workspace.
 * @author besigg
 */
public class WorkspaceScope extends AbstractScope{
	public void visit( Visitor visitor, IProgressMonitor monitor ) throws CoreException{
	 	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	 	IProject[] projects = root.getProjects();
	 	monitor.beginTask( "Search", projects.length );
	 	
	 	for( IProject project : projects ){
	 		visit( visitor, project, monitor );
	 	}
	 	
	 	monitor.done();
	}
	
	public String getDescription(){
		return "workspace";
	}
}
