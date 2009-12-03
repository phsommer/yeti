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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.search.model.group.declaration.SearchNodeContentProvider;
import tinyos.yeti.search.ui.event.ItemsAddedSearchResultEvent;
import tinyos.yeti.search.ui.event.ItemsRemovedSearchResultEvent;

/**
 * This {@link ISearchResult} contains a list of {@link IASTModelNode}s. 
 * @author besigg
 *
 */
public class ASTModelNodeSearchResult implements ITinyOSSearchResult{
	private List<ISearchResultListener> listeners = new ArrayList<ISearchResultListener>();
	
	private ISearchQuery query;
	
	private Map<ProjectTOS, ProjectNodes> nodes = new HashMap<ProjectTOS, ProjectNodes>();
	
	public ASTModelNodeSearchResult( ISearchQuery query ){
		this.query = query;
	}
	
	public synchronized void addListener( ISearchResultListener l ){
		if( !listeners.contains( l )){
			listeners.add( l );
		}
	}
	
	public synchronized void removeListener( ISearchResultListener l ){
		listeners.remove( l );
	}
	
	public synchronized void addResults( ProjectTOS project, IASTModelNode... results ){
		ProjectNodes nodes = this.nodes.get( project );
		if( nodes == null ){
			nodes = new ProjectNodes( project );
			this.nodes.put( project, nodes );
		}
		
		IASTModel model = nodes.toModel();
		List<IASTModelNode> added = new ArrayList<IASTModelNode>();
		
		for( IASTModelNode result : results ){
			if( result != null && nodes.add( result ) ){
				added.add( result );
			}
		}
		
		if( !added.isEmpty() ){
			SearchResultEvent event = new ItemsAddedSearchResultEvent( this, model, nodes.getProject().getModel(),
					added.toArray( new IASTModelNode[ added.size() ] ) );
			
			for( ISearchResultListener listener : listeners ){
				listener.searchResultChanged( event );
			}
		}
	}
	
	public synchronized void removeAll( Collection<IASTModelNode> nodes ){
		for( IASTModelNode node : nodes ){
			removeSilent( node );
		}
		
		// SearchResultEvent event = new SearchResultEvent( this ){};
		SearchResultEvent event = new ItemsRemovedSearchResultEvent( 
				this, nodes.toArray( new IASTModelNode[ nodes.size() ] ));
		
		for( ISearchResultListener listener : listeners ){
			listener.searchResultChanged( event );
		}
	}
	
	public synchronized void remove( IASTModelNode node ){
		removeSilent( node );

		// SearchResultEvent event = new SearchResultEvent( this ){};
		SearchResultEvent event = new ItemsRemovedSearchResultEvent(
				this, new IASTModelNode[]{ node });
		
		for( ISearchResultListener listener : listeners ){
			listener.searchResultChanged( event );
		}
	}
	
	private synchronized void removeSilent( IASTModelNode node ){
		Iterator<Map.Entry<ProjectTOS, ProjectNodes>> iterator = nodes.entrySet().iterator();
		while( iterator.hasNext() ){
			Map.Entry<ProjectTOS, ProjectNodes> entry = iterator.next();
			if( entry.getValue().remove( node )){
				if( entry.getValue().isEmpty() ){
					iterator.remove();
				}
			}
		}
	}
	
	public synchronized void clear(){
		nodes.clear();
		
		SearchResultEvent event = new SearchResultEvent( this ){};
		
		for( ISearchResultListener listener : listeners ){
			listener.searchResultChanged( event );
		}
	}
	
	public synchronized void connect( ISearchTreeContentProvider provider, ISearchResultListener listener ){
		addListener( listener );
		if( provider instanceof SearchNodeContentProvider ){
			SearchNodeContentProvider content = (SearchNodeContentProvider)provider;
			
			for( ProjectNodes node : nodes.values() ){
				IASTModel model = node.toModel();
				content.addModel( model, node.getProject().getModel() );
			}	
		}
	}
	
	/*
	public ProjectNodes[] getNodes(){
		return nodes.values().toArray( new ProjectNodes[ nodes.size() ] );
	}*/
	
	public ImageDescriptor getImageDescriptor(){
		return null;
	}

	public String getLabel(){
		return query.getLabel();
	}

	public ISearchQuery getQuery(){
		return query;
	}

	public String getTooltip(){
		return null;
	}
}
