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
package tinyos.yeti.editors.outline;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerComparator;

import tinyos.yeti.editors.NesCIcons;

public abstract class SortTreeAction extends Action {
    private ViewerComparator selected;
    private ViewerComparator deselected;
    
    public SortTreeAction( boolean initialSelection, ViewerComparator selected, ViewerComparator deselected ) {
        this(  initialSelection, selected, deselected, true );
    }
    
    public SortTreeAction( boolean initialSelection, ViewerComparator selected, ViewerComparator deselected, boolean setSortNow ) {
        super("Sort Members", IAction.AS_CHECK_BOX);

        this.selected = selected;
        this.deselected = deselected;
        
        setToolTipText("Sort Ascending");
        setChecked( initialSelection );
        ImageDescriptor im = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_SORT);
        setImageDescriptor(im);     

        if( setSortNow ){
            if (isChecked()) {
                setComparator( selected );
            }
            else{
                setComparator( deselected );
            }
        }
    }
    
    protected abstract void setComparator( ViewerComparator comparator );
    
    protected void informSelected( boolean checked ){
        // do nothing
    }

    @Override
    public void run() {
        //getTreeViewer().refresh();
        informSelected( isChecked() );

        if (isChecked()) {
            setComparator( selected );
        }
        else{
            setComparator( deselected );
        }
    }
}