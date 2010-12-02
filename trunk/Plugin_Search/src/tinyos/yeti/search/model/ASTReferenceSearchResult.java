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
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.search.model.group.reference.ReferenceContentProvider;
import tinyos.yeti.search.ui.event.ReferencesAddedSearchResultEvent;
import tinyos.yeti.search.ui.event.ReferencesRemovedSearchResultEvent;

/**
 * An {@link ISearchResult} for storing the result of a search for references
 * pointing to a specific element.
 * @author besigg
 */
public class ASTReferenceSearchResult implements ITinyOSSearchResult{
	private ASTReferenceSearchQuery query;
	private List<ISearchResultListener> listeners = new ArrayList<ISearchResultListener>();
	private List<Result> results = new ArrayList<Result>();
	
	public ASTReferenceSearchResult( ASTReferenceSearchQuery query ){
		this.query = query;
	}
	
	public synchronized void addListener( ISearchResultListener l ){
		if( !listeners.contains( l ))
			listeners.add( l );
	}
	
	public synchronized void removeListener( ISearchResultListener l ){
		listeners.remove( l );
	}
	
	protected void fire(){
		fire( new SearchResultEvent( this ){} );
	}	
	
	protected void fire( SearchResultEvent event ){
		for( ISearchResultListener listener : listeners.toArray( new ISearchResultListener[ listeners.size() ] )){
			listener.searchResultChanged( event );
		}
	}
	
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
	
	public int size(){
		return results.size();
	}
	
	public Result getResult( int index ){
		return results.get( index );
	}
	
	public Result[] toArray(){
		return results.toArray( new Result[ results.size() ] );
	}
	
	public synchronized void clear(){
		results.clear();
		fire();
	}
	
	public synchronized void add( Result... results ){
		
		for( int i = 0; i < results.length; i++ ){
			Result result = results[i];
		
			this.results.add( result );
		}
		fire( new ReferencesAddedSearchResultEvent( this, results ));
	}
	
	public synchronized void remove( Result... results ){
		for( Result result : results ){
			this.results.remove( result );
		}
		
		fire( new ReferencesRemovedSearchResultEvent( this, results ) );
	}
	
	public synchronized void connect( ISearchTreeContentProvider provider, ISearchResultListener listener ){
		addListener( listener );
		if( provider instanceof ReferenceContentProvider ){
			ReferenceContentProvider content = (ReferenceContentProvider)provider;
			content.addReferences( toArray() );
		}
	}
	
	public static class Result{
		private IASTModelNode node;
		private IASTReference reference;
		
		public Result( IASTModelNode node, IASTReference reference ){
			if( node == null )
				throw new IllegalArgumentException( "node must not be null" );
			if( reference == null )
				throw new IllegalArgumentException( "reference must not be null" );
			this.node = node;
			this.reference = reference;
		}
		
		/**
		 * Gets the node to which this result points.
		 * @return the resolved target of this result
		 */
		public IASTModelNode getNode(){
			return node;
		}
		
		public IASTReference getReference(){
			return reference;
		}
	}
}
