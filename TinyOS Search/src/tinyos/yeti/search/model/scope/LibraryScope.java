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

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.jobs.AllFilesJob;
import tinyos.yeti.search.model.SearchScope;

/**
 * A scope that searches the library of the {@link ProjectTOS}es that were
 * searched as well.
 * @author besigg
 */
public class LibraryScope implements SearchScope{
	private SearchScope scope;
	
	public LibraryScope( SearchScope scope ){
		this.scope = scope;
	}
	
	public void visit( Visitor visitor, IProgressMonitor monitor ) throws CoreException{
		RecordingScope scope = new RecordingScope( this.scope );
		monitor.beginTask( "Visit", 10 );
		scope.visit( visitor, new SubProgressMonitor( monitor, 3 ) );
		if( monitor.isCanceled() ){
			monitor.done();
			return;
		}
		
		Set<ProjectTOS> projects = scope.getProjects();
		Set<IParseFile> files = scope.getFiles();
		
		IProgressMonitor visitMonitor = new SubProgressMonitor( monitor, 7 );
		visitMonitor.beginTask( "Visit project", projects.size() );
		for( ProjectTOS project : projects ){
			visitAll( visitor, project, files, new SubProgressMonitor( visitMonitor, 1 ));
			if( monitor.isCanceled() ){
				break;
			}
		}
		monitor.done();
	}
	
	private void visitAll( Visitor visitor, ProjectTOS project, Set<IParseFile> visited, IProgressMonitor monitor ){
		monitor.beginTask( "Library", 2 );
		
		AllFilesJob job = new AllFilesJob( project, false, "All files" );
		project.getModel().runJob( job, new SubProgressMonitor( monitor, 1) );
		if( monitor.isCanceled() ){
			monitor.done();
			return;
		}
		
		List<IParseFile> files = job.getFiles();
		
		IProgressMonitor visitMonitor = new SubProgressMonitor( monitor, 1 );
		visitMonitor.beginTask( "Visit", files.size() );
		for( IParseFile file : files ){
			if( visited.add( file )){
				visitor.visit( project, file, new SubProgressMonitor( visitMonitor, 1 ) );
			}
			else{
				visitMonitor.worked( 1 );
			}
			if( monitor.isCanceled() ){
				break;
			}
		}
		
		monitor.done();
	}
	
	public String getDescription(){
		return scope.getDescription() + " (and library)";
	}
}
