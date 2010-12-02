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
import org.eclipse.core.runtime.SubProgressMonitor;

public class EnclosingProjectScope extends AbstractScope{
	private String[] names;
	
	public EnclosingProjectScope( String[] names ){
		this.names = names;
	}
	
	public void visit( Visitor visitor, IProgressMonitor monitor ) throws CoreException{
		if( names == null )
			return;
		
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects= new IProject[ names.length ];
		for (int i= 0; i < projects.length; i++) {
			projects[i]= root.getProject( names[i] );
		}
		
		monitor.beginTask( "Visit", projects.length );
		for( IProject project : projects ){
			visit( visitor, project, new SubProgressMonitor( monitor, 1 ) );
			if( monitor.isCanceled() ){
				break;
			}
		}
		monitor.done();
	}
	
	public String getDescription(){
		if( names.length == 0 )
			return "";
		if( names.length == 1 )
			return names[0];
		if( names.length == 2 )
			return names[0] + ", " + names[1];
		return names[0] + ", " + names[1] + ", ..."; 
	}
}
