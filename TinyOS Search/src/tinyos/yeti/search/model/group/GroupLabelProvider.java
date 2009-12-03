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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class GroupLabelProvider implements ILabelProvider{
	private DelegateBinder binder;
	private ILabelProvider delegate;
	
	public GroupLabelProvider( DelegateBinder binder, ILabelProvider delegate ){
		this.binder = binder;
		this.delegate = delegate;
	}
	
	public void setBinder( DelegateBinder binder ){
		this.binder = binder;
	}

	public Image getImage( Object element ){
		boolean unwrapped = false;
		if( element instanceof Group.Wrapper ){
			element = ((Group.Wrapper)element).getNode();
			unwrapped = true;
		}
		if( element instanceof Group )
			return ((Group)element).getImage();
		
		if( unwrapped )
			element = binder.groupChildToContentNode( element );
		
		return delegate.getImage( element );
	}

	public String getText( Object element ){
		boolean unwrapped = false;
		if( element instanceof Group.Wrapper ){
			element = ((Group.Wrapper)element).getNode();
			unwrapped = true;
		}
		if( element instanceof Group )
			return ((Group)element).getName();
		
		if( unwrapped )
			element = binder.groupChildToContentNode( element );
		
		return delegate.getText( element );
	}

	public void addListener( ILabelProviderListener listener ){
		delegate.addListener( listener );
	}

	public void dispose(){
		delegate.dispose();
	}

	public boolean isLabelProperty( Object element, String property ){
		return delegate.isLabelProperty( element, property );
	}

	public void removeListener( ILabelProviderListener listener ){
		delegate.removeListener( listener );
	}
}
