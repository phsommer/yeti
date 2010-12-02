/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2010 ETH Zurich
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

package tinyos.yeti.editors.outline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCIcons;

/**
 * An action opening a dialog allowing to select a set of {@link IOutlineFilter}s.
 * @author Benjamin Sigg
 *
 */
public class FilterSelectionAction extends Action{
	private NesCOutlinePage page;
	
	public FilterSelectionAction( NesCOutlinePage page ){
		super( "Filter...", AS_PUSH_BUTTON );
		this.page = page;
		setImageDescriptor( NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_FILTER ) );
	}
	
	@Override
	public void run(){
		openDialog();
	}
	
	private void openDialog(){
		OutlineFilterFactory[] filters = TinyOSPlugin.getDefault().getOutlineFilters();
		
		ListSelectionDialog dialog = new ListSelectionDialog(
				page.getSite().getShell(), 
				filters,
				new ContentProvider(),
				new LabelProvider(),
				"Select filters for the Outline view" );
		
		String[] ids = page.getFilters();
		Set<String> idSet = new HashSet<String>();
		for( String id : ids ){
			idSet.add( id );
		}
		List<OutlineFilterFactory> selection = new ArrayList<OutlineFilterFactory>();
		for( OutlineFilterFactory filter : filters ){
			if( idSet.contains( filter.getId() )){
				selection.add( filter );
			}
		}
		dialog.setInitialElementSelections( selection );
		if( Window.OK == dialog.open() ){
			Object[] result = dialog.getResult();
			if( result != null ){
				ids = new String[ result.length ];
				for( int i = 0; i < result.length; i++ ){
					ids[i] = ((OutlineFilterFactory)result[i]).getId();
				}
				page.setFilters( ids );
			}
		}
	}
	
	private class ContentProvider implements IStructuredContentProvider{
		public Object[] getElements( Object inputElement ){
			return (Object[])inputElement;
		}

		public void dispose(){
			// nothing
		}

		public void inputChanged( Viewer viewer, Object oldInput, Object newInput ){
			// nothing
		}		
	}
	
	private class LabelProvider implements ILabelProvider{
		public Image getImage( Object element ){
			return null;
		}

		public String getText( Object element ){
			return ((OutlineFilterFactory)element).getName();
		}

		public void addListener( ILabelProviderListener listener ){
			// nothing
		}

		public void dispose(){
			// nothing
		}

		public boolean isLabelProperty( Object element, String property ){
			return true;
		}

		public void removeListener( ILabelProviderListener listener ){
			// nothing
		}		
	}
}