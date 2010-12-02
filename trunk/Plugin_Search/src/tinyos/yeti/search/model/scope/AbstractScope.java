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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.search.model.SearchScope;

public abstract class AbstractScope implements SearchScope{
	protected void visit( Visitor visitor, IProject project, IProgressMonitor monitor ) throws CoreException {
		if( project.isAccessible() && project.hasNature( TinyOSCore.NATURE_ID ) ){
			try{
	 			ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
	 			visit( visitor, tos, new SubProgressMonitor( monitor, 1 ));
			}
			catch( MissingNatureException ex ){
				// never happens, or else is no problem
			}
 		}
	}
	
	protected void visit( Visitor visitor, ProjectTOS project, IProgressMonitor monitor ) throws CoreException {
		final List<IFile> files = new ArrayList<IFile>();
		
		project.getProject().accept( new IResourceVisitor(){
			public boolean visit( IResource resource ) throws CoreException{
				if( resource instanceof IFile ){
					files.add( (IFile)resource );
				}
				return true;
			}
		});
		
		ProjectModel model = project.getModel();
		
		monitor.beginTask( "Visit", files.size() );
		for( IFile file : files ){
			String name = file.getName();
			IParseFile parseFile = null;
			
			if( name.endsWith( ".h" ) || name.endsWith( ".nc" )){
				parseFile = model.parseFile( file );
			}
			
		 	if( parseFile != null ){
		 		visitor.visit( project, parseFile, new SubProgressMonitor( monitor, 1 ) );
		 	}
		 	else{
		 		monitor.worked( 1 );
		 	}
		 	if( monitor.isCanceled() ){
		 		monitor.done();
		 		return;
		 	}
		}
		
		monitor.done();
	}
	
	protected void visit( Visitor visitor, IFile file, IProgressMonitor monitor ) throws CoreException{
		monitor.beginTask( "Visit", 1 );
		
		if( file.isAccessible() ){
			String name = file.getName();
			if( name.endsWith( ".h" ) || name.endsWith( ".nc" )){
				IProject project = file.getProject();
				if( project.isAccessible() && project.hasNature( TinyOSCore.NATURE_ID )){
					try{
						ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
						IParseFile parseFile = tos.getModel().parseFile( file );
						if( parseFile != null ){
							visitor.visit( tos, parseFile, new SubProgressMonitor( monitor, 1 ) );
						}
					}
					catch( MissingNatureException ex ){
						// never happens, or else is no problem
					}
			    }
			}
		}
		
		monitor.done();
	}
	
	protected void addToCollection( IResource resource, final Collection<IFile> files ) throws CoreException{
		resource.accept( new IResourceVisitor(){
			public boolean visit( IResource resource ) throws CoreException{
				if( resource instanceof IFile ){
					files.add( (IFile)resource );
				}
				return true;
			}
		});
	}
}
