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
package tinyos.yeti.utility.preferences;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPreferenceProvider<T> implements IPreferenceProvider<T>{
    private List<IPreferenceListener<T>> listeners = new ArrayList<IPreferenceListener<T>>();
    
    public void addPreferenceListener( IPreferenceListener<T> listener ){
        listeners.add( listener );
    }
    
    public void removePreferenceListener( IPreferenceListener<T> listener ){
        listeners.remove( listener );
    }
    
    @SuppressWarnings("unchecked")
    protected void fireChanged( String name ){
        IPreferenceListener<T>[] array = listeners.toArray( new IPreferenceListener[ listeners.size() ] );
        for( IPreferenceListener<T> listener : array ){
            listener.preferenceChanged( this, name );
        }
    }
}
