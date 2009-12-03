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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This wrapper for a {@link IPreferenceStore} reads values from another
 * store and transfers them to a new store. This procedure happens exactly one
 * time ever, and won't be repeated in future runs.
 * @author Benjamin Sigg
 */
public class OldPluginPreferenceStore implements IPreferenceStore{
	private IPreferenceStore oldStore;
	private IPreferenceStore delegate;
	private String oldSymbolicName;
	
	private Set<String> handled = new HashSet<String>();
	
	public OldPluginPreferenceStore( IPreferenceStore delegate, String oldSymbolicName ){
		this.delegate = delegate;
		this.oldSymbolicName = oldSymbolicName;
	}
	
	private void dig( String key ){
		if( handled.add( key )){
			if( !delegate.getBoolean( "digged_" + key )){
				delegate.setValue( "digged_" + key, true );
				IPreferenceStore old = openOld();
				if( !old.isDefault( key )){
					delegate.setValue( key, old.getString( key ) );
				}
			}
		}
	}
	
	private IPreferenceStore openOld(){
		if( oldStore == null ){
			oldStore = new ScopedPreferenceStore( new InstanceScope(), oldSymbolicName );
		}
		return oldStore;
	}
	
	public void addPropertyChangeListener( IPropertyChangeListener listener ){
		delegate.addPropertyChangeListener( listener );
	}

	public boolean contains( String name ){
		return delegate.contains( name );
	}

	public void firePropertyChangeEvent( String name, Object oldValue, Object newValue ){
		delegate.firePropertyChangeEvent( name, oldValue, newValue );
	}

	public boolean getBoolean( String name ){
		dig( name );
		return delegate.getBoolean( name );
	}

	public boolean getDefaultBoolean( String name ){
		return delegate.getDefaultBoolean( name );
	}

	public double getDefaultDouble( String name ){
		return delegate.getDefaultDouble( name );
	}

	public float getDefaultFloat( String name ){
		return delegate.getDefaultFloat( name );
	}

	public int getDefaultInt( String name ){
		return delegate.getDefaultInt( name );
	}

	public long getDefaultLong( String name ){
		return delegate.getDefaultLong( name );
	}

	public String getDefaultString( String name ){
		return delegate.getDefaultString( name );
	}

	public double getDouble( String name ){
		dig( name );
		return delegate.getDouble( name );
	}

	public float getFloat( String name ){
		dig( name );
		return delegate.getFloat( name );
	}

	public int getInt( String name ){
		dig( name );
		return delegate.getInt( name );
	}

	public long getLong( String name ){
		dig( name );
		return delegate.getLong( name );
	}

	public String getString( String name ){
		dig( name );
		return delegate.getString( name );
	}

	public boolean isDefault( String name ){
		dig( name );
		return delegate.isDefault( name );
	}

	public boolean needsSaving(){
		return delegate.needsSaving();
	}

	public void putValue( String name, String value ){
		dig( name );
		delegate.putValue( name, value );
	}

	public void removePropertyChangeListener( IPropertyChangeListener listener ){
		delegate.removePropertyChangeListener( listener );
	}

	public void setDefault( String name, double value ){
		delegate.setDefault( name, value );	
	}

	public void setDefault( String name, float value ){
		delegate.setDefault( name, value );
	}

	public void setDefault( String name, int value ){
		delegate.setDefault( name, value );
	}

	public void setDefault( String name, long value ){
		delegate.setDefault( name, value );
	}

	public void setDefault( String name, String defaultObject ){
		delegate.setDefault( name, defaultObject );
	}

	public void setDefault( String name, boolean value ){
		delegate.setDefault( name, value );
	}

	public void setToDefault( String name ){
		delegate.setToDefault( name );
	}

	public void setValue( String name, double value ){
		dig( name );
		delegate.setValue( name, value );
	}

	public void setValue( String name, float value ){
		dig( name );
		delegate.setValue( name, value );
	}

	public void setValue( String name, int value ){
		dig( name );
		delegate.setValue( name, value );
	}

	public void setValue( String name, long value ){
		dig( name );
		delegate.setValue( name, value );	
	}

	public void setValue( String name, String value ){
		dig( name );
		delegate.setValue( name, value );	
	}

	public void setValue( String name, boolean value ){
		dig( name );
		delegate.setValue( name, value );
	}
}
