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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;

/**
 * Provides preferences for font-styles.
 * @author Benjamin Sigg
 */
public class StyleProvider extends AbstractPreferenceProvider<Integer>{
    private IPreferenceStore store;
    
    private IPropertyChangeListener listener = new FilteringPropertyChangeListener(){
        {
            for( String key : TextAttributeConstants.ALL_KEYS ){
                add( TextAttributeConstants.toStyleBoldKey( key ));
                add( TextAttributeConstants.toStyleItalicKey( key ));
            }
        }
        
        @Override
        protected void filteredPropertyChanged( PropertyChangeEvent event ){
            String normal = TextAttributeConstants.toNormalKey( event.getProperty() );
            fireChanged( normal );
        }
    };
    
    public StyleProvider( IPreferenceStore store ){
        this.store = store;
        store.addPropertyChangeListener( listener );
    }
    
    public Integer get( String key ){
        int bold = store.getBoolean( TextAttributeConstants.toStyleBoldKey( key ) ) ? SWT.BOLD : 0;
        int italic = store.getBoolean( TextAttributeConstants.toStyleItalicKey( key ) ) ? SWT.ITALIC : 0;
        
        return bold | italic;
    }
    
    public void dispose(){
        store.removePropertyChangeListener( listener );
    }
}
