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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A map containing some preferences which can be written into an {@link IPreferenceStore}
 * when necessary. While this class implements {@link IPreferenceStore}, not
 * all methods are available. Especially setting the default values will
 * throw an exception.<br>
 * Clients can call {@link #transmit()} to transmit all the changes made in
 * this buffer to the underlying store.<br>
 * Note: {@link IPropertyChangeListener}s are not supported.
 * @author Benjamin Sigg
 */
public class BufferedPreferenceStore implements IPreferenceStore {
    private IPreferenceStore base;

    private Set<String> defaults = new HashSet<String>();
    private Map<String, Entry> values = new HashMap<String, Entry>();
    
    public BufferedPreferenceStore( IPreferenceStore base ){
        this.base = base;
    }
    
    /**
     * Transmits the contents of this store to the underlying {@link IPreferenceStore}.
     */
    public void transmit(){
        for( Map.Entry<String, Entry> entry : values.entrySet() ){
            switch( entry.getValue().type ){
                case BOOLEAN:
                    base.setValue( entry.getKey(), (Boolean)entry.getValue().value );
                    break;
                case FLOAT:
                    base.setValue( entry.getKey(), (Float)entry.getValue().value );
                    break;
                case DOUBLE:
                    base.setValue( entry.getKey(), (Double)entry.getValue().value );
                    break;
                case INT:
                    base.setValue( entry.getKey(), (Integer)entry.getValue().value );
                    break;
                case LONG:
                    base.setValue( entry.getKey(), (Long)entry.getValue().value );
                    break;
                case STRING:
                    base.setValue( entry.getKey(), (String)entry.getValue().value );
                    break;
            }
        }
        
        for( String name : defaults ){
            base.setToDefault( name );
        }
        
        clear();
    }

    public void clear(){
        values.clear();
        defaults.clear();
    }
    
    public void addPropertyChangeListener( IPropertyChangeListener listener ){
        // ignore
    }
    
    public void removePropertyChangeListener( IPropertyChangeListener listener ){
        // ignore
    }
    
    public void firePropertyChangeEvent( String name, Object oldValue, Object newValue ){
        // ignore
    }
    
    public boolean contains( String name ){
        return base.contains( name );
    }
    
    public boolean isDefault( String name ){
        return !values.containsKey( name ) && base.isDefault( name );
    }
    
    public boolean needsSaving(){
        return false;
    }
    
    public boolean getDefaultBoolean( String name ){
        return base.getDefaultBoolean( name );
    }

    public double getDefaultDouble( String name ){
        return base.getDefaultDouble( name );
    }

    public float getDefaultFloat( String name ){
        return base.getDefaultFloat( name );
    }

    public int getDefaultInt( String name ){
        return base.getDefaultInt( name );
    }

    public long getDefaultLong( String name ){
        return base.getDefaultLong( name );
    }

    public String getDefaultString( String name ){
        return base.getDefaultString( name );
    }

    public void setDefault( String name, boolean value ){
        throw new UnsupportedOperationException();    
    }
    
    public void setDefault( String name, double value ){
        throw new UnsupportedOperationException();    
    }
    
    public void setDefault( String name, float value ){
        throw new UnsupportedOperationException();
    }
    
    public void setDefault( String name, int value ){
        throw new UnsupportedOperationException();    
    }
    
    public void setDefault( String name, long value ){
        throw new UnsupportedOperationException();
    }
    
    public void setDefault( String name, String defaultObject ){
        throw new UnsupportedOperationException();
    }
    
    public boolean getBoolean( String name ){
        if( defaults.contains( name ))
            return base.getDefaultBoolean( name );
        
        Boolean result = (Boolean)get( name, Type.BOOLEAN );
        if( result != null )
            return result.booleanValue();
        
        return base.getBoolean( name );
    }

    public double getDouble( String name ){
        if( defaults.contains( name ))
            return base.getDefaultDouble( name );
        
        Double result = (Double)get( name, Type.DOUBLE );
        if( result != null )
            return result.doubleValue();
        
        return base.getDouble( name );
    }

    public float getFloat( String name ){
        if( defaults.contains( name ))
            return base.getDefaultFloat( name );
        
        Float result = (Float)get( name, Type.FLOAT );
        if( result != null )
            return result.floatValue();
        
        return base.getFloat( name );
    }

    public int getInt( String name ){
        if( defaults.contains( name ))
            return base.getDefaultInt( name );
        
        Integer result = (Integer)get( name, Type.INT );
        if( result != null )
            return result.intValue();
        
        return base.getInt( name );
    }

    public long getLong( String name ){
        if( defaults.contains( name ))
            return base.getDefaultLong( name );
        
        Long result = (Long)get( name, Type.LONG );
        if( result != null )
            return result.longValue();
        
        return base.getLong( name );
    }

    public String getString( String name ){
        if( defaults.contains( name ))
            return base.getDefaultString( name );
        
        String result = (String)get( name, Type.STRING );
        if( result != null )
            return result;
        
        return base.getString( name );
    }

    private Object get( String name, Type type ){
        Entry result = values.get( name );
        if( result == null )
            return null;
        
        if( result.type != type )
            return null;
        
        return result.value;
    }

    public void setToDefault( String name ){
        values.remove( name );
        defaults.add( name );
    }

    public void putValue( String name, String value ){
        setValue( name, value );
    }
    
    public void setValue( String name, double value ){
        values.put( name, new Entry( Type.DOUBLE, Double.valueOf( value ) ) );
        defaults.remove( name );
    }

    public void setValue( String name, float value ){
        values.put( name, new Entry( Type.FLOAT, Float.valueOf( value ) ) );
        defaults.remove( name );
    }

    public void setValue( String name, int value ){
        values.put( name, new Entry( Type.INT, Integer.valueOf( value ) ) );
        defaults.remove( name );
    }

    public void setValue( String name, long value ){
        values.put( name, new Entry( Type.LONG, Long.valueOf( value ) ) );
        defaults.remove( name );
    }

    public void setValue( String name, String value ){
        values.put( name, new Entry( Type.STRING, value ) );
        defaults.remove( name );
    }

    public void setValue( String name, boolean value ){
        values.put( name, new Entry( Type.BOOLEAN, Boolean.valueOf( value ) ) );
        defaults.remove( name );
    }
    
    private static enum Type{
        DOUBLE, FLOAT, INT, LONG, STRING, BOOLEAN
    }
    
    private static class Entry{
        public Type type;
        public Object value;
        
        public Entry( Type type, Object value ){
            this.type = type;
            this.value = value;
        }
    }
}
