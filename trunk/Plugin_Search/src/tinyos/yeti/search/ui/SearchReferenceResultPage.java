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
package tinyos.yeti.search.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;

import tinyos.yeti.search.model.ASTReferenceSearchResult;
import tinyos.yeti.search.model.ISearchTreeContentProvider;
import tinyos.yeti.search.model.ASTReferenceSearchResult.Result;
import tinyos.yeti.search.model.group.DelegateBinder;
import tinyos.yeti.search.model.group.Group;
import tinyos.yeti.search.model.group.GroupGenerator;
import tinyos.yeti.search.model.group.reference.FileGroupGenerator;
import tinyos.yeti.search.model.group.reference.ProjectGroupGenerator;
import tinyos.yeti.search.model.group.reference.ReferenceContentProvider;
import tinyos.yeti.search.model.group.reference.ReferenceDelegateBinder;
import tinyos.yeti.search.model.group.reference.ReferenceLabelProvider;
import tinyos.yeti.search.model.group.reference.TargetGroupGenerator;
import tinyos.yeti.search.model.group.reference.TargetProjectGroupGenerator;

public class SearchReferenceResultPage extends SearchResultPage<ASTReferenceSearchResult.Result>{
	public SearchReferenceResultPage(){
		super( "search.reference" );
		addGroup( "file", "Group by file", null, new FileGroupGenerator() );
		addGroup( "project", "Group by project", null, new ProjectGroupGenerator() );
		addGroup( "target", "Group by target", null, new TargetGroupGenerator() );
		addGroup( "target_project", "Group by target and project", null, new TargetProjectGroupGenerator() );
		
		addGroup( "null", "Do not group", null, null );
	}
	
	@Override
	protected DelegateBinder createBinder( ITreeContentProvider provider,
			GroupGenerator<?, Result> generator ){
		
		return new ReferenceDelegateBinder();
	}

	@Override
	protected ISearchTreeContentProvider createContentProvider(
			GroupGenerator<?, Result> generator ){
	
		ReferenceContentProvider provider = new ReferenceContentProvider( generator );
		return provider;
	}
	
	@Override
	protected ILabelProvider createLabelProvider(){
		return new ReferenceLabelProvider();
	}

	@Override
	protected Object createInput(){
		return new ArrayList<Result>();
	}

	@Override
	public void doRemoveAll(){
		ASTReferenceSearchResult result = (ASTReferenceSearchResult)getResult();
		if( result != null ){
			result.clear();
		}
	}

	@Override
	public void doRemoveSelection(){
		List<Result> selection = getSelection();
		ASTReferenceSearchResult result = (ASTReferenceSearchResult)getResult();
		
		if( !selection.isEmpty() ){
			result.remove( selection.toArray( new Result[ selection.size() ] ) );
		}
	}

	@Override
	protected boolean hasSelection(){
		return !getSelection().isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	public List<Result> getSelection(){
		List<Result> result = new ArrayList<Result>();
		ISelection selection = getViewer().getSelection();
		if( selection instanceof IStructuredSelection ){
			Iterator<Object> iterator = ((IStructuredSelection)selection).iterator();
			while( iterator.hasNext() ){
				getSelection( iterator.next(), result );
			}
		}
		return result;
	}
	
	private void getSelection( Object next, List<Result> result ){
		if( next instanceof Group.Wrapper ){
			next = ((Group.Wrapper)next).getNode();
		}
		if( next instanceof Result ){
			result.add( (Result)next );
		}
		if( next instanceof Group ){
			for( Object child : ((Group)next).getChildren() ){
				getSelection( child, result );
			}
		}
	}
}
