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
package tinyos.yeti.search.model.group;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A content provider that is wrapped around another {@link ITreeContentProvider}, 
 * this provider handles {@link Group}s.
 * @author Benjamin Sigg
 */
public class GroupContentProvider implements ITreeContentProvider{
	private DelegateBinder binder;
	private ITreeContentProvider delegate;
	
	public GroupContentProvider( DelegateBinder binder, ITreeContentProvider delegate ){
		this.binder = binder;
		this.delegate = delegate;
	}
	
	public Object[] getChildren( Object element ){
		boolean unwrapped = false;
		if( element instanceof Group.Wrapper ){
			element = ((Group.Wrapper)element).getNode();
			unwrapped = true;
		}
		if( element instanceof Group ){
			return ((Group)element).getChildren();
		}
		if( unwrapped ){
			element = binder.groupChildToContentNode( element );
		}
		
		if( element == null )
			return new Object[]{};
		
		return delegate.getChildren( element );
	}

	public Object getParent( Object element ){
		if( element instanceof Group.Wrapper )
			return ((Group.Wrapper)element).getParent();
		
		if( element instanceof Group ){
			// root
			return null;
		}
		
		return delegate.getParent( element );
	}

	public boolean hasChildren( Object element ){
		boolean unwrapped = false;
		if( element instanceof Group.Wrapper ){
			element = ((Group.Wrapper)element).getNode();
			unwrapped = true;
		}
		if( element instanceof Group ){
			return ((Group)element).hasChildren();
		}
		if( unwrapped ){
			element = binder.groupChildToContentNode( element );
		}
		if( element == null )
			return false;
		
		return delegate.hasChildren( element );
	}

	public Object[] getElements( Object inputElement ){
		if( inputElement instanceof Group ){
			delegate.getElements( binder.getDelegateInput( ((Group)inputElement) ));
			return ((Group)inputElement).getChildren();
		}
		else{
			return delegate.getElements( inputElement );
		}
	}

	public void dispose(){
		delegate.dispose();
	}

	public void inputChanged( Viewer viewer, Object oldInput, Object newInput ){
		// ignore
	}
}
