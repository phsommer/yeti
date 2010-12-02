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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A {@link IPreferenceStore} which provides access to only a subset
 * of preferences of another {@link IPreferenceStore}. 
 * @author Benjamin Sigg
 * @deprecated not in use anywhere
 */
@Deprecated
public class SubPreferenceStore implements IPreferenceStore{
    private IPreferenceStore base;
    
    private String scope;

    private List<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();
    private IPropertyChangeListener listener = new IPropertyChangeListener(){
        public void propertyChange( PropertyChangeEvent event ){
            if( isSubInWorld( event.getProperty() )){
                firePropertyChangeEvent( worldToSub( event.getProperty() ), event.getNewValue(), event.getOldValue() );
            }
        }
    };
    
    public SubPreferenceStore( IPreferenceStore base, String scope ){
        this.base = base;
        this.scope = scope.length() + "_" + scope + "_";
        
        base.addPropertyChangeListener( listener );
    }
    
    public void dispose(){
        base.removePropertyChangeListener( listener );
    }

    public void addPropertyChangeListener( IPropertyChangeListener listener ){
        if( !listeners.contains( listener )){
            listeners.add( listener );
        }
    }

    public void removePropertyChangeListener( IPropertyChangeListener listener ){
        listeners.remove( listener );
    }

    public void firePropertyChangeEvent( String name, Object oldValue, Object newValue ){
        if( listeners.size() > 0 ){
            PropertyChangeEvent event = new PropertyChangeEvent( this, name, oldValue, newValue );
            IPropertyChangeListener[] array = listeners.toArray( new IPropertyChangeListener[ listeners.size() ] );
            for( IPropertyChangeListener listener : array ){
                listener.propertyChange( event );
            }
        }
    }

    public boolean contains( String name ){
        return base.contains( subToWorld( name ) );
    }

    public boolean getDefaultBoolean( String name ){
        return base.getDefaultBoolean( subToWorld( name ) );
    }
    public double getDefaultDouble( String name ){
        return base.getDefaultDouble( subToWorld( name ) );
    }
    public float getDefaultFloat( String name ){
        return base.getDefaultFloat( subToWorld( name ) );
    }
    public int getDefaultInt( String name ){
        return base.getDefaultInt( subToWorld( name ) );
    }
    public long getDefaultLong( String name ){
        return base.getDefaultLong( subToWorld( name ) );
    }
    public String getDefaultString( String name ){
        return base.getDefaultString( subToWorld( name ) );
    }
    
    public boolean getBoolean( String name ){
        return base.getBoolean( subToWorld( name ) );
    }
    public double getDouble( String name ){
        return base.getDouble( subToWorld( name ) );
    }
    public float getFloat( String name ){
        return base.getFloat( subToWorld( name ) );
    }
    public int getInt( String name ){
        return base.getInt( subToWorld( name ) );
    }
    public long getLong( String name ){
        return base.getLong( subToWorld( name ) );
    }
    public String getString( String name ){
        return base.getString( subToWorld( name ) );
    }

    public boolean isDefault( String name ){
        return base.isDefault( subToWorld( name ) );
    }
    public boolean needsSaving(){
        return base.needsSaving();
    }
    
    public void putValue( String name, String value ){
        base.putValue( subToWorld( name ), value );
    }
    
    public void setDefault( String name, boolean value ){
        base.setDefault( subToWorld( name ), value );
    }
    public void setDefault( String name, double value ){
        base.setDefault( subToWorld( name ), value );
    }
    public void setDefault( String name, float value ){
        base.setDefault( subToWorld( name ), value );
    }
    public void setDefault( String name, int value ){
        base.setDefault( subToWorld( name ), value );
    }
    public void setDefault( String name, long value ){
        base.setDefault( subToWorld( name ), value );
    }
    public void setDefault( String name, String value ){
        base.setDefault( subToWorld( name ), value );
    }
    
    public void setToDefault( String name ){
        base.setToDefault( subToWorld( name ) );
    }
    
    public void setValue( String name, boolean value ){
        base.setValue( subToWorld( name ), value );
    }
    public void setValue( String name, double value ){
        base.setValue( subToWorld( name ), value );
    }
    public void setValue( String name, float value ){
        base.setValue( subToWorld( name ), value );
    }
    public void setValue( String name, int value ){
        base.setValue( subToWorld( name ), value );
    }
    public void setValue( String name, long value ){
        base.setValue( subToWorld( name ), value );
    }
    public void setValue( String name, String value ){
        base.setValue( subToWorld( name ), value );
    }
    
    protected String subToWorld( String name ){
        return scope + name;
    }    
    protected String worldToSub( String name ){
        return name.substring( scope.length() );
    }
    protected boolean isSubInWorld( String name ){
        return name.startsWith( scope );
    }
}
