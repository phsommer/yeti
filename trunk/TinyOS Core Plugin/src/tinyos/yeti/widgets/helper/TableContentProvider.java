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
package tinyos.yeti.widgets.helper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class TableContentProvider<E> implements IStructuredContentProvider{
    private List<E> content = new ArrayList<E>();
    private TableViewer viewer;
    
    public TableContentProvider( TableViewer viewer ){
        this.viewer = viewer;
    }
    
    public int getSize(){
        return content.size();
    }
    
    public E getEntry( int index ){
        return content.get( index );
    }
    
    public void refresh( E entry ){
        viewer.refresh( entry );
    }
    
    public void add( E entry ){
        add( entry, content.size() );
    }
    
    public void add( E entry, int index ){
        content.add( index, entry );
        viewer.insert( entry, index );
    }
    
    public void remove( E[] entries ){
        for( E value : entries ){
            remove( value );
        }
    }
    
    public void remove( E entry ){
        int index = content.indexOf( entry );
        if( index >= 0 ){
            remove( index );
        }
    }
    
    public void remove( int index ){
        E entry = content.remove( index );
        viewer.remove( entry );
    }
    
    public void clear(){
        Object[] elements = getElements( null );
        content.clear();
        viewer.remove( elements );
    }
    
    public Object[] getElements( Object inputElement ){
        return content.toArray();
    }

    public void dispose(){
        // ignore
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ){
        // ignore
    }
}
