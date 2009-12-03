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
package tinyos.yeti.search.model.group.reference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.search.model.ISearchTreeContentProvider;
import tinyos.yeti.search.model.ASTReferenceSearchResult.Result;
import tinyos.yeti.search.model.group.GroupGenerator;
import tinyos.yeti.search.ui.event.ReferencesAddedSearchResultEvent;
import tinyos.yeti.search.ui.event.ReferencesRemovedSearchResultEvent;

public class ReferenceContentProvider implements ISearchTreeContentProvider{
	private List<Result> references = new ArrayList<Result>();
	
	private AddReferenceJob addJob = new AddReferenceJob();
	private TreeViewer viewer;
	private GroupGenerator<?, Result> group;
	
	public ReferenceContentProvider( GroupGenerator<?, Result> group ){
		this.group = group;
	}
	
	public void addReferences( Result[] references ){
		addJob.schedule( references );
	}
	
	public void removeReferences( Result[] references ){
		for( Result result : references ){
			this.references.remove( result );
		}
		removed( references );
	}
	
	public Object[] getChildren( Object parentElement ){
		return null;
	}

	public Object getParent( Object element ){
		return null;
	}

	public boolean hasChildren( Object element ){
		return false;
	}

	public boolean handle( SearchResultEvent event ){
		if( event instanceof ReferencesAddedSearchResultEvent ){
			Result[] references = ((ReferencesAddedSearchResultEvent)event).getReferences();
			addReferences( references );
			return true;
		}
		if( event instanceof ReferencesRemovedSearchResultEvent ){
			Result[] references = ((ReferencesRemovedSearchResultEvent)event).getReferences();
			removeReferences( references );
			return true;
		}
			
		return false;
	}
	
	public Object[] getElements( Object inputElement ){
		if( inputElement instanceof Object[] )
			return (Object[])inputElement;
		
		return new Object[]{};
	}

	public void dispose(){
		// ignore
	}

	@SuppressWarnings("unchecked")
	public void inputChanged( Viewer viewer, Object oldInput, Object newInput ){
		if( newInput instanceof List ){
			references = (List<Result>)newInput;
		}
		else{
			references = new ArrayList<Result>();
		}
		
		if( newInput == null )
			this.viewer = null;
		else
			this.viewer = (TreeViewer)viewer;
	}
	
	protected void added( Result[] items ){
		if( group == null ){
			if( viewer != null && !viewer.getTree().isDisposed() ){
				viewer.add( viewer.getInput(), items );	
			}
		}
		else{
			for( Result item : items ){
				group.insert( item );
			}
		}
	}
	
	protected void removed( Result[] items ){
		if( group == null ){
			if( viewer != null && !viewer.getTree().isDisposed() ){
				viewer.remove( viewer.getInput(), items );	
			}
		}
		else{
			for( Result item : items ){
				group.remove( item );
			}
		}
	}
	
	private class AddReferenceJob extends UIJob{
		private List<Result> items = new ArrayList<Result>();
		
		public AddReferenceJob(){
			super( "Add reference" );
			setSystem( true );
			setPriority( DECORATE );
		}
		
		public void schedule( Result[] references ){
			if( references.length > 0 ){
				synchronized( items ){
					for( Result result : references ){
						items.add( result );
					}
					schedule();
				}
			}
		}
		
		@Override
		public IStatus runInUIThread( IProgressMonitor monitor ){
			monitor.beginTask( "Add references", 1 );
			
			Result[] newItems;
			
			synchronized( items ){
				newItems = items.toArray( new Result[ items.size() ] );
				items.clear();
			}
			
			for( Result item : newItems ){
				ReferenceContentProvider.this.references.add( item );
			}
			
			added( newItems );
			
			monitor.done();
			return Status.OK_STATUS;
		}
	}
}
