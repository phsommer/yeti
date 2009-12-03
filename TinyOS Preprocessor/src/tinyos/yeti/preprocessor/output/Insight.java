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
package tinyos.yeti.preprocessor.output;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A map containing information about a message.
 * @author Benjamin Sigg
 */
public class Insight{
    public static Insight base( int id ){
        Insight insight = new Insight();
        return insight.setId( id );
    }
    
    /** boolean set to <code>true</code> if this information was created through the default constructor */
    public static final String KEY_MESSAGE = "m.info.message";
    
    /** integer of {@link Insights} telling what kind of information can be found */
    public static final String KEY_ID = "m.info.id";
    
    private boolean sealed = false;
    private Map<String, Object> map = new HashMap<String, Object>();
    
    /**
     * Creates a new {@link Insight} with the {@link #KEY_MESSAGE}
     * attribute set. 
     */
    public Insight(){
        put( KEY_MESSAGE, true );
    }
    
    /**
     * Creates a new {@link Insight} whit the {@link #KEY_MESSAGE}
     * not set (unless it is set in <code>map</code>).
     * @param map the initial content of this object, can be <code>null</code>
     */
    public Insight( Map<String, Object> map ){
        if( map != null )
            this.map.putAll( map );
    }
    
    public Map<String, Object> getMap(){
        return Collections.unmodifiableMap( map );
    }
    
    public boolean isMessage(){
        return get( KEY_MESSAGE, false );
    }
    
    public Insight setId( int id ){
        return put( KEY_ID, id );
    }
    
    public int getId(){
        return get( KEY_ID, Insights.UNKNOWN );
    }
    
    /**
     * Creates a completely independent copy of this object.
     * @return an unsealed copy
     */
    public Insight copy(){
        Insight information = new Insight();
        information.map = new HashMap<String, Object>( map );
        return information;
    }
    
    /**
     * Seals this object, it becomes immutable.
     * @return a sealed version of this object
     */
    public Insight seal(){
        sealed = true;
        return this;
    }
    
    public Object get( String key ){
        return map.get( key );
    }
    
    public int get( String key, int defaultValue ){
        Object result = get( key );
        if( result instanceof Integer ){
            return ((Integer)result).intValue();
        }
        return defaultValue;
    }
    
    public String get( String key, String defaultValue ){
        Object result = get( key );
        if( result instanceof String ){
            return (String)result;
        }
        return defaultValue;
    }
    
    public boolean get( String key, boolean defaultValue ){
        Object result = get( key );
        if( result instanceof Boolean ){
            return ((Boolean)result).booleanValue();
        }
        return defaultValue;
    }
    
    /**
     * Stores an additional key-value pair, returns an unsealed version of
     * this object.
     * @param key the key of the pair
     * @param value the value of the pair
     * @return an unsealed version of this containing <code>key/value</code>
     */
    public Insight put( String key, int value ){
        return putValue( key, Integer.valueOf( value ));
    }

    /**
     * Stores an additional key-value pair, returns an unsealed version of
     * this object.
     * @param key the key of the pair
     * @param value the value of the pair
     * @return an unsealed version of this containing <code>key/value</code>
     */
    public Insight put( String key, String value ){
        return putValue( key, value );
    }
    
    /**
     * Stores an additional key-value pair, returns an unsealed version of
     * this object.
     * @param key the key of the pair
     * @param value the value of the pair
     * @return an unsealed version of this containing <code>key/value</code>
     */
    public Insight put( String key, boolean value ){
        return putValue( key, Boolean.valueOf( value ));
    }
    
    /**
     * Stores an additional key-value pair, returns an unsealed version of
     * this object.
     * @param key the key of the pair
     * @param value the value of the pair
     * @return an unsealed version of this containing <code>key/value</code>
     */
    private Insight putValue( String key, Object value ){
        if( sealed ){
            return copy().putValue( key, value );
        }
        map.put( key, value );
        return this;
    }
    
    @Override
    public String toString(){
        return map.toString();
    }
}
