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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.jobs.PublicJob;
import tinyos.yeti.model.ProjectModel;

public class ASTReferenceSearchQuery implements ISearchQuery{
	private ASTReferenceSearchResult result;
	
	private String name;
	private SearchScope scope;
	private IASTModelNodeFilter filter;
	
	public ASTReferenceSearchQuery( String name, SearchScope scope, IASTModelNodeFilter filter ){
		this.name = name;
		this.scope = scope;
		this.filter = filter;
		
		result = new ASTReferenceSearchResult( this );
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
		
		result.clear();
		
		try{
			scope.visit( new SearchScope.Visitor(){
				private Map<IASTModelPath, IASTModelNode> nodes = new HashMap<IASTModelPath, IASTModelNode>();
				
				public void visit( ProjectTOS project, IParseFile file, IProgressMonitor monitor ){
					run( file, nodes, monitor );
				}
			}, monitor );
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
	
	private void run( final IParseFile file, final Map<IASTModelPath, IASTModelNode> nodeCache, IProgressMonitor monitor ){
		if( monitor.isCanceled() ){
			return;
		}
		
		final ProjectTOS project = file.getProject();
		final ProjectModel model = project.getModel();
		model.runJob( new PublicJob( "Search" ){
			@Override
			public IStatus run( IProgressMonitor monitor ){
				monitor.beginTask( "Resolve references", 2 );
				
				IASTReference[] references = model.getReferences( file, new SubProgressMonitor( monitor, 1 ) );
				if( monitor.isCanceled() )
					return Status.CANCEL_STATUS;
				
				if( references != null ){
					IProgressMonitor refMonitor = new SubProgressMonitor( monitor, 1 );
					refMonitor.beginTask( "Resolve references", references.length );
					
					List<ASTReferenceSearchResult.Result> results = new ArrayList<ASTReferenceSearchResult.Result>();
					
					for( IASTReference reference : references ){
						IASTModelNode node = nodeCache.get( reference.getTarget() );
						if( node == null ){
							node = model.getNode( reference.getTarget(), new SubProgressMonitor( refMonitor, 1 ) );
							nodeCache.put( reference.getTarget(), node );
						}
						
						if( monitor.isCanceled() )
							return Status.CANCEL_STATUS;
						
						if( node != null && filter.include( node )){
							results.add( new ASTReferenceSearchResult.Result( node, reference ) );
						}
					}
					
					if( results.size() > 0 ){
						result.add( results.toArray( new ASTReferenceSearchResult.Result[ results.size() ] ) );
					}
					
					refMonitor.done();
				}
				
				monitor.done();
				return Status.OK_STATUS;
			}
		}, monitor );
	}

}
