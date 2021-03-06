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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.search.model.SearchScope;

/**
 * A wrapper around a {@link SearchScope}, ensures that each file
 * is only visited once and can later tell which {@link ProjectTOS}es
 * and files were visited.
 * @author Benjamin Sigg
 *
 */
public class RecordingScope implements SearchScope{
	private SearchScope scope;
	private Set<ProjectTOS> projects = new HashSet<ProjectTOS>();
	private Set<IParseFile> files = new HashSet<IParseFile>();
	
	public RecordingScope( SearchScope scope ){
		this.scope = scope;
	}
	
	public void visit( final Visitor visitor, IProgressMonitor monitor ) throws CoreException{
		scope.visit( new Visitor(){
			public void visit( ProjectTOS project, IParseFile file, IProgressMonitor monitor ){
				if( files.add( file )){
					projects.add( project );
					visitor.visit( project, file, monitor );
				}
				else{
					monitor.beginTask( "Visit", 1 );
					monitor.done();
				}
			}
		}, monitor );
	}
	
	public Set<IParseFile> getFiles(){
		return files;
	}
	
	public Set<ProjectTOS> getProjects(){
		return projects;
	}
	
	public void clear(){
		files.clear();
		projects.clear();
	}
	
	public String getDescription(){
		return scope.getDescription();
	}
}
