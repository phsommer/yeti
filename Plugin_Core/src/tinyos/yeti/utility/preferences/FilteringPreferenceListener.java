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

import java.util.HashSet;
import java.util.Set;

public abstract class FilteringPreferenceListener<T> implements IPreferenceListener<T>{
    private Set<String> filter = new HashSet<String>();
    
    public void add( String name ){
        filter.add( name );
    }
    
    public void preferenceChanged( IPreferenceProvider<T> provider, String name ){
        if( filter.contains( name )){
            filteredPreferenceChanged( provider, name );
        }
    }
    
    protected abstract void filteredPreferenceChanged( IPreferenceProvider<T> provider, String name );
}
