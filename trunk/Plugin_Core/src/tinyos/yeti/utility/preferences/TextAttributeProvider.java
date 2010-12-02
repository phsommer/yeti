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
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.Color;

/**
 * A provider for {@link TextAttribute}s which relays on a {@link IPreferenceStore}. 
 * @author Benjamin Sigg
 */
public class TextAttributeProvider extends AbstractPreferenceProvider<TextAttribute>{
    private IMultiPreferenceProvider provider;

    private IPreferenceListener<Integer> styleListener = new FilteringPreferenceListener<Integer>(){
        {
            for( String key : TextAttributeConstants.ALL_KEYS ){
                add( key );
            }
        }
        
        @Override
        protected void filteredPreferenceChanged( IPreferenceProvider<Integer> provider, String name ){
            fireChanged( name );
        }
    };
    
    private IPreferenceListener<Color> colorListener = new FilteringPreferenceListener<Color>(){
        {
            for( String key : TextAttributeConstants.ALL_KEYS ){
                add( TextAttributeConstants.toColorKey( key ));
            }
        }
        
        @Override
        protected void filteredPreferenceChanged( IPreferenceProvider<Color> provider, String name ){
            fireChanged( TextAttributeConstants.toNormalKey( name ));
        }
    };
    
    public TextAttributeProvider( IMultiPreferenceProvider provider ){
        this.provider = provider;
        
        provider.getColors().addPreferenceListener( colorListener );
        provider.getFontStyles().addPreferenceListener( styleListener );
    }
    
    public TextAttribute get( String key ){
        int style = provider.getFontStyles().get( key );
        Color color = provider.getColors().get( TextAttributeConstants.toColorKey( key ) );
        
        return new TextAttribute( color, null, style, null );
    }
    
    public void dispose(){
        // nothing
    }
}
