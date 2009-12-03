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
package tinyos.yeti.search.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.ASTNodeFilterFactory;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.jobs.PublicJob;
import tinyos.yeti.model.ProjectModel;

/**
 * This query operates on the {@link IASTModel}s to find a specific set of nodes.
 * @author besigg
 *
 */
public class ASTNodeSearchQuery implements ISearchQuery{
	private ASTModelNodeSearchResult result;
	
	private String name;
	private SearchScope scope;
	private IASTModelNodeFilter filter;
	
	public ASTNodeSearchQuery( String name, SearchScope scope, IASTModelNodeFilter filter ){
		this.name = name;
		this.scope = scope;
		this.filter = filter;
		
		result = new ASTModelNodeSearchResult( this );
	}
	
	public boolean canRerun(){
		return true;
	}

	public boolean canRunInBackground(){
		return true;
	}

	public String getLabel(){
		return name;
	}

	public ISearchResult getSearchResult(){
		return result;
	}

	public IStatus run( IProgressMonitor monitor ){
		if( monitor == null )
			monitor = new NullProgressMonitor();
		
		monitor.beginTask( "Search", 100 );
		result.clear();
		try{
			final Set<IParseFile> files = new HashSet<IParseFile>();
			
			scope.visit( new SearchScope.Visitor(){
				public void visit( ProjectTOS project, IParseFile file, IProgressMonitor monitor ){
					files.add( file );
				}
			}, new SubProgressMonitor( monitor, 10 ) );
			
			IProgressMonitor runMonitor = new SubProgressMonitor( monitor, 90 );
			runMonitor.beginTask( "Search", files.size() );
			
			for( IParseFile file : files ){
				SubProgressMonitor call = new SubProgressMonitor( runMonitor, 1 );
				run( file, files, call );
				call.done();
			}
			
			runMonitor.done();
		}
		catch( CoreException ex ){
			monitor.done();
			return ex.getStatus();
		}

		monitor.done();
		if( monitor.isCanceled() )
			return Status.CANCEL_STATUS;
		
		return Status.OK_STATUS;
	}
	
	private void run( final IParseFile file, final Set<IParseFile> scope, IProgressMonitor monitor ){
		if( monitor.isCanceled() ){
			return;
		}
		
		final ProjectTOS project = file.getProject();
		final ProjectModel model = project.getModel();
		model.runJob( new PublicJob( "Search" ){
			@Override
			public IStatus run( IProgressMonitor monitor ){
				try{
					model.freeze( file );
					
					model.ensureInCache( file, true, monitor );
					IASTModel astModel = model.getCacheModel();
					
					final IASTModelNode[] results = astModel.getNodes( ASTNodeFilterFactory.and( ASTNodeFilterFactory.origin( file ), filter ) );
					if( results != null && results.length > 0 ){
						for( int i = 0; i < results.length; i++ ){
							IParseFile file = results[i].getLogicalPath().getParseFile();
							if( !scope.contains( file )){
								results[i] = null;
							}
						}

						result.addResults( project, results );
					}
				}
				finally{
					model.melt( file );
				}
				
				return Status.OK_STATUS;
			}
		}, monitor );
	}
}
