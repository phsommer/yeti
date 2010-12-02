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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

/**
 * A generic preference provider which uses a different set of cached values
 * for different {@link Device}s.
 * 
 * @author Benjamin Sigg
 *
 * @param <T> the kind of preferences handled by this object
 * @param <K> the internal representation of keys
 */
public abstract class DevicePreferenceProvider<T,K> extends AbstractPreferenceProvider<T>{
    private Map<Device, Map<K, T>> values = new HashMap<Device, Map<K, T>>();
    
    public T get( String key ){
        Device device = Display.getCurrent();
        Map<K, T> current = values.get( device );
        K raw = raw( key );
        
        if( current == null ){
            current = new HashMap<K, T>();
            values.put( device, current );
        }
        
        T value = current.get( key );
        if( value == null ){
            value = create( device, raw );
            current.put( raw, value );
        }
        
        return value;
    }
    
    public void dispose(){
        for( Map.Entry<Device, Map<K, T>> deviceEntry : values.entrySet() ){
            for( Map.Entry<K, T> entry : deviceEntry.getValue().entrySet() ){
                dispose( entry.getValue() );
            }
        }
        
        values.clear();
    }
    
    protected abstract void dispose( T value );
   
    /**
     * Gets the raw representation of <code>key</code>. The raw representation
     * should be enough to create the resource which needs disposing, but
     * does not need to be disposed itself. A {@link String} or an {@link Integer}
     * would be two good examples. Additionally the raw value must support
     * hashing and {@link Object#equals(Object)}.
     * @param key the key of the preference to search
     * @return the raw value of the preference
     */
    protected abstract K raw( String key );
    
    protected abstract T create( Device device, K raw );
}
