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
package tinyos.yeti.wizards.content;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic observed is an object that has one property which might change
 * over time, clients can add {@link GenericListener}s to this object in order
 * to get informed about changes. Each {@link GenericObserved} is a
 * {@link GenericListener} as well, so they can be nested. 
 * @author Benjamin Sigg
 *
 * @param <T> the kind of event that is send to the listeners
 */
public class GenericObserved<T> implements GenericListener<T>{
    private List<GenericListener<? super T>> listeners = new ArrayList<GenericListener<? super T>>();
    
    public void addListener( GenericListener<? super T> listener ){
        listeners.add( listener );
    }
    
    public void removeListener( GenericListener<? super T> listener ){
        listeners.remove( listener );
    }
    
    @SuppressWarnings( "unchecked" )
    public void trigger( T value ){
        GenericListener<? super T>[] listeners = this.listeners.toArray( new GenericListener[ this.listeners.size() ] );
        for( GenericListener<? super T> listener : listeners ){
            listener.trigger( value );
        }
    }
}
