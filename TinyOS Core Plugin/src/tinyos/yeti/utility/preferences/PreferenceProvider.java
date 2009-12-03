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

/**
 * A generic preference provider which uses maps when possible, and 
 * falls back to factory methods when necessary.
 * 
 * @author Benjamin Sigg
 *
 * @param <T> the kind of preferences handled by this object
 * @param <K> the raw representation of <code>T</code>
 */
public abstract class PreferenceProvider<T, K> extends AbstractPreferenceProvider<T>{
    private  Map<K, T> values = new HashMap<K, T>();
    
    public T get( String key ){
        K raw = raw( key );
        T value = values.get( raw );
        if( value == null ){
            value = create( raw );
            values.put( raw, value );
        }
        return value;
    }
    
    public void dispose(){
        for( Map.Entry<K, T> entry : values.entrySet() ){
            dispose( entry.getValue() );
        }
        values.clear();
    }
    
    protected abstract T create( K raw );
    
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
    
    protected abstract void dispose( T value );
}
