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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkingSet;

import tinyos.yeti.search.model.SearchScope;

/**
 * A {@link SearchScope} that evaluates a {@link ISelection}, searching for
 * {@link IResource}s and for {@link IWorkingSet}s.
 * @author Benjamin Sigg
 */
public class SelectionScope extends AbstractScope{
	private ISelection selection;
	
	public SelectionScope( ISelection selection ){
		this.selection = selection;
	}
	
	@SuppressWarnings("unchecked")
	public void visit( Visitor visitor, IProgressMonitor monitor ) throws CoreException{
		monitor.beginTask( "Search", 10 );
		
		if( selection instanceof IStructuredSelection && !selection.isEmpty() ){
			IProgressMonitor collectMonitor = new SubProgressMonitor( monitor, 1 );
			collectMonitor.beginTask( "Collect", IProgressMonitor.UNKNOWN );
			
			Set<IFile> files = new HashSet<IFile>();
			
			Iterator<Object> iterator = ((IStructuredSelection)selection).iterator();
			while( iterator.hasNext() ){
				Object current = iterator.next();
				
				if( current instanceof IWorkingSet ){
					IWorkingSet set = (IWorkingSet)current;
					IAdaptable[] elements = set.getElements();
					for( IAdaptable element : elements ){
						IResource resource = (IResource)element.getAdapter( IResource.class );
						if( resource instanceof IFile ){
							addToCollection( resource, files );
						}
					}
				}
				else if( current instanceof IAdaptable ){
					IResource resource = (IResource)((IAdaptable)current).getAdapter( IResource.class );
					if( resource instanceof IFile ){
						files.add( (IFile)resource );
					}
				}
			}
			
			collectMonitor.done();
			
			IFile[] array = files.toArray( new IFile[ files.size() ] );
			
			IProgressMonitor visitMonitor = new SubProgressMonitor( monitor, 9 );
			visitMonitor.beginTask( "Visit", array.length );
			
			for( IFile file : array ){
				visit( visitor, file, new SubProgressMonitor( visitMonitor, 1 ) );
				if( monitor.isCanceled() ){
					break;
				}
			}
		}
		
		monitor.done();
	}
	
	@SuppressWarnings("unchecked")
	public String getDescription(){
		if( selection instanceof IStructuredSelection ){
			List<String> names = new ArrayList<String>();
			
			Iterator<Object> iterator = ((IStructuredSelection)selection).iterator();
			while( iterator.hasNext() && names.size() < 3 ){
				Object current = iterator.next();
				
				if( current instanceof IWorkingSet ){
					IWorkingSet set = (IWorkingSet)current;
					names.add( set.getLabel() );
				}
				else if( current instanceof IAdaptable ){
					IResource resource = (IResource)((IAdaptable)current).getAdapter( IResource.class );
					if( resource instanceof IFile ){
						names.add( ((IFile)resource).getName() );
					}
				}
			}
			
			if( names.size() == 0 )
				return "";
			if( names.size() == 1 )
				return names.get( 0 );
			if( names.size() == 2 )
				return names.get( 0 ) + ", " + names.get( 1 );
			return names.get( 0 ) + ", " + names.get( 1 ) + ", ...";
		}
		else
			return "";
	}
}
